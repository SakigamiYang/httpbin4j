package me.sakigamiyang.httpbin4j.controllers.dynamicdata;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.util.Random;

public class BytesHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        int n = Math.min(Integer.parseInt(ctx.pathParam("n")), 102400);
        String seedParam = ctx.queryParam("seed");
        Random random;
        if (seedParam != null) {
            long seed = Long.parseLong(seedParam);
            random = new Random(seed);
        } else {
            random = new Random();
        }

        byte[] body = new byte[n];
        random.nextBytes(body);
        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.contentType("application/octet-stream");
        ctx.result(body);
    }
}
