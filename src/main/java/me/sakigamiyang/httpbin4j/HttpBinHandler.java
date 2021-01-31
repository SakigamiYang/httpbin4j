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

        if (uri.equals("/")) {
            HomeHandler.handle(baseRequest, response);
        } else if (uri.startsWith("/deny")) {
            DenyHandler.handle(baseRequest, response);
        } else if ((method.equals("DELETE") && uri.equals("/delete"))
                || (method.equals("GET") && uri.equals("/get"))
                || (method.equals("PATCH") && uri.equals("/patch"))
                || (method.equals("POST") && uri.equals("/post"))
                || (method.equals("PUT") && uri.equals("/put"))) {
            HttpMethodHandler.handleHttpMethod(baseRequest, request, response);
        } else if (method.equals("GET") && uri.startsWith("/basic-auth/")) {
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
        } else if (uri.startsWith("/status/")) {
            // /status/{codes}
            try {
                String[] strCodes = uri.substring("/status/".length()).split(",");
                Integer[] codes = Arrays.stream(strCodes).map(Integer::parseInt).toArray(Integer[]::new);
                StatusCodeHandler.handleStatus(baseRequest, request, response, codes);
            } catch (Throwable t) {
                response.sendRedirect("/deny");
            }
        } else if (method.equals("GET") && uri.startsWith("/headers")) {
            RequestInspectionHandler.handleHeaders(baseRequest, request, response);
        } else if (method.equals("GET") && uri.startsWith("/ip")) {
            RequestInspectionHandler.handleIP(baseRequest, request, response);
        } else if (method.equals("GET") && uri.startsWith("/user-agent")) {
            RequestInspectionHandler.handleUserAgent(baseRequest, request, response);
        } else if (method.equals("GET") && uri.startsWith("/cache/")) {
            // /cache/{value}
            try {
                String strValue = uri.substring("/cache/".length());
                int value = Integer.parseInt(strValue);
                ResponseInspectionHandler.handleCacheValue(baseRequest, request, response, value);
            } catch (NumberFormatException e) {
                response.sendRedirect("/deny");
            }
        } else if (method.equals("GET") && uri.startsWith("/cache")) {
            ResponseInspectionHandler.handleCache(baseRequest, request, response);
        } else if (method.equals("GET") && uri.startsWith("/etag/")) {
            // /etag/{etag}
            String etag = uri.substring("/etag/".length());
            ResponseInspectionHandler.handleEtag(baseRequest, request, response, etag);
        } else if ((method.equals("GET")
                || method.equals("POST"))
                && uri.startsWith("/response-headers")) {
            ResponseInspectionHandler.handleResponseHeaders(baseRequest, request, response);
        } else if (method.equals("GET") && uri.startsWith("/brotli")) {
            ResponseFormatHandler.handleCompression(
                    baseRequest, request, response, ResponseFormatHandler.CompressionType.BROTLI);
        } else if (method.equals("GET") && uri.startsWith("/deflate")) {
            ResponseFormatHandler.handleCompression(
                    baseRequest, request, response, ResponseFormatHandler.CompressionType.DEFLATE);
        } else if (method.equals("GET") && uri.startsWith("/gzip")) {
            ResponseFormatHandler.handleCompression(
                    baseRequest, request, response, ResponseFormatHandler.CompressionType.GZIP);
        } else if (method.equals("GET") && uri.startsWith("/encoding/utf8")) {
            ResponseFormatHandler.handleEncodingUTF8(baseRequest, response);
        } else if (method.equals("GET") && uri.startsWith("/html")) {
            ResponseFormatHandler.handleHTML(baseRequest, response);
        } else if (method.equals("GET") && uri.startsWith("/json")) {
            try {
                ResponseFormatHandler.handleJSON(baseRequest, response);
            } catch (ParseException e) {
                response.sendRedirect("/deny");
            }
        } else if (method.equals("GET") && uri.startsWith("/xml")) {
            ResponseFormatHandler.handleXML(baseRequest, response);
        } else if (method.equals("GET") && uri.startsWith("/robots.txt")) {
            ResponseFormatHandler.handleRobotsTxt(baseRequest, response);
        } else if (method.equals("GET") && uri.startsWith("/image/jpeg")) {
            ImageHandler.handleImageWithType(baseRequest, response, ImageHandler.ImageType.JPEG);
        } else if (method.equals("GET") && uri.startsWith("/image/png")) {
            ImageHandler.handleImageWithType(baseRequest, response, ImageHandler.ImageType.PNG);
        } else if (method.equals("GET") && uri.startsWith("/image/svg")) {
            ImageHandler.handleImageWithType(baseRequest, response, ImageHandler.ImageType.SVG);
        } else if (method.equals("GET") && uri.startsWith("/image/webp")) {
            ImageHandler.handleImageWithType(baseRequest, response, ImageHandler.ImageType.WEBP);
        } else if (method.equals("GET") && uri.startsWith("/image")) {
            ImageHandler.handleImage(baseRequest, request, response);
        } else if (uri.startsWith("/anything")) {
            AnythingHandler.handle(baseRequest, request, response);
        }

        // disallowed route
        if (!baseRequest.isHandled()) {
            response.sendRedirect("/deny");
        }
    }
}
