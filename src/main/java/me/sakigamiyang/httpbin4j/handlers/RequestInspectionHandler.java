package me.sakigamiyang.httpbin4j.handlers;

import org.eclipse.jetty.server.Request;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class RequestInspectionHandler {
    public static void handleHeaders(Request baseRequest,
                                     HttpServletRequest request,
                                     HttpServletResponse response) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            JSONObject body = new JSONObject();
            body.put("headers", Common.mapHeadersToJSON(request));
            Common.respondJSON(response, os, body, HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }

    public static void handleIP(Request baseRequest,
                                HttpServletRequest request,
                                HttpServletResponse response) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            JSONObject body = new JSONObject();
            body.put("origin", Common.getClientIpAddress(request));
            Common.respondJSON(response, os, body, HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }

    public static void handleUserAgent(Request baseRequest,
                                       HttpServletRequest request,
                                       HttpServletResponse response) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            JSONObject body = new JSONObject();
            body.put("user-agent", request.getHeader("User-Agent"));
            Common.respondJSON(response, os, body, HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }
}
