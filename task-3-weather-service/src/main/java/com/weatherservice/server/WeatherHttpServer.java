package com.weatherservice.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.weatherservice.service.WeatherService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

// HTTP сервер
public class WeatherHttpServer {
    private final HttpServer server; // HTTP сервер
    private final WeatherService weatherService; // для получения данных о погоде

    public WeatherHttpServer(int port, WeatherService weatherService) throws IOException {
        this.weatherService = weatherService;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        setupRoutes();
    }

    // Настройка маршрутов сервера
    private void setupRoutes() {
        server.createContext("/", new FileHandler("templates/index.html", "text/html"));
        server.createContext("/static", new StaticHandler());
        server.createContext("/weather", new WeatherHandler());
        server.createContext("/health", e -> sendResponse(e, 200, "{\"status\":\"OK\"}"));
        server.setExecutor(null);
    }

    public void start() { server.start(); }
    public void stop() { server.stop(0); }

    // Обработка для файлов
    private class FileHandler implements HttpHandler {
        private final String resourcePath, contentType;
        FileHandler(String resourcePath, String contentType) {
            this.resourcePath = resourcePath; this.contentType = contentType;
        }
        @Override public void handle(HttpExchange ex) throws IOException {
            if (!"GET".equals(ex.getRequestMethod())) { sendResponse(ex, 405, ""); return; }
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (is == null) { sendResponse(ex, 404, "{\"error\":\"Не найдено\"}"); return; }
                byte[] data = is.readAllBytes();
                ex.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
                ex.sendResponseHeaders(200, data.length);
                try (OutputStream os = ex.getResponseBody()) { os.write(data); }
            }
        }
    }

    // Обработка статических файлов
    private class StaticHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if (!"GET".equals(ex.getRequestMethod())) { sendResponse(ex, 405, ""); return; }
            String path = ex.getRequestURI().getPath().substring(1); // удаляем ведущий слеш
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                if (is == null) { sendResponse(ex, 404, "{\"error\":\"Файл не найден\"}"); return; }
                String type = path.endsWith(".css") ? "text/css" :
                        path.endsWith(".js") ? "application/javascript" : "application/octet-stream";
                byte[] data = is.readAllBytes();
                ex.getResponseHeaders().set("Content-Type", type);
                ex.sendResponseHeaders(200, data.length);
                try (OutputStream os = ex.getResponseBody()) { os.write(data); }
            }
        }
    }

    // Обработка запросов погоды
    private class WeatherHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if (!"GET".equals(ex.getRequestMethod())) { sendResponse(ex, 405, ""); return; }
            String city = extractCityFromQuery(ex.getRequestURI().getQuery());
            if (city == null || city.isEmpty()) {
                sendResponse(ex, 400, "{\"error\":\"Параметр city обязателен\"}"); return;
            }
            String response = weatherService.getWeatherData(city);
            sendResponse(ex, 200, response);
        }
        private String extractCityFromQuery(String query) {
            if (query == null) return null;
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2 && kv[0].equals("city")) return kv[1];
            }
            return null;
        }
    }

    // Отправка HTTP ответа
    private void sendResponse(HttpExchange ex, int status, String body) throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(status, data.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(data); }
    }
}
