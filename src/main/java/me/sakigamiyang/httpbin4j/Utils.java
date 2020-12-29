package me.sakigamiyang.httpbin4j;

import java.util.Date;
import java.util.Random;

public class Utils {
    /**
     * Return string if not null, else empty string.
     *
     * @param s string
     * @return string if not null, or else empty string
     */
    public static String ifNullSetEmpty(String s) {
        return ifNullSetDefault(s, "");
    }

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

    /**
     * Choice an element from an array randomly.
     *
     * @param array target array
     * @param <T>   type of element
     * @return randomly chosen element
     */
    public static <T> T randomChoice(T[] array) {
        Random rand = new Random((new Date()).getTime());
        return array[rand.nextInt(array.length)];
    }
}
