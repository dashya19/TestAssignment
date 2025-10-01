# 2. Сервис обмена файлами

Простой файловый сервер на Java с поддержкой авторизации (JWT), загрузки и скачивания файлов, получения статистики и работы со статическими файлами (frontend).

## Возможности

* Аутентификация через `JWT`
* Загрузка файлов на сервер
* Скачивание файлов по `ID`
* Получение статистики (размер, количество, активные загрузки)
* Просмотр списка загруженных файлов
* Очистка старых файлов (старше 30 дней)
* Раздача статических файлов (`index.html`, CSS, JS)

---

## Архитектура

* **HttpServerApp** — инициализация и запуск встроенного HTTP-сервера.
* **FileStorage** — управление сохранением, чтением и удалением файлов + метаданные.
* **AuthHandler** — принимает логин/пароль и выдает JWT токен.
* **UploadHandler** — загружает файлы (требует авторизации).
* **DownloadHandler** — скачивание файлов по `downloadId`.
* **StatsHandler** — возвращает статистику (файлы, размер, загрузки).
* **UserFilesHandler** — список всех файлов пользователя.
* **JwtUtil** — генерация и проверка JWT токенов.
* **StaticFileHandler** — раздача статических файлов (SPA).

---

## API эндпоинты

### Авторизация

`POST /api/auth/login`

```json
{
  "username": "admin",
  "password": "admin"
}
```

Ответ:

```json
{
  "token": "<jwt-token>"
}
```

### Загрузка файла

`POST /api/upload?filename=test.txt`

* Заголовок: `Authorization: Bearer <token>`
* Тело: бинарные данные файла

Ответ:

```json
{
  "downloadId": "uuid-идентификатор"
}
```

### Скачивание файла

`GET /api/download/{id}`

* Возвращает файл с оригинальным именем.

### Статистика

`GET /api/stats`

* Заголовок: `Authorization: Bearer <token>`

Ответ:

```json
{
  "totalFiles": 5,
  "totalSize": 1234567,
  "activeDownloads": 5,
  "recentFiles": [
    {
      "fileName": "test.txt",
      "fileSize": 100,
      "uploadDate": 1696234123000,
      "downloadCount": 2
    }
  ]
}
```

### Список файлов пользователя

`GET /api/user/files`

* Заголовок: `Authorization: Bearer <token>`

Ответ:

```json
{
  "files": [
    {
      "fileName": "test.txt",
      "fileSize": 100,
      "downloadId": "uuid",
      "uploadDate": 1696234123000,
      "downloadCount": 2
    }
  ]
}
```

---

## Запуск

1. Склонировать проект:

```bash
# Перейти в папку задания 2
cd task-2-file-service

# Скомпилировать и запустить
mvn compile exec:java
```

---

4. Сервер будет доступен на: [http://localhost:8080](http://localhost:8080)

---

## Примечания

* Данные хранятся в `src/main/resources/files/`.
* Метаданные сохраняются в `files_metadata.json`.
* Очистка старых файлов запускается автоматически (файлы старше 30 дней удаляются).

