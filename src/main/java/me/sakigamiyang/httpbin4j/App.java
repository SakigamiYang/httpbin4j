package me.sakigamiyang.httpbin4j;

import io.javalin.Javalin;
import io.javalin.core.compression.Brotli;
import io.javalin.core.compression.Gzip;
import me.sakigamiyang.httpbin4j.controllers.*;

import javax.servlet.http.HttpServletResponse;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * App.
 */
public class App {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> config.compressionStrategy(new Brotli(4), new Gzip(6)));
        makingControllers(app);
        app.start();
    }

    private static void makingControllers(Javalin app) {
        app.get("/", new IndexController());

        // HTTP methods
        app.get("/get", new HttpMethodController());
        app.post("/post", new HttpMethodController());
        app.put("/put", new HttpMethodController());
        app.patch("/patch", new HttpMethodController());
        app.delete("/delete", new HttpMethodController());

        // Status codes
        StatusCodeController statusCodesController = new StatusCodeController();
        app.routes(() -> path("/status/:statusCodes", () -> {
            get(statusCodesController);
            post(statusCodesController);
            put(statusCodesController);
            patch(statusCodesController);
            delete(statusCodesController);
        }));

        // Request inspection
        app.get("/headers", new RequestInspectionControllers.HeadersController());
        app.get("/ip", new RequestInspectionControllers.IpController());
        app.get("/user-agent", new RequestInspectionControllers.UserAgentController());

        // Response inspection
        app.get("/cache", new ResponseInspectionControllers.CacheController());
        app.get("/cache/:value", new ResponseInspectionControllers.CacheValueController());
        app.get("/etag/:etag", new ResponseInspectionControllers.EtagController());

        // Response formats
        app.get("/brotli", new ResponseFormatControllers.BrotliController());
        app.get("/deflate", new ResponseFormatControllers.DeflateController());
        app.get("/deny", new ResponseFormatControllers.DenyController());
        app.get("/encoding/utf8", new ResponseFormatControllers.EncodingUTF8Controller());
        app.get("/gzip", new ResponseFormatControllers.GzipController());
        app.get("/html", new ResponseFormatControllers.HTMLController());
        app.get("/json", new ResponseFormatControllers.JsonController());
        app.get("/robots.txt", new ResponseFormatControllers.RobotsTxtController());
        app.get("/xml", new ResponseFormatControllers.XMLController());

        // Images
        app.get("/image/:image_format", new ImageController());

        // Others
        app.error(HttpServletResponse.SC_NOT_FOUND, ctx -> ctx.redirect("/deny"));
    }
}
