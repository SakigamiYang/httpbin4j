package me.sakigamiyang.httpbin4j.handlers;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Common.
 */
public class Common {
    /**
     * Null output stream.
     */
    public static final OutputStream NULL_OUTPUT_STREAM = new OutputStream() {
        @Override
        public void write(int b) {
        }

        @Override
        public void write(byte[] b) {
        }

        @Override
        public void write(byte[] b, int off, int len) {
        }
    };

    /**
     * Read from input stream and write to output stream.
     *
     * @param from input stream
     * @param to   output stream
     * @return byte count of content
     * @throws IOException IO exception
     */
    public static long copyStream(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[4096];
        long total = 0;
        while (true) {
            int length = from.read(buf);
            if (length == -1) {
                break;
            }
            to.write(buf, 0, length);
            total += length;
        }
        return total;
    }

    /**
     * Copy resource.
     *
     * @param response response
     * @param resource resource uri
     * @throws IOException IO exception
     */
    public static void copyResource(HttpServletResponse response, String resource) throws IOException {
        try (InputStream is = Common.class.getResourceAsStream(resource)) {
            long length = copyStream(is, response.getOutputStream());
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
        enumerationAsStream(request.getHeaderNames()).forEach(name -> {
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
        enumerationAsStream(request.getParameterNames()).forEach(name -> {
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

    /**
     * Convert Enumeration to Stream.
     * - Lazy
     * - Don't process any items before the terminal action has been commenced and if the terminal operation is short-circuiting
     * - Iterate only as many items as necessary
     *
     * @param enumeration enumeration
     * @param <T>         type of elements
     * @return stream
     */
    public static <T> Stream<T> enumerationAsStream(Enumeration<T> enumeration) {
        return StreamSupport.stream(
                new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED) {
                    @Override
                    public boolean tryAdvance(Consumer<? super T> action) {
                        if (enumeration.hasMoreElements()) {
                            action.accept(enumeration.nextElement());
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void forEachRemaining(Consumer<? super T> action) {
                        while (enumeration.hasMoreElements()) {
                            action.accept(enumeration.nextElement());
                        }
                    }
                },
                false);
    }
}
