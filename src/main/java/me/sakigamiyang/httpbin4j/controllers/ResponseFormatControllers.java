package me.sakigamiyang.httpbin4j.controllers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.plugin.json.JavalinJson;
import me.sakigamiyang.httpbin4j.Helpers;
import me.sakigamiyang.httpbin4j.HttpUtil;
import me.sakigamiyang.httpbin4j.handlers.Common;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

public class ResponseFormatControllers {
    public static class BrotliController implements Handler {
        @Override
        public void handle(@NotNull Context ctx) {
            HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
            ctx.header("Content-Encoding", "br");
            ctx.contentType("application/json");
            String data = JavalinJson.toJson(new TreeMap<String, Object>() {{
                put("origin", ctx.ip());
                put("method", ctx.method());
                put("headers", ctx.headerMap());
                put("brotli", true);
            }});
            ctx.result(Helpers.compress(data, CompressorStreamFactory.BROTLI));
        }
    }

    public static class DeflateController implements Handler {
        @Override
        public void handle(@NotNull Context ctx) {
            HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
            ctx.header("Content-Encoding", "deflate");
            ctx.contentType("application/json");
            String data = JavalinJson.toJson(new TreeMap<String, Object>() {{
                put("origin", ctx.ip());
                put("method", ctx.method());
                put("headers", ctx.headerMap());
                put("deflated", true);
            }});
            ctx.result(Helpers.compress(data, CompressorStreamFactory.DEFLATE));
        }
    }

    public static class DenyController implements Handler {
        @Override
        public void handle(@NotNull Context ctx) throws Exception {
            byte[] body = Common.getResource("/deny.html");
            HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
            ctx.html(new String(body, StandardCharsets.UTF_8));
        }
    }

    public static class EncodingUTF8Controller implements Handler {
        @Override
        public void handle(@NotNull Context ctx) throws Exception {
            byte[] body = HttpUtil.getResource("/demo.txt");
            HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
            ctx.html(new String(body, StandardCharsets.UTF_8));
        }
    }

    public static class GzipController implements Handler {
        @Override
        public void handle(@NotNull Context ctx) {
            HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
            ctx.header("Content-Encoding", "gzip");
            ctx.contentType("application/json");
            String data = JavalinJson.toJson(new TreeMap<String, Object>() {{
                put("origin", ctx.ip());
                put("method", ctx.method());
                put("headers", ctx.headerMap());
                put("gzipped", true);
            }});
            ctx.result(Helpers.compress(data, CompressorStreamFactory.GZIP));
        }
    }

    public static class HTMLController implements Handler {
        @Override
        public void handle(@NotNull Context ctx) throws Exception {
            byte[] body = HttpUtil.getResource("/demo.html");
            HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
            ctx.html(new String(body, StandardCharsets.UTF_8));
        }
    }

    public static class JsonController implements Handler {
        @Override
        public void handle(@NotNull Context ctx) throws Exception {
            byte[] body = HttpUtil.getResource("/demo.json");
            HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
            ctx.json(new String(body, StandardCharsets.UTF_8));
        }
    }

    public static class RobotsTxtController implements Handler {
        @Override
        public void handle(@NotNull Context ctx) throws Exception {
            byte[] body = HttpUtil.getResource("/robots.txt");
            HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
            ctx.contentType("text/plain");
            ctx.result(new String(body, StandardCharsets.UTF_8));
        }
    }

    public static class XMLController implements Handler {
        @Override
        public void handle(@NotNull Context ctx) throws Exception {
            byte[] body = HttpUtil.getResource("/demo.xml");
            HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
            ctx.contentType("application/xml");
            ctx.result(new String(body, StandardCharsets.UTF_8));
        }
    }
}
