package com.fileservice.config;

import com.fileservice.controller.*;
import com.fileservice.service.FileStorage;
import com.fileservice.util.StaticFileHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

// Класс конфигурации HTTP сервера
public class HttpServerApp {
    public static void start(int port) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // API
        server.createContext("/api/auth/login", new AuthHandler());
        server.createContext("/api/upload", new UploadHandler());
        server.createContext("/api/download", new DownloadHandler());
        server.createContext("/api/stats", new StatsHandler());
        server.createContext("/api/user/files", new UserFilesHandler());

        // Frontend
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        System.out.println("Server started at http://localhost:" + port);
        server.start();

        // Очистка старых файлов
        FileStorage.startCleanupTask();
    }
}