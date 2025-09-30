package com.fileservice.controller;

import com.fileservice.model.FileMetadata;
import com.fileservice.service.FileStorage;
import com.fileservice.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;

// Список файлов пользователя
public class UserFilesHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String token = getToken(exchange);
        if (JwtUtil.validateToken(token) == null) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        try {
            Collection<FileMetadata> files = FileStorage.getAll();
            JSONArray jsonArray = new JSONArray();

            for (FileMetadata file : files) {
                JSONObject json = new JSONObject();
                json.put("fileName", file.fileName);
                json.put("fileSize", file.fileSize);
                json.put("downloadId", file.id);
                json.put("uploadDate", file.uploadDate.getTime());
                json.put("downloadCount", file.downloadCount);
                jsonArray.put(json);
            }

            JSONObject response = new JSONObject();
            response.put("files", jsonArray);

            byte[] bytes = response.toString().getBytes();
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

    private String getToken(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }
}