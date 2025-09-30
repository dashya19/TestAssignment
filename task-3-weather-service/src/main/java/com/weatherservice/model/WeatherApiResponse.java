package com.weatherservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Ответ API погоды
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherApiResponse {
    private String city; // название города
    private double latitude; // широта местополож
    private double longitude; // долгота местополож
    private WeatherForecast weatherData; // данные о погоде
    private String temperatureChart; // base64-encoded изображение графика темп

    public WeatherApiResponse(String city, double latitude, double longitude,
                              WeatherForecast weatherData, String temperatureChart) {
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.weatherData = weatherData;
        this.temperatureChart = temperatureChart;
    }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public WeatherForecast getWeatherData() { return weatherData; }
    public void setWeatherData(WeatherForecast weatherData) { this.weatherData = weatherData; }
    public String getTemperatureChart() { return temperatureChart; }
    public void setTemperatureChart(String temperatureChart) { this.temperatureChart = temperatureChart; }
}