package me.sakigamiyang.httpbin4j.controllers.responseformats;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

public class JsonController implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        byte[] body = HttpUtil.getResource("/demo.json");
        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.json(new String(body, StandardCharsets.UTF_8));
    }
}
