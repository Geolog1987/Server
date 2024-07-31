package org.example;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.io.BufferedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    // Список допустимых путей к файлам
    private static final List<String> VALID_PATHS = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private static final int PORT = 9999;// Порт, на котором будет запущен сервер
    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);// Пул потоков для обработки запросов клиентов

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();// Ожидание подключения клиента
                threadPool.execute(() -> handleClient(socket));// Обработка подключения в отдельном потоке
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для обработки подключения клиента
    private void handleClient(Socket socket) {
        try (
                // Создание потоков для чтения запроса и записи ответа
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            // Чтение первой строки запроса (request line)
            String requestLine = in.readLine();
            // Разделение строки запроса на части
            String[] parts = requestLine.split(" ");

            // Проверка корректности запроса и допустимости пути
            if (parts.length != 3 || !VALID_PATHS.contains(parts[1])) {
                sendNotFoundResponse(out);
                return;
            }

            // Построение пути к файлу
            Path filePath = Path.of(".", "public", parts[1]);
            // Определение MIME-типа файла
            String mimeType = Files.probeContentType(filePath);

            // Обработка специального случая для classic.html
            if ("/classic.html".equals(parts[1])) {
                // Замена {time} на текущие дату и время в содержимом файла
                String content = new String(Files.readString(filePath).replace("{time}", LocalDateTime.now().toString()).getBytes());
                sendResponse(out, mimeType, content.getBytes());
            } else {
                // Чтение содержимого файла и отправка ответа клиенту
                byte[] content = Files.readAllBytes(filePath);
                sendResponse(out, mimeType, content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для отправки ответа 404 Not Found
    private void sendNotFoundResponse(BufferedOutputStream out) throws IOException {
        out.write(("HTTP/1.1 404 Not Found\\r\\n" +
                "Content-Length: 0\\r\\n" +
                "Connection: close\\r\\n" +
                "\\r\\n").getBytes());
        out.flush();
    }

    // Метод для отправки успешного ответа
    private void sendResponse(BufferedOutputStream out, String mimeType, byte[] content) throws IOException {
        out.write(("HTTP/1.1 200 OK\\r\\n" +
                "Content-Type: " + mimeType + "\\r\\n" +
                "Content-Length: " + content.length + "\\r\\n" +
                "Connection: close\\r\\n" +
                "\\r\\n").getBytes());
        out.write(content);
        out.flush();
    }

    // Главный метод для запуска сервера
    public static void main(String[] args) {
        new Server().start();
    }
}
