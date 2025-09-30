package com.fileservice.controller;

import com.fileservice.service.FileStorage;
import com.fileservice.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Загрузка файлов на сервер
public class UploadHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String token = getToken(exchange);
        if (JwtUtil.validateToken(token) == null) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String fileName = getFileNameFromRequest(exchange); // имя файла из query параметров или заголовков

        try {
            String id = FileStorage.saveFile(fileName, exchange.getRequestBody());

            JSONObject resp = new JSONObject();
            resp.put("downloadId", id);

            byte[] bytes = resp.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        } finally {
            exchange.close();
        }
    }

    private String getFileNameFromRequest(HttpExchange exchange) {
        // Получение из query параметра
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.contains("filename=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("filename=")) {
                    return param.substring(9);
                }
            }
        }

        // Получение из заголовков
        String contentDisposition = exchange.getRequestHeaders().getFirst("Content-Disposition");
        if (contentDisposition != null && contentDisposition.contains("filename=")) {
            int start = contentDisposition.indexOf("filename=") + 9;
            int end = contentDisposition.indexOf("\"", start + 1);
            if (end > start) {
                return contentDisposition.substring(start + 1, end);
            }
        }

        return "file_" + System.currentTimeMillis() + ".bin"; // значение по умолчанию
    }

    private String getToken(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }
}
