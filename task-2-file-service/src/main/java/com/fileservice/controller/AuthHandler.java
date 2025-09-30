package com.fileservice.controller;

import com.fileservice.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Обработчик аутентификации
public class AuthHandler implements HttpHandler {
    private static final String USER = "admin";
    private static final String PASS = "admin";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(body);

            String username = json.optString("username");
            String password = json.optString("password");

            System.out.println("Попытка входа в систему: " + username + "/" + password);

            if (USER.equals(username) && PASS.equals(password)) {
                String token = JwtUtil.generateToken(username);
                JSONObject response = new JSONObject();
                response.put("token", token);

                byte[] bytes = response.toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            } else {
                exchange.sendResponseHeaders(401, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        } finally {
            exchange.close();
        }
    }
}
