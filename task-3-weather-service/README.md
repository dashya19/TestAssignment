# 3. Сервис прогноза погоды по городам

Простой HTTP-сервис прогноза погоды, написанный на Java без использования Spring. Работает с открытым API [Open-Meteo](https://open-meteo.com/), поддерживает кэширование и генерацию графиков температуры.

## Возможности

* Получение геокоординат города через Open-Meteo Geocoding API
* Получение почасового прогноза температуры
* Кэширование ответов (15 минут)
* Генерация графиков температуры (PNG в base64)
* Веб-интерфейс на HTML + CSS + JS

### Основные компоненты

* **WeatherHttpServer** — встроенный HTTP-сервер (com.sun.net.httpserver)
* **LocationService** — работа с API Open-Meteo для геокодинга и прогноза
* **WeatherService** — объединяет логику, формирует JSON-ответ
* **WeatherCacheService** — кэширует данные на 15 минут
* **ChartGenerator** — строит PNG-график на основе JFreeChart
* **JsonUtils** — парсинг и сериализация JSON через Jackson

## Запуск

```bash
# Перейти в каталог проекта
cd task-3-weather-service

# Скомпилировать
mvn clean package

# Запустить
java -cp target/task-3-weather-service-1.0-SNAPSHOT.jar com.weatherservice.WeatherApplication
```

## Запуск

1. Склонировать проект:

```bash
# Перейти в папку задания 3
cd task-3-weather-service

# Скомпилировать и запустить
mvn compile exec:java
```

---

2. Сервер будет доступен на: [http://localhost:8080](http://localhost:8080)

---
## Внешние API

Сервис использует публичные API **Open-Meteo**:

### Геокодинг города

```
GET https://geocoding-api.open-meteo.com/v1/search?name={city}&count=1&language=en&format=json
```

Пример:

```
https://geocoding-api.open-meteo.com/v1/search?name=Berlin&count=1&language=en&format=json
```

### Прогноз погоды

```
GET https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&hourly=temperature_2m&forecast_days=2&timezone=auto
```

Пример:

```
https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m&forecast_days=2&timezone=auto
```

### Веб-интерфейс

* Главная страница: [http://localhost:8080](http://localhost:8080)
* Статические ресурсы: `/static/styles.css`, `/static/script.js`



