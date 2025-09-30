package com.weatherservice.service;

import com.weatherservice.model.CityCoordinates;
import com.weatherservice.model.WeatherForecast;
import com.weatherservice.util.JsonUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// Сервис геолокации
public class LocationService {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();
    // HTTP клиент
    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search"; // URL API для геокодинга
    private static final String WEATHER_URL = "https://api.open-meteo.com/v1/forecast"; // URL API для погоды

    // Метод для получения координат города
    public CityCoordinates getCityCoordinates(String city) {
        try {
            String url = GEOCODING_URL + "?name=" + java.net.URLEncoder.encode(city, "UTF-8")
                    + "&count=1&language=en&format=json";

            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.statusCode() == 200 ? JsonUtils.parseCityInfo(response.body()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    // Метод для получения данных о погоде по координатам
    public WeatherForecast getWeatherData(double latitude, double longitude) {
        try {
            String url = WEATHER_URL + "?latitude=" + latitude + "&longitude=" + longitude
                    + "&hourly=temperature_2m&forecast_days=2&timezone=auto";

            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.statusCode() == 200 ? JsonUtils.parseWeatherData(response.body()) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
