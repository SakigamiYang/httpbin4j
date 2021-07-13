package me.sakigamiyang.httpbin4j.controllers.cookies;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class CookiesHandler implements Handler {
    private static final List<String> env_cookies = Arrays.asList(
            "_gauges_unique",
            "_gauges_unique_year",
            "_gauges_unique_month",
            "_gauges_unique_day",
            "_gauges_unique_hour",
            "__utmz",
            "__utma",
            "__utmb");

    @Override
    public void handle(@NotNull Context ctx) {
        String showEnvParam = ctx.queryParam("show_env");
        boolean hideEnv = showEnvParam == null;

        Map<String, Object> body = new TreeMap<>();
        Map<String, String> cookies = new TreeMap<>();
        Stream<Map.Entry<String, String>> entryStream = ctx.cookieMap().entrySet().stream();
        if (hideEnv) {
            entryStream = entryStream.filter(entry -> !env_cookies.contains(entry.getKey()));
        }
        entryStream.forEach(entry -> cookies.put(entry.getKey(), entry.getValue()));
        body.put("cookies", cookies);
        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.json(body);
    }
}
