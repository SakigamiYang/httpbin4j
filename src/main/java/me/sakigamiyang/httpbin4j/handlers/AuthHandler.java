package me.sakigamiyang.httpbin4j.handlers;

import me.sakigamiyang.httpbin4j.Utils;
import me.sakigamiyang.httpbin4j.handlers.entity.auth.Auth;
import me.sakigamiyang.httpbin4j.handlers.entity.auth.BasicAuth;
import me.sakigamiyang.httpbin4j.handlers.entity.auth.BearerAuth;
import me.sakigamiyang.httpbin4j.handlers.entity.auth.DigestAuth;
import org.eclipse.jetty.server.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class AuthHandler {
    private static final String DIGEST_AUTH_DEFAULT_ALGORITHM = "md5";
    private static final String DIGEST_AUTH_DEFAULT_STALE_AFTER = "never";
    private static final List<String> DIGEST_AUTH_REQUIRE_COOKIE_HANDLING_FLAGS = Arrays.asList("1", "t", "true");
    private static final List<String> DIGEST_AUTH_ALGORITHM_LIST = Arrays.asList("md5", "sha-256", "sha-512");
    private static final List<String> DIGEST_AUTH_QOP_LIST = Arrays.asList("auth", "auth-int");
    private static final String DIGEST_AUTH_INFO_KEY_USERNAME = "username";
    private static final String DIGEST_AUTH_INFO_KEY_REALM = "realm";
    private static final String DIGEST_AUTH_INFO_KEY_NONCE = "nonce";
    private static final String DIGEST_AUTH_INFO_KEY_URI = "uri";
    private static final String DIGEST_AUTH_INFO_KEY_RESPONSE = "response";
    private static final String DIGEST_AUTH_INFO_KEY_QOP = "qop";
    private static final String DIGEST_AUTH_INFO_KEY_NC = "nc";
    private static final String DIGEST_AUTH_INFO_KEY_CNONCE = "cnonce";
    private static final List<String> DIGEST_AUTH_INFO_MUST_CONTAIN_LIST =
            Arrays.asList(DIGEST_AUTH_INFO_KEY_USERNAME,
                    DIGEST_AUTH_INFO_KEY_REALM,
                    DIGEST_AUTH_INFO_KEY_NONCE,
                    DIGEST_AUTH_INFO_KEY_URI,
                    DIGEST_AUTH_INFO_KEY_RESPONSE);

    private static final Random rand = new Random(19880124);

    public static void handleBasicAuth(Request baseRequest,
                                       HttpServletRequest request,
                                       HttpServletResponse response,
                                       String user,
                                       String passwd) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            basicAuth(request, response, os, user, passwd, HttpServletResponse.SC_UNAUTHORIZED);
            baseRequest.setHandled(true);
        }
    }

    public static void handleHiddenBasicAuth(Request baseRequest,
                                             HttpServletRequest request,
                                             HttpServletResponse response,
                                             String user,
                                             String passwd) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            basicAuth(request, response, os, user, passwd, HttpServletResponse.SC_NOT_FOUND);
            baseRequest.setHandled(true);
        }
    }

    public static void handleBearer(Request baseRequest,
                                    HttpServletRequest request,
                                    HttpServletResponse response) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
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
        Auth auth = parseAuthorizationHeader(request.getHeader("Authorization"));
        if (!(auth instanceof BasicAuth)) {
            response.setHeader("WWW-Authenticate", "Basic");
            response.setStatus(failureStatus);
            return;
        }

        BasicAuth basicAuth = (BasicAuth) auth;
        if (!user.equals(basicAuth.getUsername()) || !passwd.equals(basicAuth.getPassword())) {
            response.setHeader("WWW-Authenticate", "Basic");
            response.setStatus(failureStatus);
            return;
        }

        JSONObject body = new JSONObject();
        body.put("authenticated", true);
        body.put("user", user);
        Common.respondJSON(response, os, body, HttpServletResponse.SC_OK);
    }

    private static void bearer(HttpServletRequest request,
                               HttpServletResponse response,
                               OutputStream os) throws IOException {
        Auth auth = parseAuthorizationHeader(request.getHeader("Authorization"));
        if (!(auth instanceof BearerAuth)) {
            response.setHeader("WWW-Authenticate", "Bearer");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        BearerAuth bearerAuth = (BearerAuth) auth;
        JSONObject body = new JSONObject();
        body.put("authenticated", true);
        body.put("token", bearerAuth.getToken());
        Common.respondJSON(response, os, body, HttpServletResponse.SC_OK);
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

        Auth auth = parseAuthorizationHeader(request.getHeader("Authorization"));
        if (!(auth instanceof DigestAuth) ||
                (requireCookieHandling && request.getHeader("Cookie") == null)) {
            digestUnauthorizedResponse(request, response, qop, algorithm, false);
            response.addCookie(new Cookie("stale_after", staleAfter));
            response.addCookie(new Cookie("fake", "fake_value"));
            return;
        }

        DigestAuth digestAuth = (DigestAuth) auth;

        Cookie[] cookies = request.getCookies();
        if (cookies == null) cookies = new Cookie[0];
        List<Cookie> cookieList = Arrays.asList(cookies);

        Cookie fake = cookieList.parallelStream()
                .filter(cookie -> "fake".equalsIgnoreCase(cookie.getName()))
                .findFirst()
                .orElse(new Cookie("fake", "fake_value"));
        if (requireCookieHandling && !"fake_value".equals(fake.getValue())) {
            JSONObject body = new JSONObject();
            body.put("errors", new JSONArray().put("missing cookie set on challenge"));
            Common.respondJSON(response, os, body, HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String currentNonce = digestAuth.getNonce();
        String staleAfterValue = cookieList.parallelStream()
                .filter(cookie -> "stale_after".equalsIgnoreCase(cookie.getName()))
                .findFirst()
                .orElse(new Cookie("stale_after", null))
                .getValue();

        String lastNonceValue = cookieList.parallelStream()
                .filter(cookie -> "last_nonce".equalsIgnoreCase(cookie.getName()))
                .findFirst()
                .orElse(new Cookie("last_nonce", null))
                .getValue();
        if (currentNonce.equals(lastNonceValue) || "0".equals(staleAfterValue)) {
            digestUnauthorizedResponse(request, response, qop, algorithm, false);
            response.addCookie(new Cookie("stale_after", staleAfter));
            response.addCookie(new Cookie("fake", "fake_value"));
            response.addCookie(new Cookie("last_nonce", currentNonce));
            return;
        }
        // TODO:
        // not check_digest_auth(user, passwd)


        JSONObject body = new JSONObject();
        body.put("authenticated", true);
        body.put("user", user);
        Common.respondJSON(response, os, body, HttpServletResponse.SC_OK);
        response.addCookie(new Cookie("fake", "fake_value"));
        if (staleAfterValue != null) {
            response.addCookie(new Cookie("stale_after", nextStaleAfterValue(staleAfterValue)));
        }
    }

    private static Auth parseAuthorizationHeader(String value) {
        if (value == null) {
            return null;
        }

        String authType, authInfo;
        try {
            String[] tempArray = value.split(" ", 2);
            authType = tempArray[0].toLowerCase().trim();
            authInfo = tempArray[1].trim();
        } catch (Throwable t) {
            return null;
        }

        switch (authType) {
            case BasicAuth.TYPE:
                try {
                    byte[] bytes = Base64.getDecoder().decode(authInfo);
                    String[] tempArray = new String(bytes, StandardCharsets.UTF_8).split(":", 2);
                    return new BasicAuth(tempArray[0], tempArray[1]);
                } catch (Throwable t) {
                    // do nothing
                }
                break;
            case BearerAuth.TYPE:
                try {
                    return new BearerAuth(authInfo);
                } catch (Throwable t) {
                    // do nothing
                }
                break;
            case DigestAuth.TYPE:
                try {
                    Map<String, String> headerDict = Common.parseDictHeader(authInfo);
                    for (String key : DIGEST_AUTH_INFO_MUST_CONTAIN_LIST) {
                        if (!headerDict.containsKey(key)) {
                            return null;
                        }
                    }
                    if (headerDict.containsKey(DIGEST_AUTH_INFO_KEY_QOP)) {
                        if (headerDict.getOrDefault(DIGEST_AUTH_INFO_KEY_NC, "").isEmpty() ||
                                headerDict.getOrDefault(DIGEST_AUTH_INFO_KEY_CNONCE, "").isEmpty()) {
                            return null;
                        }
                    }

                    String username = headerDict.remove(DIGEST_AUTH_INFO_KEY_USERNAME);
                    String realm = headerDict.remove(DIGEST_AUTH_INFO_KEY_REALM);
                    String nonce = headerDict.remove(DIGEST_AUTH_INFO_KEY_NONCE);
                    String uri = headerDict.remove(DIGEST_AUTH_INFO_KEY_URI);
                    String response = headerDict.remove(DIGEST_AUTH_INFO_KEY_RESPONSE);
                    String qop = headerDict.remove(DIGEST_AUTH_INFO_KEY_QOP);
                    String nc = headerDict.remove(DIGEST_AUTH_INFO_KEY_NC);
                    String cnonce = headerDict.remove(DIGEST_AUTH_INFO_KEY_CNONCE);
                    return new DigestAuth(
                            username,
                            realm,
                            nonce,
                            uri,
                            response,
                            qop,
                            nc,
                            cnonce,
                            headerDict
                    );
                } catch (Throwable t) {
                    // do nothing
                }
                break;
        }
        return null;
    }

    private static void digestUnauthorizedResponse(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   String qop,
                                                   String algorithm,
                                                   boolean stale) {
        String nonce = Common.hash(
                String.join(":", request.getRemoteAddr(),
                        String.valueOf(Instant.now().toEpochMilli()),
                        String.valueOf(rand.nextInt())),
                algorithm);
        String opaque = Common.hash(String.valueOf(rand.nextInt()), algorithm);
        String value = String.join(",",
                "me@sakigami-yang.me",
                nonce,
                String.format("opaque=%s", opaque),
                String.format("qop=%s", qop == null ? "\"auth,auth-int\"" : qop),
                String.format("algorithm=%s", algorithm),
                String.format("stale=%s", stale));

        response.setHeader("WWW-Authenticate", value);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private static String nextStaleAfterValue(String staleAfterValue) {
        try {
            return String.valueOf(Integer.parseInt(staleAfterValue) - 1);
        } catch (Throwable t) {
            return DIGEST_AUTH_DEFAULT_STALE_AFTER;
        }
    }
}
