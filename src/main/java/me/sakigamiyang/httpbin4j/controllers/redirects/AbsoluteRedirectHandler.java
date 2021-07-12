package me.sakigamiyang.httpbin4j.controllers.redirects;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.controllers.httpmethods.HttpMethodHandler;
import org.jetbrains.annotations.NotNull;

public class AbsoluteRedirectHandler implements Handler {
    private final HttpMethodHandler httpMethodHandler = new HttpMethodHandler();

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
            httpMethodHandler.handle(ctx);
        } else {
            ctx.redirect(String.format("/absolute-redirect/%s", n - 1));
        }
    }
}
