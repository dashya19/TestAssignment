package com.weatherservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Модель координат города
@JsonIgnoreProperties(ignoreUnknown = true)
public class CityCoordinates {
    private String name; // название города
    private double latitude; // широта
    private double longitude; // долгота

    public CityCoordinates() {}

    public CityCoordinates(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
