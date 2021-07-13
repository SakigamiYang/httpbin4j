package me.sakigamiyang.httpbin4j.controllers.dynamicdata;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class UUID4Handler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        Map<String, Object> body = new TreeMap<>();
        body.put("uuid", UUID.randomUUID().toString());
        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.json(body);
    }
}
