package me.sakigamiyang.httpbin4j.handlers;

import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

public class ImageHandler {
    public enum ImageType {
        NONE,
        JPEG,
        PNG,
        SVG,
        WEBP
    }

    public static void handleImage(Request baseRequest,
                                   HttpServletRequest request,
                                   HttpServletResponse response) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            List<String> headers = Common.enumerationToList(request.getHeaderNames())
                    .stream().map(String::toLowerCase).collect(Collectors.toList());
            if (!headers.contains("accept")) {
                byte[] body = Common.getResource("/images/pig_icon.png");
                Common.responseData(response, os, body, HttpServletResponse.SC_OK, "image/png");
            } else {
                List<String> accept = Common.enumerationToList(request.getHeaders("accept"))
                        .stream().map(String::toLowerCase).collect(Collectors.toList());

                if (accept.contains("image/jpeg")) {
                    handleImageWithType(baseRequest, response, ImageType.JPEG);
                } else if (accept.contains("image/png")) {
                    handleImageWithType(baseRequest, response, ImageType.PNG);
                } else if (accept.contains("image/svg+xml")) {
                    handleImageWithType(baseRequest, response, ImageType.SVG);
                } else if (accept.contains("image/webp")) {
                    handleImageWithType(baseRequest, response, ImageType.WEBP);
                } else {
                    handleImageWithType(baseRequest, response, ImageType.NONE);
                }
            }
            baseRequest.setHandled(true);
        }
    }

    public static void handleImageWithType(Request baseRequest,
                                           HttpServletResponse response,
                                           ImageType imageType) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            if (imageType == ImageType.JPEG) {
                byte[] body = Common.getResource("/images/jackal.jpg");
                Common.responseData(response, os, body, HttpServletResponse.SC_OK, "image/jpeg");
            } else if (imageType == ImageType.PNG) {
                byte[] body = Common.getResource("/images/pig_icon.png");
                Common.responseData(response, os, body, HttpServletResponse.SC_OK, "image/png");
            } else if (imageType == ImageType.SVG) {
                byte[] body = Common.getResource("/images/svg_logo.svg");
                Common.responseData(response, os, body, HttpServletResponse.SC_OK, "image/svg+xml");
            } else if (imageType == ImageType.WEBP) {
                byte[] body = Common.getResource("/images/wolf_1.webp");
                Common.responseData(response, os, body, HttpServletResponse.SC_OK, "image/webp");
            } else {
                Common.respondHTML(response, os, new byte[0], HttpServletResponse.SC_NOT_ACCEPTABLE);
            }
            baseRequest.setHandled(true);
        }
    }
}
