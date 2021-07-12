package me.sakigamiyang.httpbin4j.controllers.auth;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import me.sakigamiyang.httpbin4j.controllers.auth.entity.AuthInfo;
import me.sakigamiyang.httpbin4j.controllers.auth.entity.Bearer;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.TreeMap;

public class BearerHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        String authHeader = ctx.header("Authorization");
        AuthInfo authInfo = AuthInfo.create(authHeader);
        Bearer bearer = Bearer.create(authInfo);

        if (bearer == null) {
            ctx.header("WWW-Authenticate", "Bearer");
            HttpUtil.responseData(ctx, HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Map<String, Object> body = new TreeMap<>();
        body.put("authenticated", true);
        body.put("token", bearer.getToken());
        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.json(body);
    }
}
