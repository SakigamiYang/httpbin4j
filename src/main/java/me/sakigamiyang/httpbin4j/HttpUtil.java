package me.sakigamiyang.httpbin4j;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import io.javalin.http.Context;
import me.sakigamiyang.httpbin4j.handlers.Common;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtil {
    private static final Pattern VALID_HEADER_VALUE = Pattern.compile("\\s*(W/)?\"?([^\"]*)\"?\\s*");

    /**
     * Load a classpath resource to an array of byte.
     *
     * @param resource resource uri
     * @return array of byte
     * @throws IOException IO exception
     */
    public static byte[] getResource(String resource) throws IOException {
        try (InputStream is = Common.class.getResourceAsStream(resource)) {
            assert is != null;
            return ByteStreams.toByteArray(is);
        }
    }

    /**
     * Make response
     *
     * @param ctx    context for http-request handler
     * @param status http status
     */
    public static void responseData(@NotNull Context ctx,
                                    int status) {
        ctx.status(status);
        ctx.header("Accept-Charset", "utf-8");
        ctx.header("Date", new Date().toString());
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Credentials", "true");
    }

    /**
     * Parse multi-value header
     *
     * @param headerValue header value, separated by half-width comma
     * @return list of items in header value
     */
    public static List<String> parseMultiValueHeader(String headerValue) {
        List<String> parsedParts = new ArrayList<>();
        if (!Strings.isNullOrEmpty(headerValue)) {
            for (String part : headerValue.split(",")) {
                Matcher matcher = VALID_HEADER_VALUE.matcher(part);
                if (matcher.find()) {
                    String value = matcher.group(2);
                    if (!Strings.isNullOrEmpty(value)) {
                        parsedParts.add(value);
                    }
                }
            }
        }
        return parsedParts;
    }

    /**
     * Parse header with like "k1=v1,k2=v2,...".
     *
     * @param value header value
     * @return k-v map
     */
    public static Map<String, String> parseDictHeader(String value) {
        Map<String, String> result = new TreeMap<>();

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
}
