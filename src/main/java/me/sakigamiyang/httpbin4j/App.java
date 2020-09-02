package me.sakigamiyang.httpbin4j;

import org.eclipse.jetty.server.Server;

/**
 * App.
 */
public class App {
    public static void main(String[] args) throws Throwable {
        Server server = new Server(80);
        server.setHandler(new HttpBinHandler());
        server.start();
        server.join();
    }
}
