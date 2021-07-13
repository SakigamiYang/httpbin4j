package me.sakigamiyang.httpbin4j.controllers.cookies;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class CookiesSetNameValueHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        ctx.redirect("/cookies");
        ctx.cookie(ctx.pathParam("name"), ctx.pathParam("value"));
    }
}
