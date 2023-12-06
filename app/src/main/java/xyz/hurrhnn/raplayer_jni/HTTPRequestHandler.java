package xyz.hurrhnn.raplayer_jni;

import org.jsoup.Connection;
import org.jsoup.UncheckedIOException;

import java.net.SocketTimeoutException;

public class HTTPRequestHandler {

    public static class Connect extends HTTPRequestHandler {
        protected final int maxAttempt = 5;

        Connection connection;
        private int currentAttempt = 0;

        public Connect(Connection connection) {
            this.connection = connection;
        }

        public Connection.Response tryConnect() throws Exception {
            while (true) {
                currentAttempt++;
                try {
                    Connection.Response response = connection.timeout(1000).execute();
                    if (response.body() != null) {
                        // System.out.println("Tried " + currentAttempt + ", ");
                        return response;
                    }
                } catch (SocketTimeoutException | UncheckedIOException socketTimeoutException) {
                    if (currentAttempt == maxAttempt)
                        break;
                }
            }
            throw new SocketTimeoutException();
        }
    }
}
