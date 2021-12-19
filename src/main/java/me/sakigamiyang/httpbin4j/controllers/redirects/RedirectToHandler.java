package me.sakigamiyang.httpbin4j.controllers.redirects;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

public class RedirectToHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        int statusCode = HttpServletResponse.SC_FOUND;
        try {
            String statusCodeParam = ctx.queryParam("status_code");
            if (statusCodeParam != null) {
                int statusCodeTemp = Integer.parseInt(statusCodeParam);
                if (statusCodeTemp >= 300 && statusCodeTemp < 400) {
                    statusCode = statusCodeTemp;
                }
            }
        } catch (Throwable t) {
            // do nothing
        }
        ctx.header("Location", Optional.ofNullable(ctx.queryParam("url")).orElse(""));
        HttpUtil.responseData(ctx, statusCode);
        ctx.html("");
    }
}
