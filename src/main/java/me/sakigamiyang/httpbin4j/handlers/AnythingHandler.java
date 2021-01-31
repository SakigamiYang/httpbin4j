package me.sakigamiyang.httpbin4j.handlers;

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

public class AnythingHandler {
    public static void handle(Request baseRequest,
                              HttpServletRequest request,
                              HttpServletResponse response) throws IOException {
        try (InputStream is = request.getInputStream();
             OutputStream os = response.getOutputStream()) {
            String contentType = request.getContentType();
            JSONObject body = new JSONObject();

            body.put("url", Common.getFullURL(request));
            body.put("args", Common.mapArgsToJSON(request));
            body.put("headers", Common.mapHeadersToJSON(request));
            body.put("origin", Common.getClientIpAddress(request));
            body.put("method", request.getMethod());

            if (contentType != null && contentType.startsWith("multipart/form-data")) {
                JSONObject form = new JSONObject();
                MultiPartFormInputStream stream = new MultiPartFormInputStream(is, contentType, null, null);
                for (Part part : stream.getParts()) {
                    try (InputStream pis = part.getInputStream();
                         ByteArrayOutputStream pbaos = new ByteArrayOutputStream()) {
                        Common.copyStream(pis, pbaos);
                        form.put(part.getName(), pbaos.toString("UTF-8"));
                    }
                }
                body.put("data", "");
                body.put("form", form);
                body.put("json", JSONObject.NULL);
            } else {
                String str;
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    Common.copyStream(is, baos);
                    str = baos.toString("UTF-8");
                }
                body.put("data", str);
                body.put("form", new JSONObject());
                body.put("files", new JSONObject());
                try {
                    body.put("json", new JSONObject(str));
                } catch (JSONException e) {
                    body.put("json", JSONObject.NULL);
                }
            }

            Common.respondJSON(response, os, body, HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }
}
