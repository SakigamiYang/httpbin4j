import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Utils {
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
}
