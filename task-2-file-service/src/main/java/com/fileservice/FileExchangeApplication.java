package com.fileservice;

// Точка входа в приложение
public class FileExchangeApplication {
    public static void main(String[] args) throws Exception {
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get("src/main/resources/files"));
        HttpServerApp.start(8080);
    }
}
