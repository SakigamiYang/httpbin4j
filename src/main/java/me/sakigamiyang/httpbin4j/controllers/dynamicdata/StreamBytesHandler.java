package me.sakigamiyang.httpbin4j.controllers.dynamicdata;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;

public class StreamBytesHandler implements Handler {
    private final Random random = new Random();

    @Override
    public void handle(@NotNull Context ctx) throws IOException {
        int n = Math.min(Integer.parseInt(ctx.pathParam("n")), 102400);

        if (ctx.queryParamMap().containsKey("seed")) {
            int seed = Integer.parseInt(
                    Optional.ofNullable(ctx.queryParam("seed"))
                            .orElse("0"));
            random.setSeed(seed);
        }

        int chunkSize;
        if (ctx.queryParamMap().containsKey("chunk_size")) {
            chunkSize = Integer.parseInt(
                    Optional.ofNullable(ctx.queryParam("chunk_size"))
                            .orElse("1"));
            chunkSize = Math.max(chunkSize, 1);
        } else {
            chunkSize = 10240;
        }

        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.contentType(ContentType.OCTET_STREAM);

        try (ServletOutputStream os = ctx.res.getOutputStream()) {
            byte[] chunks;
            while (n >= chunkSize) {
                chunks = new byte[chunkSize];
                random.nextBytes(chunks);
                os.write(chunks);
                n -= chunkSize;
            }
            if (n >= 0) {
                chunks = new byte[n];
                random.nextBytes(chunks);
                os.write(chunks);
            }
        }
    }
}
