package me.sakigamiyang.httpbin4j.controllers.httpmethods;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.plugin.json.JavalinJson;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.TreeMap;

public class HttpMethodHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        Map<String, Object> body = new TreeMap<>();
        if (ctx.isMultipartFormData()) {
            body.put("data", "");
            body.put("form", ctx.formParamMap());
            body.put("json", null);
        } else {
            body.put("data", ctx.body());
            body.put("form", "");
            try {
                body.put("json", JavalinJson.fromJson(ctx.body(), Object.class));
            } catch (Throwable t) {
                body.put("json", null);
            }
        }
        body.put("url", ctx.fullUrl());
        body.put("origin", ctx.ip());
        body.put("headers", ctx.headerMap());
        body.put("args", ctx.queryParamMap());

        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.json(body);
    }
}
