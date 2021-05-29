package me.sakigamiyang.httpbin4j.controllers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.Helpers;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class StatusCodeController implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        try {
            String[] statusCodes = ctx.pathParam("statusCodes").split(",");
            Integer[] codes = Arrays.stream(statusCodes)
                    .map(Integer::parseInt)
                    .toArray(Integer[]::new);
            HttpUtil.responseData(ctx, Helpers.randomChoice(codes));
            ctx.html("");
        } catch (Throwable t) {
            ctx.redirect("/deny");
        }
    }
}
