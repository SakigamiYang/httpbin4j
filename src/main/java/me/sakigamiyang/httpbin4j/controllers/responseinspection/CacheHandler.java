package me.sakigamiyang.httpbin4j.controllers.responseinspection;

import com.google.common.base.Strings;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.TreeMap;
import java.util.UUID;

public class CacheHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        String ifModifiedSince = ctx.header("If-Modified-Since");
        String ifNoneMatch = ctx.header("If-None-Match");
        boolean unchanged = Strings.isNullOrEmpty(ifModifiedSince) && Strings.isNullOrEmpty(ifNoneMatch);
        if (unchanged) {
            HttpUtil.responseData(ctx, HttpServletResponse.SC_NOT_MODIFIED);
            ctx.html("");
        } else {
            HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
            ctx.header("Last-Modified", String.valueOf(new Date().getTime()))
                    .header("ETag", UUID.randomUUID().toString().replace("-", ""));
            ctx.json(new TreeMap<String, Object>() {{
                put("url", ctx.fullUrl());
                put("origin", ctx.ip());
                put("headers", ctx.headerMap());
                put("args", ctx.queryParamMap());
            }});
        }
    }
}
