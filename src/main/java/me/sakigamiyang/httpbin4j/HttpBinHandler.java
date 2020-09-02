package me.sakigamiyang.httpbin4j;

import lombok.extern.slf4j.Slf4j;
import me.sakigamiyang.httpbin4j.handlers.HomeHandler;
import me.sakigamiyang.httpbin4j.handlers.HttpMethodHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * HttpBinHandler.
 */
@Slf4j
public class HttpBinHandler extends AbstractHandler {
    private static final int MAX_DELAY_MS = 10 * 1000;  // delay = 10s

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        if (uri.equals("/")) {
            HomeHandler.handleHome(baseRequest, request, response);
        } else if ((method.equals("DELETE") && uri.equals("/delete")) ||
                (method.equals("GET") && uri.equals("/get")) ||
                (method.equals("PATCH") && uri.equals("/patch")) ||
                (method.equals("POST") && uri.equals("/post")) ||
                (method.equals("PUT") && uri.equals("/put"))) {
            HttpMethodHandler.handleHttpMethod(baseRequest, request, response);
        }
    }
}
