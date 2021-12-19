package me.sakigamiyang.httpbin4j.controllers.anything;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UploadedFile;
import io.javalin.plugin.json.JavalinJackson;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnythingHandler implements Handler {
    private final JavalinJackson jackson = new JavalinJackson();

    @Override
    public void handle(@NotNull Context ctx) {
        Map<String, Object> body = new TreeMap<>();

        if (ctx.isMultipartFormData()) {
            body.put("data", "");
            body.put("form", ctx.formParamMap());
            body.put("json", null);
            body.put("files", ctx.uploadedFiles().stream()
                    .collect(Collectors.toMap(UploadedFile::getFilename, Function.identity())));
        } else {
            body.put("data", ctx.body());
            body.put("form", "");
            try {
                body.put("json", jackson.fromJsonString(ctx.body(), Object.class));
            } catch (Throwable t) {
                body.put("json", null);
            }
        }

        body.put("url", ctx.url());
        body.put("method", ctx.method());
        body.put("origin", ctx.ip());
        body.put("headers", ctx.headerMap());
        body.put("args", ctx.queryParamMap());

        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.json(body);
    }
}
