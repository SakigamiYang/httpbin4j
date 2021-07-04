package me.sakigamiyang.httpbin4j.controllers.auth;

import com.google.common.base.Strings;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import me.sakigamiyang.httpbin4j.controllers.auth.entity.AuthInfo;
import me.sakigamiyang.httpbin4j.controllers.auth.entity.Digest;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class DigestController implements Handler {
    private static final String DIGEST_AUTH_DEFAULT_ALGORITHM = "md5";
    private static final String DIGEST_AUTH_DEFAULT_STALE_AFTER = "never";
    private static final List<String> DIGEST_AUTH_REQUIRE_COOKIE_HANDLING_FLAGS = Arrays.asList("1", "t", "true");
    private static final List<String> DIGEST_AUTH_ALGORITHM_LIST = Arrays.asList("md5", "sha-256", "sha-512");
    private static final List<String> DIGEST_AUTH_QOP_LIST = Arrays.asList("auth", "auth-int");

    private static final Random rand = new Random(Calendar.getInstance().getTimeInMillis());

    @Override
    public void handle(@NotNull Context ctx) {
        String qop = ctx.pathParam("qop");
        String user = ctx.pathParam("user");
        String passwd = ctx.pathParam("passwd");
        String algorithm = ctx.pathParam("algorithm");
        String stale_after = ctx.pathParam("stale_after");

        boolean requireCookieHandling = DIGEST_AUTH_REQUIRE_COOKIE_HANDLING_FLAGS.contains(
                ctx.queryParam("require-cookie", "")
        );

        if (!DIGEST_AUTH_QOP_LIST.contains(qop)) {
            qop = "auth,auth-int";
        }
        if (Strings.isNullOrEmpty(algorithm) || !DIGEST_AUTH_ALGORITHM_LIST.contains(algorithm)) {
            algorithm = DIGEST_AUTH_DEFAULT_ALGORITHM;
        }
        if (Strings.isNullOrEmpty(stale_after)) {
            stale_after = DIGEST_AUTH_DEFAULT_STALE_AFTER;
        }

        String authHeader = ctx.header("Authorization");
        AuthInfo authInfo = AuthInfo.create(authHeader);
        Digest digest = Digest.create(authInfo);

        Map<String, Object> body = new TreeMap<>();
        body.put("authenticated", true);
        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.json(body);
    }
}
