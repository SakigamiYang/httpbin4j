package me.sakigamiyang.httpbin4j.controllers.dynamicdata;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.plugin.json.JavalinJackson;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class StreamHandler implements Handler {
    private final JavalinJackson jackson = new JavalinJackson();

    @Override
    public void handle(@NotNull Context ctx) throws IOException {
        int n = Math.min(Integer.parseInt(ctx.pathParam("n")), 100);

        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.contentType(ContentType.APPLICATION_JSON);

        try (ServletOutputStream os = ctx.res.getOutputStream()) {
            for (int i = 0; i < n; ++i) {
                Map<String, Object> map = new TreeMap<>();
                map.put("url", ctx.url());
                map.put("args", ctx.queryParamMap());
                map.put("headers", ctx.headerMap());
                map.put("origin", ctx.ip());
                map.put("id", i);
                byte[] bytes = (jackson.toJsonString(map) + "\n").getBytes(StandardCharsets.UTF_8);
                os.write(bytes);
            }
        }
    }
}
