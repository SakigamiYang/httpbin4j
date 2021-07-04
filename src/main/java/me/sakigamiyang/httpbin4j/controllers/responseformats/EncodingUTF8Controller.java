package me.sakigamiyang.httpbin4j.controllers.responseformats;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

public class EncodingUTF8Controller implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        byte[] body = HttpUtil.getResource("/demo.txt");
        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.html(new String(body, StandardCharsets.UTF_8));
    }
}
