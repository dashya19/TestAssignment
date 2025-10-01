### Команды для запуска

```bash
# Перейти в папку проекта
cd task-3-weather-service

# Скомпилировать проект
mvn compile

# Запустить сервер
mvn exec:java -Dexec.mainClass="com.weatherservice.WeatherServer"

# Или собрать JAR и запустить
mvn clean package
java -jar target/weather-service-1.0.jar

# Доступ к приложению
http://localhost:8080

#API Endpoints
GET /weather?city={город}
GET / - главная страница с формой
GET https://geocoding-api.open-meteo.com/v1/search?name={city}
GET https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&hourly=temperature_2m
