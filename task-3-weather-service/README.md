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
