package com.fileservice.model;

import java.util.Date;

// Модель метаданных файла
public class FileMetadata {
    public String id;
    public String fileName;
    public long fileSize;
    public Date uploadDate;
    public int downloadCount = 0;

    public FileMetadata(String id, String fileName, long fileSize) {
        this.id = id;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.uploadDate = new Date();
    }
}
