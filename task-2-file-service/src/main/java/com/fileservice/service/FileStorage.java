package com.fileservice.service;

import com.fileservice.model.FileMetadata;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Сервис управления файлами
public class FileStorage {
    private static final String STORAGE_PATH = "src/main/resources/files/";
    private static final String METADATA_FILE = "src/main/resources/files_metadata.json";
    private static final Map<String, FileMetadata> files = new ConcurrentHashMap<>();

    static {
        loadMetadata();
    }

    public static String saveFile(String fileName, InputStream input) throws IOException {
        String id = UUID.randomUUID().toString();
        Path path = Paths.get(STORAGE_PATH, id + "_" + fileName);
        Files.copy(input, path, StandardCopyOption.REPLACE_EXISTING);

        FileMetadata meta = new FileMetadata(id, fileName, Files.size(path));
        files.put(id, meta);

        saveMetadata(); // сохранение метаданных после добавления файла

        return id;
    }

    public static File getFile(String id) {
        FileMetadata meta = files.get(id);
        if (meta != null) {
            meta.downloadCount++;
            saveMetadata(); // сохранение метаданных после увеличения счетчика скачиваний
            return new File(STORAGE_PATH, id + "_" + meta.fileName);
        }
        return null;
    }

    public static FileMetadata getFileMetadata(String id) {
        return files.get(id);
    }

    public static Collection<FileMetadata> getAll() {
        return files.values();
    }

    // Загрузка метаданных из файла
    private static void loadMetadata() {
        try {
            File metadataFile = new File(METADATA_FILE);
            if (metadataFile.exists()) {
                String content = new String(Files.readAllBytes(metadataFile.toPath()));
                JSONArray jsonArray = new JSONArray(content);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    FileMetadata meta = new FileMetadata(
                            json.getString("id"),
                            json.getString("fileName"),
                            json.getLong("fileSize")
                    );
                    meta.uploadDate = new Date(json.getLong("uploadDate"));
                    meta.downloadCount = json.getInt("downloadCount");

                    // проверка, существует ли файл на диске
                    File file = new File(STORAGE_PATH, meta.id + "_" + meta.fileName);
                    if (file.exists()) {
                        files.put(meta.id, meta);
                    }
                }
                System.out.println("Loaded " + files.size() + " file metadata entries");
            }
        } catch (Exception e) {
            System.err.println("Error loading metadata: " + e.getMessage());
        }
    }

    // Сохранение метаданных в файл
    private static void saveMetadata() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (FileMetadata meta : files.values()) {
                JSONObject json = new JSONObject();
                json.put("id", meta.id);
                json.put("fileName", meta.fileName);
                json.put("fileSize", meta.fileSize);
                json.put("uploadDate", meta.uploadDate.getTime());
                json.put("downloadCount", meta.downloadCount);
                jsonArray.put(json);
            }

            Files.write(Paths.get(METADATA_FILE), jsonArray.toString().getBytes());
        } catch (Exception e) {
            System.err.println("Error saving metadata: " + e.getMessage());
        }
    }

    // Восстановление метаданных из существующих файлов (на случай если метаданные потеряны)
    public static void recoverMetadataFromFiles() {
        try {
            File storageDir = new File(STORAGE_PATH);
            if (!storageDir.exists()) {
                storageDir.mkdirs();
                return;
            }

            File[] fileList = storageDir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isFile()) {
                        String filename = file.getName();
                        int underscoreIndex = filename.indexOf('_');

                        if (underscoreIndex > 0) {
                            String id = filename.substring(0, underscoreIndex);
                            String originalName = filename.substring(underscoreIndex + 1);

                            // Если файл есть на диске, но нет в метаданных - идет восстанавление
                            if (!files.containsKey(id)) {
                                FileMetadata meta = new FileMetadata(id, originalName, file.length());
                                meta.uploadDate = new Date(file.lastModified());
                                files.put(id, meta);
                                System.out.println("Recovered metadata for file: " + originalName);
                            }
                        }
                    }
                }
                saveMetadata(); // сохранение восстановленных метаданных
            }
        } catch (Exception e) {
            System.err.println("Error recovering metadata from files: " + e.getMessage());
        }
    }

    public static void startCleanupTask() {
        recoverMetadataFromFiles(); // восстанавлеение метаданных из файлов

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            Date now = new Date();
            files.values().removeIf(meta -> {
                long days = (now.getTime() - meta.uploadDate.getTime()) / (1000L * 60 * 60 * 24);
                if (days > 30) {
                    File f = new File(STORAGE_PATH, meta.id + "_" + meta.fileName);
                    if (f.exists()) {
                        f.delete();
                    }
                    return true;
                }
                return false;
            });
            saveMetadata(); // сохранение метаданных после очистки
        }, 1, 24, TimeUnit.HOURS);
    }
}