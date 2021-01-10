package me.sakigamiyang.httpbin4j.handlers;

import org.eclipse.jetty.server.Request;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class ResponseFormatHandler {
    public enum CompressionType {
        FLAT,
        BROTLI,
        DEFLATE,
        GZIP
    }

    public static void handleCompression(Request baseRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response,
                                         CompressionType compressionType) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            JSONObject body = new JSONObject();
            body.put("origin", request.getRemoteAddr());
            body.put("method", request.getMethod());
            body.put("headers", Common.mapHeadersToJSON(request));
            if (compressionType == CompressionType.BROTLI) {
                body.put("brotli", true);
                response.setHeader("Content-Encoding", "br");
            } else if (compressionType == CompressionType.DEFLATE) {
                body.put("deflated", true);
                response.setHeader("Content-Encoding", "deflate");
            } else if (compressionType == CompressionType.GZIP) {
                body.put("gzipped", true);
                response.setHeader("Content-Encoding", "gzip");
            } else {
                throw new IllegalArgumentException(
                        String.format("'compressionType' should not be '%s'", CompressionType.FLAT));
            }
            Common.respondJSON(response, os, body, HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }
}
