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

// Статистика системы
public class StatsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String token = getToken(exchange);
        if (JwtUtil.validateToken(token) == null) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        Collection<FileMetadata> files = FileStorage.getAll();
        long totalSize = files.stream().mapToLong(f -> f.fileSize).sum();

        JSONObject resp = new JSONObject();
        resp.put("totalFiles", files.size());
        resp.put("totalSize", totalSize);
        resp.put("activeDownloads", files.size());

        JSONArray arr = new JSONArray();
        files.forEach(f -> {
            JSONObject j = new JSONObject();
            j.put("fileName", f.fileName);
            j.put("fileSize", f.fileSize);
            j.put("uploadDate", f.uploadDate.getTime());
            j.put("downloadCount", f.downloadCount);
            arr.put(j);
        });
        resp.put("recentFiles", arr);

        byte[] bytes = resp.toString().getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private String getToken(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }
}
