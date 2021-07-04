package me.sakigamiyang.httpbin4j.controllers.images;

import com.google.common.base.Strings;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.sakigamiyang.httpbin4j.HttpUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;

public class ImageController implements Handler {
    private enum ImageType {
        JPEG,
        PNG,
        SVG,
        WEBP
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String imageType = ctx.pathParam("image_type");

        if (Strings.isNullOrEmpty(imageType)) {
            String accept = ctx.header("accept");
            if (Strings.isNullOrEmpty(accept)) {
                imageType = "png";
            } else {
                accept = accept.toLowerCase();
                if (accept.contains("image/jpeg")) {
                    imageType = "jpg";
                } else if (accept.contains("image/png")) {
                    imageType = "png";
                } else if (accept.contains("image/svg+xml")) {
                    imageType = "svg";
                } else if (accept.contains("image/webp")) {
                    imageType = "webp";
                }
            }
        }

        if (Strings.isNullOrEmpty(imageType)) {
            HttpUtil.responseData(ctx, HttpServletResponse.SC_NOT_ACCEPTABLE);
            ctx.result("");
        } else {
            switch (imageType) {
                case "jpeg":
                    this.handleImageType(ctx, ImageType.JPEG);
                    break;
                case "png":
                    this.handleImageType(ctx, ImageType.PNG);
                    break;
                case "svg":
                    this.handleImageType(ctx, ImageType.SVG);
                    break;
                case "webp":
                    this.handleImageType(ctx, ImageType.WEBP);
                    break;
                default:
                    ctx.redirect("/deny");
                    break;
            }
        }
    }

    private void handleImageType(@NotNull Context ctx, ImageType imageType) throws Exception {
        byte[] body;
        switch (imageType) {
            case JPEG:
                body = HttpUtil.getResource("/images/jackal.jpg");
                ctx.contentType("image/jpeg");
                break;
            case PNG:
                body = HttpUtil.getResource("/images/pig_icon.png");
                ctx.contentType("image/png");
                break;
            case SVG:
                body = HttpUtil.getResource("/images/svg_logo.svg");
                ctx.contentType("image/svg+xml");
                break;
            case WEBP:
                body = HttpUtil.getResource("/images/wolf_1.webp");
                ctx.contentType("image/webp");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + imageType);
        }
        HttpUtil.responseData(ctx, HttpServletResponse.SC_OK);
        ctx.result(body);
    }
}
