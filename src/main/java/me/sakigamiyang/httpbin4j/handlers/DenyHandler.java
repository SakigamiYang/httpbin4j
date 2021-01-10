package me.sakigamiyang.httpbin4j.handlers;

import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class DenyHandler {
    public static void handle(Request baseRequest,
                              HttpServletResponse response) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            byte[] body = Common.getResource("/deny.html");
            Common.respondHTML(response, os, body, HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }
}
