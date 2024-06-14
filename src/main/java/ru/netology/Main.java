package ru.netology;

import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
        ExecutorService threadPool = Executors.newFixedThreadPool(64);

        try (final var serverSocket = new ServerSocket(9999)) {
            while (true) {
                threadPool.execute(new Server(serverSocket.accept(), validPaths));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //2. Класс Server обрабатывает параметры Qery String
    public static class Server implements Runnable {
        private final Socket socket;
        private final List<String> validPaths;

        public Server(Socket socket, List<String> validPaths) {
            this.socket = socket;
            this.validPaths = validPaths;
        }

        @Override
        public void run() {
            try (
                    final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final var out = new BufferedOutputStream(socket.getOutputStream());
            ) {
                final var requestLine = in.readLine();
                final var parts = requestLine.split(" ");

                if (parts.length !=3) {
                    return;
                }

                final var path = parts[1];
                Map<String, String> queryParams = extractQueryParams(path);

                //3. Реализовал фунциональность по обработке параметров из Query  String
                final var request = new Request(path, queryParams);
                String lastParam = request.getQueryParam("last");
                if (lastParam != null) {
                    int lastValue = Integer.parseInt(lastParam);
                }

                //4. Доработал фунциональность поиска хендлера
                final var pathQuery = request.getPath().split("\\?")[0];
                if (!validPaths.contains(pathQuery)) {
                    out.write((
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Lenght: 0\r\n" +
                                    "Connection: Close\r"
                            ).getBytes());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private Map<String, String> extractQueryParams(String path) {
            Map<String, String> queryParams = new HashMap<>();
            int queryIdx = path.indexOf("?");
            if (queryIdx != -1) {
                String queryString = path.substring(queryIdx + 1);
                String[] pairs = queryString.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        queryParams.put(keyValue[0], keyValue[1]);
                    }
                }
            }
            return queryParams;
        }
    }
}

