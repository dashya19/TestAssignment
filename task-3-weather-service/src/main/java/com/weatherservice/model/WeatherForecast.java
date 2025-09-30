package com.weatherservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Прогноз погоды
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherForecast {
    private HourlyData hourly; // почасовые данные о погоде

    public WeatherForecast() {}

    public WeatherForecast(HourlyData hourly) {
        this.hourly = hourly;
    }

    public HourlyData getHourly() { return hourly; }
    public void setHourly(HourlyData hourly) { this.hourly = hourly; }

    // Класс для хранения почасовых данных
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HourlyData {
        private List<String> time; // временные метки

        @JsonProperty("temperature_2m")
        private List<Double> temperature2m; // темп на высоте 2м

        public HourlyData() {}
        public HourlyData(List<String> time, List<Double> temperature2m) {
            this.time = time;
            this.temperature2m = temperature2m;
        }

        public List<String> getTime() { return time; }
        public void setTime(List<String> time) { this.time = time; }
        public List<Double> getTemperature2m() { return temperature2m; }
        public void setTemperature2m(List<Double> temperature2m) { this.temperature2m = temperature2m; }
    }
}
