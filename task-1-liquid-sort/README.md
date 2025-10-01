### Команды для запуска

```bash
# Перейти в папку проекта
cd task-1-liquid-sort

# Скомпилировать проект
mvn compile

# Запустить приложение
mvn exec:java -Dexec.mainClass="com.liquids.LiquidsSortingApplication"

# Или собрать JAR и запустить
mvn clean package
java -jar target/liquid-sort-1.0.jar
