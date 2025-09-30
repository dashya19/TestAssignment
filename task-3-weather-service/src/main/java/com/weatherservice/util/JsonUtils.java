package com.weatherservice.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherservice.model.CityCoordinates;
import com.weatherservice.model.WeatherForecast;

import java.util.List;
import java.util.Map;

// Утилиты JSON
public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"error\":\"Ошибка сериализации JSON\"}";
        }
    }

    // Пасринг инф о городе
    public static CityCoordinates parseCityInfo(String json) {
        try {
            Map<String,Object> resp = mapper.readValue(json, new TypeReference<>(){});
            List<Map<String,Object>> results = (List<Map<String,Object>>) resp.get("results");
            if (results != null && !results.isEmpty()) {
                Map<String,Object> city = results.get(0);
                String name = (String) city.get("name");
                Double lat = (Double) city.get("latitude");
                Double lon = (Double) city.get("longitude");
                if (name != null && lat != null && lon != null)
                    return new CityCoordinates(name, lat, lon);
            }
        } catch (Exception ignored) {}
        return null;
    }

    // Парсинг даннх погоды
    public static WeatherForecast parseWeatherData(String json) {
        try {
            return mapper.readValue(json, WeatherForecast.class);
        } catch (Exception e) {
            return null;
        }
    }
}