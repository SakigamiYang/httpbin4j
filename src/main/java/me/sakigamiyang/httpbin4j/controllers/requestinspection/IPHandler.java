package me.sakigamiyang.httpbin4j.controllers.requestinspection;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.util.TreeMap;

public class IPHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.json(new TreeMap<String, Object>() {{
            put("origin", ctx.ip());
        }});
    }
}
