package me.sakigamiyang.httpbin4j;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Random;

public class Helpers {
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
    public static <T> T randomChoice(@Nonnull T[] array) {
        Random rand = new Random((new Date()).getTime());
        return array[rand.nextInt(array.length)];
    }

    /**
     * Join byte arrays into one byte array.
     *
     * @param delimiter  delimiter
     * @param byteArrays byte arrays
     * @return byte array
     */
    public static byte[] joinByteArrays(@Nonnull byte[] delimiter, @Nonnull byte[]... byteArrays) {
        if (byteArrays.length == 0) {
            return new byte[0];
        }
        if (byteArrays.length == 1) {
            return byteArrays[0];
        }
        byte[] result = byteArrays[0];
        for (int i = 1; i < byteArrays.length; ++i) {
            result = ByteBuffer.allocate(result.length + delimiter.length)
                    .put(result)
                    .put(delimiter)
                    .array();

            byte[] temp = byteArrays[i];
            result = ByteBuffer.allocate(result.length + temp.length)
                    .put(result)
                    .put(temp)
                    .array();
        }
        return result;
    }
}
