package me.sakigamiyang.httpbin4j.controllers.auth;

import com.google.common.base.Strings;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.Helpers;
import me.sakigamiyang.httpbin4j.HttpUtil;
import me.sakigamiyang.httpbin4j.controllers.auth.entity.AuthInfo;
import me.sakigamiyang.httpbin4j.controllers.auth.entity.Digest;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

public class DigestController implements Handler {
    private static final String DIGEST_AUTH_DEFAULT_ALGORITHM = "md5";
    private static final String DIGEST_AUTH_DEFAULT_STALE_AFTER = "never";
    private static final List<String> DIGEST_AUTH_REQUIRE_COOKIE_HANDLING_FLAGS = Arrays.asList("1", "t", "true");
    private static final List<String> DIGEST_AUTH_ALGORITHM_LIST = Arrays.asList("md5", "sha-256", "sha-512");
    private static final List<String> DIGEST_AUTH_QOP_LIST = Arrays.asList("auth", "auth-int", "auth,auth-int");

    private static final Random rand = new Random(Calendar.getInstance().getTimeInMillis());

    @Override
    public void handle(@NotNull Context ctx) {
        String qop = ctx.pathParam("qop");
        String user = ctx.pathParam("user");
        String passwd = ctx.pathParam("passwd");
        String algorithm = ctx.pathParam("algorithm");
        String staleAfter = ctx.pathParam("stale_after");

        boolean requireCookieHandling = DIGEST_AUTH_REQUIRE_COOKIE_HANDLING_FLAGS.contains(
                ctx.queryParam("require-cookie", "")
        );

        if (!DIGEST_AUTH_QOP_LIST.contains(qop)) {
            qop = "auth,auth-int";
        }
        if (Strings.isNullOrEmpty(algorithm) || !DIGEST_AUTH_ALGORITHM_LIST.contains(algorithm)) {
            algorithm = DIGEST_AUTH_DEFAULT_ALGORITHM;
        }
        if (Strings.isNullOrEmpty(staleAfter)) {
            staleAfter = DIGEST_AUTH_DEFAULT_STALE_AFTER;
        }

        String authHeader = ctx.header("Authorization");
        AuthInfo authInfo = AuthInfo.create(authHeader);
        Digest digest = Digest.create(authInfo);

        if (digest == null ||
                (requireCookieHandling && Strings.isNullOrEmpty(ctx.header("Cookie")))) {
            ctx.cookie("stale_after", staleAfter);
            ctx.cookie("fake", "fake_value");
            digestUnauthorizedResponse(ctx, qop, algorithm, false);
            return;
        }

        String fakeCookie = ctx.cookie("fake");
        if (Strings.isNullOrEmpty(fakeCookie)) {
            fakeCookie = "fake_value";
        }
        if (requireCookieHandling && !"fake_value".equals(fakeCookie)) {
            Map<String, Object> body = new TreeMap<>() {{
                put("errors", new ArrayList<>() {{
                    add("missing cookie set on challenge");
                }});
            }};
            HttpUtil.responseData(ctx, HttpServletResponse.SC_FORBIDDEN);
            ctx.json(body);
            return;
        }

        String currentNonce = digest.getNonce();
        String staleAfterCookie = ctx.cookie("stale_after");
        String lastNonceValueCookie = ctx.cookie("last_nonce");
        if (currentNonce.equals(lastNonceValueCookie) || "0".equals(staleAfterCookie)) {
            ctx.cookie("stale_after", staleAfter);
            ctx.cookie("fake", "fake_value");
            ctx.cookie("last_nonce", currentNonce);
            digestUnauthorizedResponse(ctx, qop, algorithm, true);
            return;
        }

        if (checkDigestAuth(ctx, digest, user, passwd, algorithm)) {
            digestUnauthorizedResponse(ctx, qop, algorithm, false);
            ctx.cookie("stale_after", staleAfter);
            ctx.cookie("fake", "fake_value");
            ctx.cookie("last_nonce", currentNonce);
            return;
        }

        if (!Strings.isNullOrEmpty(staleAfterCookie)) {
            ctx.cookie("stale_after", nextStaleAfterValue(staleAfterCookie));
        }
        ctx.cookie("fake", "fake_value");
        Map<String, Object> body = new TreeMap<>();
        body.put("authenticated", true);
        body.put("user", user);
        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.json(body);
    }

