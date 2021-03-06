package me.sakigamiyang.httpbin4j.controllers.cookies;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class CookiesSetHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        ctx.redirect("/cookies");
        ctx.queryParamMap().forEach((key, value) -> ctx.cookie(key, String.join(",", value)));
    }
}
