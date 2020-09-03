package me.sakigamiyang.httpbin4j.handlers;

import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DenyHandler {
    public static void handle(Request baseRequest,
                              HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Content-Type", "text/html; charset=utf-8");
        Common.copyResource(response, "/deny.html");
        baseRequest.setHandled(true);
    }
}