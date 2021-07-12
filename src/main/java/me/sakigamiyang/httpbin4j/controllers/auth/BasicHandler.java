package me.sakigamiyang.httpbin4j.controllers.auth;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import me.sakigamiyang.httpbin4j.controllers.auth.entity.AuthInfo;
import me.sakigamiyang.httpbin4j.controllers.auth.entity.Basic;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.TreeMap;

public class BasicHandler implements Handler {
    private final boolean hidden;

    public BasicHandler(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public void handle(@NotNull Context ctx) {
        String user = ctx.pathParam("user");
        String passwd = ctx.pathParam("passwd");

        int unauthorizedStatusCode = hidden ?
                HttpServletResponse.SC_NOT_FOUND :
                HttpServletResponse.SC_UNAUTHORIZED;
        String authHeader = ctx.header("Authorization");
        AuthInfo authInfo = AuthInfo.create(authHeader);
        Basic basic = Basic.create(authInfo);

        if (basic == null ||
                !(basic.getUser().equals(user) && basic.getPasswd().equals(passwd))) {
            ctx.header("WWW-Authenticate", "Basic");
            HttpUtil.responseData(ctx, unauthorizedStatusCode);
            return;
        }

        Map<String, Object> body = new TreeMap<>();
        body.put("authenticated", true);
        body.put("user", basic.getUser());
        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.json(body);
    }
}

