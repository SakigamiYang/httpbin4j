package me.sakigamiyang.httpbin4j;

import lombok.extern.slf4j.Slf4j;
import me.sakigamiyang.httpbin4j.handlers.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.simple.parser.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

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

        if (method.equals("GET") && uri.startsWith("/basic-auth/")) {
            // /basic-auth/{user}/{passwd}
            String[] auth = uri.substring("/basic-auth/".length()).split("/", 2);
            if (auth.length != 2) {
                response.sendRedirect("/deny");
            }
            String user = auth[0];
            String passwd = auth[1];
            AuthHandler.handleBasicAuth(baseRequest, request, response, user, passwd);
        } else if (method.equals("GET") && uri.startsWith("/bearer")) {
            AuthHandler.handleBearer(baseRequest, request, response);
        } else if (method.equals("GET") && uri.startsWith("/digest-auth/")) {
            // /hidden-basic-auth/{user}/{passwd}
            String[] auth = uri.substring("/digest-auth/".length()).split("/", 5);
            if (auth.length < 3 || auth.length > 5) {
                response.sendRedirect("/deny");
            }
            String qop = auth[0];
            String user = auth[1];
            String passwd = auth[2];
            String algorithm = auth.length < 4 ? null : auth[3].toLowerCase();
            String staleAfter = auth.length < 5 ? null : auth[4].toLowerCase();
            AuthHandler.handleDigestAuth(baseRequest, request, response, qop, user, passwd, algorithm, staleAfter);
        } else if (method.equals("GET") && uri.startsWith("/hidden-basic-auth/")) {
            // /hidden-basic-auth/{user}/{passwd}
            String[] auth = uri.substring("/hidden-basic-auth/".length()).split("/", 2);
            if (auth.length != 2) {
                response.sendRedirect("/deny");
            }
            String user = auth[0];
            String passwd = auth[1];
            AuthHandler.handleHiddenBasicAuth(baseRequest, request, response, user, passwd);
        }



        else if (uri.startsWith("/anything")) {
            AnythingHandler.handle(baseRequest, request, response);
        }

        // disallowed route
        if (!baseRequest.isHandled()) {
            response.sendRedirect("/deny");
        }
    }
}
