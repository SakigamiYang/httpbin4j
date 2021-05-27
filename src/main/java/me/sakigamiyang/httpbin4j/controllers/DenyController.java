package me.sakigamiyang.httpbin4j.controllers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import me.sakigamiyang.httpbin4j.handlers.Common;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

public class DenyController implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        byte[] body = Common.getResource("/deny.html");
        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.html(new String(body, StandardCharsets.UTF_8));
    }
}
