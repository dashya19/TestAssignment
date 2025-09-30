package com.fileservice.controller;

import com.fileservice.service.FileStorage;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

// Скачивание файлов
public class DownloadHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        if (parts.length < 4) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }
        String id = parts[3];

        // Получение метаданных файла для получения оригинального имени
        com.fileservice.model.FileMetadata meta = FileStorage.getFileMetadata(id);
        if (meta == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        File file = FileStorage.getFile(id);
        if (file == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        // Устанавка правильных заголовков
        exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
        exchange.getResponseHeaders().add("Content-Disposition",
                "attachment; filename=\"" + meta.fileName + "\"");
        exchange.getResponseHeaders().add("Content-Length", String.valueOf(file.length()));

        exchange.sendResponseHeaders(200, file.length());

        try (OutputStream os = exchange.getResponseBody();
             FileInputStream fis = new FileInputStream(file)) {
            fis.transferTo(os);
        }
    }
}

