package me.sakigamiyang.httpbin4j.controllers.dynamicdata;

import com.google.common.io.BaseEncoding;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

public class Base64Handler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        String value = ctx.pathParam("value");
        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        try {
            ctx.html(BaseEncoding.base64Url().encode(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Throwable t) {
            ctx.html("Incorrect Base64 data try: SFRUUEJJTiBpcyBhd2Vzb21l");
        }
    }
}
