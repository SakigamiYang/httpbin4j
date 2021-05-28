package me.sakigamiyang.httpbin4j.controllers;

import com.google.common.base.Strings;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import static me.sakigamiyang.httpbin4j.HttpUtil.parseMultiValueHeader;

public class ResponseInspectionController {
    public static class CacheController implements Handler {
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

    public static class CacheValueController implements Handler {
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

    public static class EtagController implements Handler {
        @Override
        public void handle(@NotNull Context ctx) {
            String etag = ctx.pathParam("etag");
            List<String> ifNoneMatch = parseMultiValueHeader(ctx.header("If-None-Match"));
            List<String> ifMatch = parseMultiValueHeader(ctx.header("If-Match"));

            if (!ifNoneMatch.isEmpty() &&
                    (ifNoneMatch.contains(etag) || ifNoneMatch.contains("*"))) {
                ctx.header("ETag", etag);
                HttpUtil.responseData(ctx, HttpServletResponse.SC_NOT_MODIFIED);
                ctx.html("");
            } else if (!ifMatch.isEmpty() &&
                    (!ifNoneMatch.contains(etag) && !ifNoneMatch.contains("*"))) {
                HttpUtil.responseData(ctx, HttpServletResponse.SC_PRECONDITION_FAILED);
                ctx.html("");
            } else {
                HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
                ctx.header("ETag", etag);
                ctx.json(new TreeMap<String, Object>() {{
                    put("url", ctx.fullUrl());
                    put("origin", ctx.ip());
                    put("headers", ctx.headerMap());
                    put("args", ctx.queryParamMap());
                }});
            }
        }
    }
}
