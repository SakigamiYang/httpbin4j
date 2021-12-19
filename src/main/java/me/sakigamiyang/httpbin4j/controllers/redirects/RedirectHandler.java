package me.sakigamiyang.httpbin4j.controllers.redirects;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RedirectHandler implements Handler {
    private final AbsoluteRedirectHandler absoluteRedirectHandler = new AbsoluteRedirectHandler();
    private final RelativeRedirectHandler relativeRedirectHandler = new RelativeRedirectHandler();

    @Override
    public void handle(@NotNull Context ctx) {
        boolean absolute = "true".equalsIgnoreCase(
                Optional.ofNullable(ctx.queryParam("absolute")).orElse("false"));
        if (absolute) {
            absoluteRedirectHandler.handle(ctx);
        } else {
            relativeRedirectHandler.handle(ctx);
        }
    }
}
