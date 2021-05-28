package me.sakigamiyang.httpbin4j;

import io.javalin.Javalin;
import me.sakigamiyang.httpbin4j.controllers.*;

import javax.servlet.http.HttpServletResponse;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * App.
 */
public class App {
    public static void main(String[] args) {
        Javalin app = Javalin.create();
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

        // Status codes
        StatusCodesController statusCodesController = new StatusCodesController();
        app.routes(() -> {
            path("/status/:statusCodes", () -> {
                get(statusCodesController);
                post(statusCodesController);
                put(statusCodesController);
                patch(statusCodesController);
                delete(statusCodesController);
            });
        });

        // Request inspection
        app.get("/headers", new RequestInspectionController.HeadersController());
        app.get("/ip", new RequestInspectionController.IpController());
        app.get("/user-agent", new RequestInspectionController.UserAgentController());

        // Response inspection
        app.get("/cache", new ResponseInspectionController.CacheController());
        app.get("/cache/:value", new ResponseInspectionController.CacheValueController());
        app.get("/etag/:etag", new ResponseInspectionController.EtagController());

        // Others
        app.error(HttpServletResponse.SC_NOT_FOUND, ctx -> {
            ctx.redirect("/deny");
        });
    }
}
