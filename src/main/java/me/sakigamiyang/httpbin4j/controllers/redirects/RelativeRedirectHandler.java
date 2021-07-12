package me.sakigamiyang.httpbin4j.controllers.redirects;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import me.sakigamiyang.httpbin4j.controllers.httpmethods.HttpMethodHandler;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;

public class RelativeRedirectHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        int n;
        try {
            n = Integer.parseInt(ctx.pathParam("n"));
        } catch (Throwable t) {
            n = 1;
        }
        assert n > 0;

        if (n == 1) {
            ctx.header("Location", "/get");
        } else {
            ctx.header("Location", String.format("/relative-redirect/%s", n - 1));
        }
        HttpUtil.responseData(ctx, HttpServletResponse.SC_FOUND);
        ctx.html("");
    }
}
