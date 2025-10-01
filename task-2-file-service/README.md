### Команды для запуска

```bash
# Перейти в папку проекта
cd task-2-file-service

# Скомпилировать проект
mvn compile

# Запустить сервер
mvn exec:java -Dexec.mainClass="com.fileservice.FileServer"

# Или собрать JAR и запустить
mvn clean package
java -jar target/file-service-1.0.jar
