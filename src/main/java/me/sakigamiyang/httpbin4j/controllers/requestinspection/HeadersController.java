package me.sakigamiyang.httpbin4j.controllers.requestinspection;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;

public class HeadersController implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.json(ctx.headerMap());
    }
}
