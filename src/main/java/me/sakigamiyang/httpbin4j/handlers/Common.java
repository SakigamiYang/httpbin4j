package me.sakigamiyang.httpbin4j.handlers;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
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
        public void write(@Nonnull byte[] b) {
        }

        @Override
        public void write(@Nonnull byte[] b, int off, int len) {
        }
    };

    /**
     * Read from input stream.
     *
     * @param from input stream
     * @return content
     * @throws IOException IO exception
     */
    public static String readStream(InputStream from) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(from))) {
            char[] charBuffer = new char[128];
            int bytesRead;
            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                sb.append(charBuffer, 0, bytesRead);
            }
        }
        return sb.toString();
    }

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
     * Get client IP address
     *
     * @param request request
     * @return client IP address
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (Strings.isNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (Strings.isNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (Strings.isNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (Strings.isNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (Strings.isNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
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
     * Respond HTML data.
     *
     * @param response response
     * @param os       output stream
     * @param body     body of response
     * @param status   status
     * @throws IOException IO exception
     */
    public static void respondHTML(HttpServletResponse response,
                                   OutputStream os,
                                   byte[] body,
                                   int status) throws IOException {
        response.setContentLengthLong(body.length);
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");
        response.setDateHeader("Date", new Date().getTime());
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setStatus(status);
        os.write(body);
        os.flush();
    }

    /**
     * Respond JSON data.
     *
     * @param response   response
     * @param os         output stream
     * @param jsonObject json object (body of response)
     * @param status     status
     * @throws IOException IO exception
     */
    public static void respondJSON(HttpServletResponse response,
                                   OutputStream os,
                                   JSONObject jsonObject,
                                   int status) throws IOException {
        byte[] body = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
        response.setContentLengthLong(body.length);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setDateHeader("Date", (new Date()).getTime());
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setStatus(status);
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

    /**
     * Parse header with like "k1=v1,k2=v2,...".
     *
     * @param value header value
     * @return k-v map
     */
    public static Map<String, String> parseDictHeader(String value) {
        Map<String, String> result = new HashMap<>();

        if (value != null) {
            for (String item : value.split(",")) {
                if (!item.contains("=")) {
                    result.put(item, null);
                }
                String[] tempArray = item.split("=", 2);
                result.put(tempArray[0].trim(), tempArray[1].replaceAll("\"", "").trim());
            }
        }

        return result;
    }

    /**
     * hash functions
     *
     * @param data      data
     * @param algorithm algorithm
     * @return hash result string
     */
    @SuppressWarnings({"deprecation", "UnstableApiUsage"})
    public static String hash(String data, String algorithm) {
        switch (algorithm) {
            case "sha-256":
                return Hashing.sha256().hashBytes(data.getBytes()).toString();
            case "sha-512":
                return Hashing.sha512().hashBytes(data.getBytes()).toString();
            default:
                return Hashing.md5().hashBytes(data.getBytes()).toString();
        }
    }
}