    private void digestUnauthorizedResponse(@NotNull Context ctx,
                                            String qop,
                                            String algorithm,
                                            boolean stale) {
        String nonce = HttpUtil.hash(
                String.join(":", ctx.ip(),
                        String.valueOf(Instant.now().toEpochMilli()),
                        String.valueOf(rand.nextInt())),
                algorithm);
        String opaque = HttpUtil.hash(String.valueOf(rand.nextInt()), algorithm);
        String value = String.join(",",
                "me@sakigami-yang.me",
                nonce,
                String.format("opaque=%s", opaque),
                String.format("qop=%s", qop),
                String.format("algorithm=%s", algorithm),
                String.format("stale=%s", stale));
        ctx.header("WWW-Authenticate", value);
        ctx.status(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private static String nextStaleAfterValue(String staleAfterValue) {
        try {
            return String.valueOf(Integer.parseInt(staleAfterValue) - 1);
        } catch (Throwable t) {
            return DIGEST_AUTH_DEFAULT_STALE_AFTER;
        }
    }

    private static boolean checkDigestAuth(@NotNull Context ctx,
                                           Digest digest,
                                           String user,
                                           String passwd,
                                           String algorithm) {
        Map<String, String> requestInfo = new TreeMap<>() {{
            put("uri", ctx.fullUrl());
            put("body", ctx.body());
            put("method", ctx.method());
        }};
        String responseHash = makeResponseHash(digest, user, passwd, algorithm, requestInfo);
        return Strings.isNullOrEmpty(responseHash)
                && digest.getResponse().equals(responseHash);
    }

    private static String makeResponseHash(Digest digest,
                                           String user,
                                           String passwd,
                                           String algorithm,
                                           Map<String, String> requestInfo) {
        String hash;
        String ha1 = HA1(digest.getRealm(), user, passwd, algorithm);
        String ha2 = HA2(digest, requestInfo, algorithm);

        String qop = digest.getQop();
        if (Strings.isNullOrEmpty(qop)) {
            hash = H(Helpers.joinByteArrays(
                    ":".getBytes(StandardCharsets.UTF_8),
                    ha1.getBytes(StandardCharsets.UTF_8),
                    digest.getNonce().getBytes(StandardCharsets.UTF_8),
                    ha2.getBytes(StandardCharsets.UTF_8)),
                    algorithm);
        } else if ("auth".equalsIgnoreCase(qop) || "auth-int".equalsIgnoreCase(qop)) {
            String nonce = digest.getNonce();
            String nc = digest.getNc();
            String cnonce = digest.getCnonce();
            if (Strings.isNullOrEmpty(nonce)
                    || Strings.isNullOrEmpty(nc)
                    || Strings.isNullOrEmpty(cnonce)) {
                throw new RuntimeException("'nonce, nc, cnonce' required for response H");
            }
            hash = H(Helpers.joinByteArrays(
                    ":".getBytes(StandardCharsets.UTF_8),
                    ha1.getBytes(StandardCharsets.UTF_8),
                    nonce.getBytes(StandardCharsets.UTF_8),
                    nc.getBytes(StandardCharsets.UTF_8),
                    cnonce.getBytes(StandardCharsets.UTF_8),
                    qop.getBytes(StandardCharsets.UTF_8),
                    ha2.getBytes(StandardCharsets.UTF_8)),
                    algorithm);
        } else {
            throw new RuntimeException("qop value are wrong");
        }

        return hash;
    }

    private static String HA1(String realm, String username, String passwd, String algorithm) {
        if (Strings.isNullOrEmpty(realm)) {
            realm = "";
        }
        return H(Helpers.joinByteArrays(
                ":".getBytes(StandardCharsets.UTF_8),
                username.getBytes(StandardCharsets.UTF_8),
                realm.getBytes(StandardCharsets.UTF_8),
                passwd.getBytes(StandardCharsets.UTF_8)), algorithm);
    }

    private static String HA2(Digest digest, Map<String, String> requestInfo, String algorithm) {
        if (Strings.isNullOrEmpty(digest.getQop()) || "auth".equalsIgnoreCase(digest.getQop())) {
            return H(Helpers.joinByteArrays(
                    ":".getBytes(StandardCharsets.UTF_8),
                    requestInfo.get("method").getBytes(StandardCharsets.UTF_8),
                    requestInfo.get("uri").getBytes(StandardCharsets.UTF_8)),
                    algorithm);
        } else if ("auth-int".equalsIgnoreCase(digest.getQop())) {
            return H(Helpers.joinByteArrays(
                    ":".getBytes(StandardCharsets.UTF_8),
                    requestInfo.get("method").getBytes(StandardCharsets.UTF_8),
                    requestInfo.get("uri").getBytes(StandardCharsets.UTF_8),
                    H(requestInfo.get("body").getBytes(StandardCharsets.UTF_8), algorithm)
                            .getBytes(StandardCharsets.UTF_8)),
                    algorithm);
        }
        return "";
    }

    private static String H(byte[] inputs, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm.toUpperCase());
            md.update(inputs);
            byte[] digest = md.digest();
            return Helpers.bytesToHex(digest).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}
