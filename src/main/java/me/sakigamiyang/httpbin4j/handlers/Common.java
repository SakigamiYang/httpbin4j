package me.sakigamiyang.httpbin4j.handlers;

import me.sakigamiyang.httpbin4j.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Common.
 */
public class Common {
    /**
     * Copy resource.
     *
     * @param response response
     * @param resource resource uri
     * @throws IOException IO exception
     */
    public static void copyResource(HttpServletResponse response, String resource) throws IOException {
        try (InputStream is = Common.class.getResourceAsStream(resource)) {
            long length = Utils.copyStream(is, response.getOutputStream());
            response.setContentLengthLong(length);
        }
    }

    /**
     * Get URL with query string from request.
     *
     * @param request request
     * @return URL with query string
     */
    public static String getFullURL(HttpServletRequest request) {
        StringBuilder url = new StringBuilder(request.getRequestURL().toString());
        String queryString = request.getQueryString();
        return (queryString == null ?
                url :
                url.append('?').append(queryString)).toString();
    }

    /**
     * Map request headers to JSON.
     *
     * @param request request
     * @return JSONObject
     */
    public static JSONObject mapHeadersToJSON(HttpServletRequest request) {
        JSONObject headers = new JSONObject();
        Utils.enumerationAsStream(request.getHeaderNames()).forEach(name -> {
            List<String> values = Collections.list(request.getHeaders(name));
            if (values.size() == 1) {
                headers.put(name, values.get(0));
            } else {
                headers.put(name, new JSONArray(values));
            }
        });
        return headers;
    }

    /**
     * Map request args to JSON.
     *
     * @param request request
     * @return JSONObject
     */
    public static JSONObject mapArgsToJSON(HttpServletRequest request) {
        JSONObject args = new JSONObject();
        Utils.enumerationAsStream(request.getParameterNames()).forEach(name -> {
            String[] values = request.getParameterValues(name);
            if (values.length == 1) {
                args.put(name, values[0]);
            } else {
                args.put(name, new JSONArray(values));
            }
        });
        return args;
    }

    /**
     * Respond JSON data.
     *
     * @param response   response
     * @param os         output stream
     * @param jsonObject json object (body of response)
     * @throws IOException IO exception
     */
    public static void respondJSON(HttpServletResponse response,
                                   OutputStream os,
                                   JSONObject jsonObject) throws IOException {
        byte[] body = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
        response.setContentLengthLong(body.length);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        os.write(body);
        os.flush();
    }
}
