package me.sakigamiyang.httpbin4j.controllers.responseformats;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.plugin.json.JavalinJackson;
import me.sakigamiyang.httpbin4j.Helpers;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.util.TreeMap;

public class DeflateHandler implements Handler {
    private final JavalinJackson jackson = new JavalinJackson();

    @Override
    public void handle(@NotNull Context ctx) {
        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.header("Content-Encoding", "deflate");
        ctx.contentType("application/json");
        String data = jackson.toJsonString(new TreeMap<String, Object>() {{
            put("origin", ctx.ip());
            put("method", ctx.method());
            put("headers", ctx.headerMap());
            put("deflated", true);
        }});
        ctx.result(Helpers.compress(data, CompressorStreamFactory.DEFLATE));
    }
}
