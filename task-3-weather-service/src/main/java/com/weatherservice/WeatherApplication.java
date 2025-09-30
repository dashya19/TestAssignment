package com.weatherservice;

import com.weatherservice.server.WeatherHttpServer;
import com.weatherservice.service.LocationService;
import com.weatherservice.service.WeatherCacheService;
import com.weatherservice.service.WeatherService;

public class WeatherApplication {
    public static void main(String[] args) {
        try {
            int port = 8080;
            WeatherCacheService cacheService = new WeatherCacheService();
            LocationService geoCodingService = new LocationService(); // сервис геолокации
            WeatherService weatherService = new WeatherService(geoCodingService, cacheService);  // сервис погоды

            WeatherHttpServer server = new WeatherHttpServer(port, weatherService);
            System.out.println("Сервис погоды запущен на порту " + port);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}