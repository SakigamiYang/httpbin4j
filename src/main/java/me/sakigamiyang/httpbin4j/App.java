package me.sakigamiyang.httpbin4j;

import io.javalin.Javalin;
import io.javalin.core.compression.CompressionStrategy;
import me.sakigamiyang.httpbin4j.controllers.DenyController;
import me.sakigamiyang.httpbin4j.controllers.IndexController;
import me.sakigamiyang.httpbin4j.controllers.anything.AnythingController;
import me.sakigamiyang.httpbin4j.controllers.auth.BasicController;
import me.sakigamiyang.httpbin4j.controllers.auth.BearerController;
import me.sakigamiyang.httpbin4j.controllers.auth.DigestController;
import me.sakigamiyang.httpbin4j.controllers.httpmethods.HttpMethodController;
import me.sakigamiyang.httpbin4j.controllers.images.ImageController;
import me.sakigamiyang.httpbin4j.controllers.requestinspection.HeadersController;
import me.sakigamiyang.httpbin4j.controllers.requestinspection.IPController;
import me.sakigamiyang.httpbin4j.controllers.requestinspection.UserAgentController;
import me.sakigamiyang.httpbin4j.controllers.responseformats.*;
import me.sakigamiyang.httpbin4j.controllers.responseinspection.CacheController;
import me.sakigamiyang.httpbin4j.controllers.responseinspection.CacheValueController;
import me.sakigamiyang.httpbin4j.controllers.responseinspection.EtagController;
import me.sakigamiyang.httpbin4j.controllers.statuscodes.StatusCodeController;

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
        app.get("/", new IndexController());
        app.get("/deny", new DenyController());

        // HTTP methods
        app.get("/get", new HttpMethodController());
        app.post("/post", new HttpMethodController());
        app.put("/put", new HttpMethodController());
        app.patch("/patch", new HttpMethodController());
        app.delete("/delete", new HttpMethodController());

        // Auth
        app.get("/basic-auth/:user/:passwd", new BasicController(false));
        app.get("/hidden-basic-auth/:user/:passwd", new BasicController(true));
        app.get("/bearer", new BearerController());
        app.get("/digest-auth/:qop/:user/:passwd", new DigestController());
        app.get("/digest-auth/:qop/:user/:passwd/:algorithm", new DigestController());
        app.get("/digest-auth/:qop/:user/:passwd/:algorithm/:stale_after", new DigestController());

        // Status codes
        app.routes(() -> path("/status/:statusCodes", () -> {
            get(new StatusCodeController());
            post(new StatusCodeController());
            put(new StatusCodeController());
            patch(new StatusCodeController());
            delete(new StatusCodeController());
        }));

        // Request inspection
        app.get("/headers", new HeadersController());
        app.get("/ip", new IPController());
        app.get("/user-agent", new UserAgentController());

        // Response inspection
        app.get("/cache", new CacheController());
        app.get("/cache/:value", new CacheValueController());
        app.get("/etag/:etag", new EtagController());

        // Response formats
        app.get("/brotli", new BrotliController());
        app.get("/deflate", new DeflateController());
        app.get("/encoding/utf8", new EncodingUTF8Controller());
        app.get("/gzip", new GzipController());
        app.get("/html", new HTMLController());
        app.get("/json", new JsonController());
        app.get("/robots.txt", new RobotsTxtController());
        app.get("/xml", new XMLController());

        // Images
        app.get("/image/:image_format", new ImageController());

        // Anything
        app.routes(() -> path("/anything/:anything", () -> {
            get(new AnythingController());
            post(new AnythingController());
            put(new AnythingController());
            patch(new AnythingController());
            delete(new AnythingController());
        }));

        // Others
        app.error(HttpServletResponse.SC_NOT_FOUND, ctx -> ctx.redirect("/deny"));
    }
}
