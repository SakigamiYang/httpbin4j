package me.sakigamiyang.httpbin4j.controllers.responseinspection;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.TreeMap;

import static me.sakigamiyang.httpbin4j.HttpUtil.parseMultiValueHeader;

public class ETagHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        String eTag = ctx.pathParam("etag");
        List<String> ifNoneMatch = parseMultiValueHeader(ctx.header("If-None-Match"));
        List<String> ifMatch = parseMultiValueHeader(ctx.header("If-Match"));

        if (!ifNoneMatch.isEmpty() &&
                (ifNoneMatch.contains(eTag) || ifNoneMatch.contains("*"))) {
            ctx.header("ETag", eTag);
            HttpUtil.responseData(ctx, HttpServletResponse.SC_NOT_MODIFIED);
            ctx.html("");
        } else if (!ifMatch.isEmpty() &&
                (!ifNoneMatch.contains(eTag) && !ifNoneMatch.contains("*"))) {
            HttpUtil.responseData(ctx, HttpServletResponse.SC_PRECONDITION_FAILED);
            ctx.html("");
        } else {
            HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
            ctx.header("ETag", eTag);
            ctx.json(new TreeMap<String, Object>() {{
                put("url", ctx.url());
                put("origin", ctx.ip());
                put("headers", ctx.headerMap());
                put("args", ctx.queryParamMap());
            }});
        }
    }
}
