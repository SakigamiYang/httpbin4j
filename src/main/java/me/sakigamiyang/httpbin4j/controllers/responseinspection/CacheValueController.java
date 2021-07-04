package me.sakigamiyang.httpbin4j.controllers.responseinspection;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.util.TreeMap;

public class CacheValueController implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        String value = ctx.pathParam("value");
        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.header("Cache-Control", String.format("public, max-age=%s", value));
        ctx.json(new TreeMap<String, Object>() {{
            put("url", ctx.fullUrl());
            put("origin", ctx.ip());
            put("headers", ctx.headerMap());
            put("args", ctx.queryParamMap());
        }});
    }
}
