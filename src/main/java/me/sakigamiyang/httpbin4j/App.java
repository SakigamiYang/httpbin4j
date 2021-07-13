package me.sakigamiyang.httpbin4j;

import io.javalin.Javalin;
import io.javalin.core.compression.CompressionStrategy;
import me.sakigamiyang.httpbin4j.controllers.DenyHandler;
import me.sakigamiyang.httpbin4j.controllers.IndexHandler;
import me.sakigamiyang.httpbin4j.controllers.anything.AnythingHandler;
import me.sakigamiyang.httpbin4j.controllers.auth.BasicHandler;
import me.sakigamiyang.httpbin4j.controllers.auth.BearerHandler;
import me.sakigamiyang.httpbin4j.controllers.auth.DigestHandler;
import me.sakigamiyang.httpbin4j.controllers.cookies.CookiesDeleteHandler;
import me.sakigamiyang.httpbin4j.controllers.cookies.CookiesHandler;
import me.sakigamiyang.httpbin4j.controllers.cookies.CookiesSetHandler;
import me.sakigamiyang.httpbin4j.controllers.cookies.CookiesSetNameValueHandler;
import me.sakigamiyang.httpbin4j.controllers.httpmethods.HttpMethodHandler;
import me.sakigamiyang.httpbin4j.controllers.images.ImageHandler;
import me.sakigamiyang.httpbin4j.controllers.redirects.AbsoluteRedirectHandler;
import me.sakigamiyang.httpbin4j.controllers.redirects.RedirectHandler;
import me.sakigamiyang.httpbin4j.controllers.redirects.RedirectToHandler;
import me.sakigamiyang.httpbin4j.controllers.redirects.RelativeRedirectHandler;
import me.sakigamiyang.httpbin4j.controllers.requestinspection.HeadersHandler;
import me.sakigamiyang.httpbin4j.controllers.requestinspection.IPHandler;
import me.sakigamiyang.httpbin4j.controllers.requestinspection.UserAgentHandler;
import me.sakigamiyang.httpbin4j.controllers.responseformats.*;
import me.sakigamiyang.httpbin4j.controllers.responseinspection.CacheHandler;
import me.sakigamiyang.httpbin4j.controllers.responseinspection.CacheValueHandler;
import me.sakigamiyang.httpbin4j.controllers.responseinspection.ETagHandler;
import me.sakigamiyang.httpbin4j.controllers.statuscodes.StatusCodeHandler;

import javax.servlet.http.HttpServletResponse;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * App.
 */
public class App {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> config.compressionStrategy(CompressionStrategy.NONE));
        makingControllers(app);
        app.start();
    }

    private static void makingControllers(Javalin app) {
        app.get("/", new IndexHandler());
        app.get("/deny", new DenyHandler());

        // HTTP methods
        app.get("/get", new HttpMethodHandler());
        app.post("/post", new HttpMethodHandler());
        app.put("/put", new HttpMethodHandler());
        app.patch("/patch", new HttpMethodHandler());
        app.delete("/delete", new HttpMethodHandler());

        // Auth
        app.get("/basic-auth/:user/:passwd", new BasicHandler(false));
        app.get("/hidden-basic-auth/:user/:passwd", new BasicHandler(true));
        app.get("/bearer", new BearerHandler());
        app.get("/digest-auth/:qop/:user/:passwd", new DigestHandler());
        app.get("/digest-auth/:qop/:user/:passwd/:algorithm", new DigestHandler());
        app.get("/digest-auth/:qop/:user/:passwd/:algorithm/:stale_after", new DigestHandler());

        // Status codes
        app.routes(() -> path("/status/:statusCodes", () -> {
            get(new StatusCodeHandler());
            post(new StatusCodeHandler());
            put(new StatusCodeHandler());
            patch(new StatusCodeHandler());
            delete(new StatusCodeHandler());
        }));

        // Request inspection
        app.get("/headers", new HeadersHandler());
        app.get("/ip", new IPHandler());
        app.get("/user-agent", new UserAgentHandler());

        // Response inspection
        app.get("/cache", new CacheHandler());
        app.get("/cache/:value", new CacheValueHandler());
        app.get("/etag/:etag", new ETagHandler());

        // Response formats
        app.get("/brotli", new BrotliHandler());
        app.get("/deflate", new DeflateHandler());
        app.get("/encoding/utf8", new EncodingUTF8Handler());
        app.get("/gzip", new GzipHandler());
        app.get("/html", new HTMLHandler());
        app.get("/json", new JsonHandler());
        app.get("/robots.txt", new RobotsTxtHandler());
        app.get("/xml", new XMLHandler());

        // Cookies
        app.get("/cookies", new CookiesHandler());
        app.get("/cookies/set", new CookiesSetHandler());
        app.get("/cookies/set/:name/:value", new CookiesSetNameValueHandler());
        app.get("/cookies/delete", new CookiesDeleteHandler());

        // Images
        app.get("/image/:image_format", new ImageHandler());

        // Redirects
        app.get("/absolute-redirect/:n", new AbsoluteRedirectHandler());
        app.get("/relative-redirect/:n", new RelativeRedirectHandler());
        app.routes(() -> path("/redirect-to", () -> {
            get(new RedirectToHandler());
            post(new RedirectToHandler());
            put(new RedirectToHandler());
            patch(new RedirectToHandler());
            delete(new RedirectToHandler());
        }));
        app.get("/redirect/:n", new RedirectHandler());

        // Anything
        app.routes(() -> path("/anything/:anything", () -> {
            get(new AnythingHandler());
            post(new AnythingHandler());
            put(new AnythingHandler());
            patch(new AnythingHandler());
            delete(new AnythingHandler());
        }));

        // Others
        app.error(HttpServletResponse.SC_NOT_FOUND, ctx -> ctx.redirect("/deny"));
    }
}
