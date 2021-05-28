package me.sakigamiyang.httpbin4j.controllers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.util.TreeMap;

public class RequestInspectionController {
    public static class HeadersController implements Handler {
        @Override
        public void handle(@NotNull Context ctx) {
            HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
            ctx.json(ctx.headerMap());
        }
    }

    public static class IpController implements Handler {
        @Override
        public void handle(@NotNull Context ctx) {
            HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
            ctx.json(new TreeMap<String, String>() {{
                put("origin", ctx.ip());
            }});
        }
    }

    public static class UserAgentController implements Handler {
        @Override
        public void handle(@NotNull Context ctx) {
            HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
            ctx.json(new TreeMap<String, String>() {{
                put("user-agent", ctx.userAgent());
            }});
        }
    }
}
