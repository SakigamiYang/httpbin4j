package me.sakigamiyang.httpbin4j;

public class Utils {
    public static String ifNullSetEmpty(String s) { return ifNullSetDefault(s, ""); }

    /**
     * Return string if not null, else specific default value.
     *
     * @param s            string
     * @param defaultValue default value
     * @return string if not null, or else default value
     */
    public static String ifNullSetDefault(String s, String defaultValue) {
        return s == null ? defaultValue : s;
    }
}
