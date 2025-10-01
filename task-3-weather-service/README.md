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
## API

### Проверка состояния

```
GET /health
```

Ответ:

```json
{"status":"OK"}
```

### Получение прогноза

```
GET /weather?city={cityName}
```

Пример:

```
GET http://localhost:8080/weather?city=Berlin
```

Ответ:

```json
{
  "city": "Berlin",
  "latitude": 52.5244,
  "longitude": 13.4105,
  "weatherData": {
    "hourly": {
      "time": ["2025-10-01T00:00", "2025-10-01T01:00", ...],
      "temperature_2m": [15.3, 14.8, ...]
    }
  },
  "temperatureChart": "iVBORw0KGgoAAAANS..."   # PNG в base64
}
```

### Веб-интерфейс

* Главная страница: [http://localhost:8080](http://localhost:8080)
* Статические ресурсы: `/static/styles.css`, `/static/script.js`

## Зависимости

* **Jackson** — сериализация/десериализация JSON
* **JFreeChart** — построение графиков
* **Java HttpClient (java.net.http)** — вызовы внешних API

## Пример использования через curl

```bash
# Проверка состояния
curl http://localhost:8080/health

# Получение прогноза
curl http://localhost:8080/weather?city=London
```
