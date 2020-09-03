package me.sakigamiyang.httpbin4j.handlers;

import me.sakigamiyang.httpbin4j.Utils;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.server.Request;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class AuthHandler {
    private static final String BASIC_AUTH_PREFIX = "Basic ";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String DIGEST_AUTH_DEFAULT_ALGORITHM = "md5";
    private static final String DIGEST_AUTH_DEFAULT_STALE_AFTER = "never";
    private static final List<String> DIGEST_AUTH_REQUIRE_COOKIE_HANDLING_FLAGS = Arrays.asList("1", "t", "true");
    private static final List<String> DIGEST_AUTH_ALGORITHM_LIST = Arrays.asList("md5", "sha-256", "sha-512");
    private static final List<String> DIGEST_AUTH_QOP_LIST = Arrays.asList("auth", "auth-int");

    public static void handleBasicAuth(Request baseRequest,
                                       HttpServletRequest request,
                                       HttpServletResponse response,
                                       String user,
                                       String passwd) throws IOException {
        try (InputStream is = request.getInputStream();
             OutputStream os = response.getOutputStream()) {
            Common.copyStream(is, Common.NULL_OUTPUT_STREAM);
            basicAuth(request, response, os, user, passwd, HttpServletResponse.SC_UNAUTHORIZED);
            baseRequest.setHandled(true);
        }
    }

    public static void handleHiddenBasicAuth(Request baseRequest,
                                             HttpServletRequest request,
                                             HttpServletResponse response,
                                             String user,
                                             String passwd) throws IOException {
        try (InputStream is = request.getInputStream();
             OutputStream os = response.getOutputStream()) {
            Common.copyStream(is, Common.NULL_OUTPUT_STREAM);
            basicAuth(request, response, os, user, passwd, HttpServletResponse.SC_NOT_FOUND);
            baseRequest.setHandled(true);
        }
    }

    public static void handleBearer(Request baseRequest,
                                    HttpServletRequest request,
                                    HttpServletResponse response) throws IOException {
        try (InputStream is = request.getInputStream();
             OutputStream os = response.getOutputStream()) {
            Common.copyStream(is, Common.NULL_OUTPUT_STREAM);
            bearer(request, response, os);
            baseRequest.setHandled(true);
        }
    }

    public static void handleDigestAuth(Request baseRequest,
                                        HttpServletRequest request,
                                        HttpServletResponse response,
                                        String qop,
                                        String user,
                                        String passwd,
                                        String algorithm,
                                        String staleAfter) throws IOException {
        try (InputStream is = request.getInputStream();
             OutputStream os = response.getOutputStream()) {
            Common.copyStream(is, Common.NULL_OUTPUT_STREAM);
            digestAuth(request, response, os, qop, user, passwd, algorithm, staleAfter);
            baseRequest.setHandled(true);
        }
    }

    private static void basicAuth(HttpServletRequest request,
                                  HttpServletResponse response,
                                  OutputStream os,
                                  String user,
                                  String passwd,
                                  int failureStatus) throws IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BASIC_AUTH_PREFIX)) {
            response.setStatus(failureStatus);
            return;
        }

        byte[] bytes = Base64.decodeBase64(authorization.substring(BASIC_AUTH_PREFIX.length()));
        String[] parts = new String(bytes, StandardCharsets.UTF_8).split(":", 2);
        if (!user.equals(parts[0]) || !passwd.equals(parts[1])) {
            response.setStatus(failureStatus);
            return;
        }

        JSONObject body = new JSONObject();
        body.put("authenticated", true);
        body.put("user", parts[0]);
        Common.respondJSON(response, os, body);
    }

    private static void bearer(HttpServletRequest request,
                               HttpServletResponse response,
                               OutputStream os) throws IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            response.setHeader("WWW-Authenticate", "Bearer");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        JSONObject body = new JSONObject();
        body.put("authenticated", true);
        body.put("token", authorization.substring(BEARER_PREFIX.length()));
        Common.respondJSON(response, os, body);
    }

    private static void digestAuth(HttpServletRequest request,
                                   HttpServletResponse response,
                                   OutputStream os,
                                   String qop,
                                   String user,
                                   String passwd,
                                   String algorithm,
                                   String staleAfter) throws IOException {
        boolean requireCookieHandling = DIGEST_AUTH_REQUIRE_COOKIE_HANDLING_FLAGS.contains(
                Utils.ifNullSetEmpty(request.getParameter("require-cookie")).toLowerCase()
        );
        if (!DIGEST_AUTH_QOP_LIST.contains(qop)) {
            qop = null;
        }
        if (!DIGEST_AUTH_ALGORITHM_LIST.contains(algorithm)) {
            algorithm = DIGEST_AUTH_DEFAULT_ALGORITHM;
        }

        String authorization = request.getHeader("Authorization");

        // TODO: finish this
    }
}
