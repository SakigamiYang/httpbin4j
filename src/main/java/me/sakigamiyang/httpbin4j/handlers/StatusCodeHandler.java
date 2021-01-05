package me.sakigamiyang.httpbin4j.handlers;

import me.sakigamiyang.httpbin4j.Utils;
import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class StatusCodeHandler {
    public static void handleStatus(Request baseRequest,
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    Integer[] codes) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            int status =  Utils.randomChoice(codes);
            Common.respondHTML(response, os, new byte[0], status);
            baseRequest.setHandled(true);
        }
    }
}
