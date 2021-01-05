package me.sakigamiyang.httpbin4j.handlers;

import org.eclipse.jetty.server.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class ResponseInspectionHandler {
    public static void handleCache(Request baseRequest,
                                   HttpServletRequest request,
                                   HttpServletResponse response) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            String ifModifiedSince = request.getHeader("If-Modified-Since");
            String ifNoneMatch = request.getHeader("If-None-Match");
            boolean notConditional = ifModifiedSince == null && ifNoneMatch == null;

            if (notConditional) {
                Common.respondHTML(response, os, new byte[0], HttpServletResponse.SC_NOT_MODIFIED);
            } else {
                JSONObject body = new JSONObject();
                body.put("url", Common.getFullURL(request));
                body.put("origin", request.getRemoteAddr());
                body.put("headers", Common.mapHeadersToJSON(request));
                body.put("args", Common.mapArgsToJSON(request));
                response.setDateHeader("Last-Modified", new Date().getTime());
                response.setHeader("ETag", UUID.randomUUID().toString().replace("-", ""));
                Common.respondJSON(response, os, body, HttpServletResponse.SC_OK);
            }

            baseRequest.setHandled(true);
        }
    }

    public static void handleCacheValue(Request baseRequest,
                                        HttpServletRequest request,
                                        HttpServletResponse response,
                                        int value) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            JSONObject body = new JSONObject();
            body.put("url", Common.getFullURL(request));
            body.put("origin", request.getRemoteAddr());
            body.put("headers", Common.mapHeadersToJSON(request));
            body.put("args", Common.mapArgsToJSON(request));
            response.setHeader("Cache-Control", String.format("public, max-age=%s", value));
            Common.respondJSON(response, os, body, HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }

    private static List<String> parseMultiValueHeader(String headerName) {
        return new ArrayList<>();
    }

    public static void handleEtag(Request baseRequest,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  String etag) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            List<String> ifNoneMatch = parseMultiValueHeader(request.getHeader("If-None-Match"));
            List<String> ifMatch = parseMultiValueHeader(request.getHeader("If-Match"));

            if (!ifNoneMatch.isEmpty() &&
                    (ifNoneMatch.contains(etag) || ifNoneMatch.contains("*"))) {
                response.setHeader("ETag", etag);
                Common.respondHTML(response, os, new byte[0], HttpServletResponse.SC_NOT_MODIFIED);
            } else if (!ifMatch.isEmpty() &&
                    (!ifNoneMatch.contains(etag) && !ifNoneMatch.contains("*"))) {
                Common.respondHTML(response, os, new byte[0], HttpServletResponse.SC_PRECONDITION_FAILED);
            } else {
                JSONObject body = new JSONObject();
                body.put("url", Common.getFullURL(request));
                body.put("origin", request.getRemoteAddr());
                body.put("headers", Common.mapHeadersToJSON(request));
                body.put("args", Common.mapArgsToJSON(request));
                response.setHeader("ETag", etag);
                Common.respondJSON(response, os, body, HttpServletResponse.SC_OK);
            }

            baseRequest.setHandled(true);
        }
    }

    public static void handleResponseHeaders(Request baseRequest,
                                             HttpServletRequest request,
                                             HttpServletResponse response) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            JSONObject body = new JSONObject();
            Enumeration<String> parameterNames = request.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String parameterName = parameterNames.nextElement();
                String[] parameterValues = request.getParameterValues(parameterName);
                body.put(parameterName, new JSONArray(parameterValues));
            }
            body.put("Content-Type", "application/json");

            int currBodyLength = body.length();
            while (true) {
                body.put("Content-Length", currBodyLength);
                int nextBodyLength = body.length();
                if (nextBodyLength == currBodyLength) {
                    break;
                }
                currBodyLength = nextBodyLength;
            }

            Common.respondJSON(response, os, body, HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }
}
