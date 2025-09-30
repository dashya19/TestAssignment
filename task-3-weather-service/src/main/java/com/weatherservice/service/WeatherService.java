package com.weatherservice.service;

import com.weatherservice.model.WeatherForecast;
import com.weatherservice.model.CityCoordinates;
import com.weatherservice.model.WeatherApiResponse;
import com.weatherservice.util.JsonUtils;
import com.weatherservice.util.ChartGenerator;
import java.util.Base64;

public class WeatherService {
    private final LocationService geoCodingService; // для работы с геолокацией
    private final WeatherCacheService cacheService; // для работы с кэшем

    public WeatherService(LocationService geoCodingService, WeatherCacheService cacheService) {
        this.geoCodingService = geoCodingService;
        this.cacheService = cacheService;
    }

    // Метод для получения данных о погоде
    public String getWeatherData(String city) {
        try {
            String cached = cacheService.get(city);
            if (cached != null) return cached;

            CityCoordinates coords = geoCodingService.getCityCoordinates(city);
            if (coords == null) return "{\"error\": \"Город не найден\"}";

            WeatherForecast forecast = geoCodingService.getWeatherData(
                    coords.getLatitude(), coords.getLongitude());
            if (forecast == null || forecast.getHourly() == null || forecast.getHourly().getTemperature2m() == null)
                return "{\"error\": \"Данные о погоде недоступны\"}";

            byte[] chart = ChartGenerator.generateTemperatureChart(
                    forecast.getHourly().getTime(),
                    forecast.getHourly().getTemperature2m(),
                    city
            );

            WeatherApiResponse response = new WeatherApiResponse(
                    city, coords.getLatitude(), coords.getLongitude(),
                    forecast,
                    chart.length > 0 ? Base64.getEncoder().encodeToString(chart) : ""
            );

            String json = JsonUtils.toJson(response);
            cacheService.put(city, json);
            return json;
        } catch (Exception e) {
            return "{\"error\": \"Не удалось получить данные о погоде\"}";
        }
    }
}
