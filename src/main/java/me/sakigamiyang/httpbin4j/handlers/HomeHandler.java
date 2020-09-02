package me.sakigamiyang.httpbin4j.handlers;

import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HomeHandler {
    public static void handleHome(Request baseRequest,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Content-Type", "text/html; charset=utf-8");
        Common.copyResource(response, "/index.html");
        baseRequest.setHandled(true);
    }
}
