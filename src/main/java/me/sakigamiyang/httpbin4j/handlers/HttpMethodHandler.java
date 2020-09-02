package me.sakigamiyang.httpbin4j.handlers;

import me.sakigamiyang.httpbin4j.Utils;
import org.eclipse.jetty.http.MultiPartFormInputStream;
import org.eclipse.jetty.server.Request;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpMethodHandler {
    public static void handleHttpMethod(Request baseRequest,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws IOException {
        try (InputStream is = request.getInputStream();
             OutputStream os = response.getOutputStream()) {
            String contentType = request.getContentType();
            JSONObject body = new JSONObject();

            if (contentType != null && contentType.startsWith("multipart/form-data")) {
                MultiPartFormInputStream stream = new MultiPartFormInputStream(is, contentType, null, null);
                JSONObject data = new JSONObject();
                for (Part part : stream.getParts()) {
                    try (InputStream pis = part.getInputStream();
                         ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        Utils.copyStream(pis, baos);
                        data.put(part.getName(), new String(baos.toByteArray(), StandardCharsets.UTF_8));
                    }
                }
                body.put("data", "");
                body.put("form", data);
                body.put("json", JSONObject.NULL);
            } else {
                String str;
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    Utils.copyStream(is, baos);
                    str = new String(baos.toByteArray(), StandardCharsets.UTF_8);
                }
                body.put("data", str);
                try {
                    body.put("json", new JSONObject(str));
                } catch (JSONException e) {
                    // Client can provide non-JSON data.
                }
            }

            body.put("url", Common.getFullURL(request));
            body.put("origin", request.getRemoteAddr());
            body.put("headers", Common.mapHeadersToJSON(request));
            body.put("args", Common.mapArgsToJSON(request));

            Common.respondJSON(response, os, body);
            baseRequest.setHandled(true);
        }
    }
}
