package me.sakigamiyang.httpbin4j.controllers.dynamicdata;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.controllers.anything.AnythingHandler;
import org.jetbrains.annotations.NotNull;

public class DelayHandler implements Handler {
    private final AnythingHandler anythingHandler = new AnythingHandler();

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        double delay = Math.min(Double.parseDouble(ctx.pathParam("delay")), 10.0);
        delay *= 1000;  // ms
        int millis = (int) (delay * 1000);
        int nanos = (int) (delay * 1000000) - millis * 1000;
        try {
            Thread.sleep(millis, nanos);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        anythingHandler.handle(ctx);
    }
}
