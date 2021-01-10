package me.sakigamiyang.httpbin4j.handlers;

import org.eclipse.jetty.server.Request;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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

    public static void handleEncodingUTF8(Request baseRequest,
                                          HttpServletResponse response) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            byte[] body = Common.getResource("/demo.txt");
            Common.respondHTML(response, os, body, HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }

    public static void handleHTML(Request baseRequest,
                                  HttpServletResponse response) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            byte[] body = Common.getResource("/demo.html");
            Common.respondHTML(response, os, body, HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }

    public static void handleJSON(Request baseRequest,
                                  HttpServletResponse response) throws IOException, ParseException {
        try (OutputStream os = response.getOutputStream()) {
            String s = new String(Common.getResource("/demo.json"), StandardCharsets.UTF_8);
            JSONObject json = (JSONObject) new JSONParser().parse(s);
            Common.respondJSON(response, os, json, HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }

    public static void handleXML(Request baseRequest,
                                 HttpServletResponse response) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            byte[] body = Common.getResource("/demo.xml");
            Common.respondXML(response, os, body, HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }

    public static void handleRobotsTxt(Request baseRequest,
                                       HttpServletResponse response) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            byte[] body = Common.getResource("/robots.txt");
            Common.respondPlainText(response, os, body, HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }
}
