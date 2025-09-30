package com.weatherservice.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Кэширование
public class WeatherCacheService {
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>(); // хранение кэшированных данных
    private static final long CACHE_TTL = 15 * 60 * 1000; // время жизни 15 минут

    // Класс для представления записи кэша
    private static class CacheEntry {
        String data; // данные в кэше
        long timestamp; // временная метка создания записи

        CacheEntry(String data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        // Метод проверки истечения срока действия записи
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL;
        }
    }

    // Метод для помещения данных в кэш
    public void put(String key, String data) {
        cache.put(key, new CacheEntry(data));
    }

    // Метод для получения данных из кэша
    public String get(String key) {
        CacheEntry entry = cache.get(key); // получение по ключу
        if (entry != null && !entry.isExpired()) {
            return entry.data;
        }
        if (entry != null) {
            cache.remove(key);
        }
        return null;
    }

    // Метод для очистки всего кэша
    public void clear() {
        cache.clear();
    }
}
