import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * HttpBinHandler.
 */
@Slf4j
public class HttpBinHandler extends AbstractHandler {
    private static final int MAX_DELAY_MS = 10 * 1000;  // delay = 10s

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        log.trace("request: {}", request);
        Utils.enumerationAsStream(request.getHeaderNames()).forEach(
                headerName -> log.trace("header: ({}, {})", headerName, request.getHeader(headerName))
        );
        try (InputStream is = request.getInputStream();
             OutputStream os = response.getOutputStream()) {
            String method = request.getMethod();
            String uri = request.getRequestURI();

            if (uri.equals("/")) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setHeader("Content-Type", "text/html; charset=utf-8");
                copyResource(response, "/index.html");
                baseRequest.setHandled(true);
            }
        } catch (JSONException e) {
            log.trace("JSON exception", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            baseRequest.setHandled(true);
        }
    }

    private void copyResource(HttpServletResponse response, String resource) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resource)) {
            long length = Utils.copyStream(is, response.getOutputStream());
            response.setContentLengthLong(length);
        }
    }
}
