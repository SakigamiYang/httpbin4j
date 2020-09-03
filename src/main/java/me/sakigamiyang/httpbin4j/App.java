package me.sakigamiyang.httpbin4j;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;

/**
 * App.
 */
@Slf4j
public class App {
    public static void main(String[] args) throws Throwable {
        Server server = new Server(80);
        server.setHandler(new HttpBinHandler());

        try {
            server.start();
            server.join();
        } finally {
            server.stop();
            server.destroy();
        }
    }
}
