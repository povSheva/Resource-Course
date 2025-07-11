package com.example.demo.service;

import com.example.demo.dao.FileEntityDao;
import com.example.demo.entity.FileEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class FileEntityService {

    private final FileEntityDao dao;
    private final Path repoRoot = Paths.get("file_repo");   // вынесите в конфиг

    public FileEntityService(FileEntityDao dao) {
        this.dao = dao;
    }

    /** Вернуть все файлы из БД */
    public List<FileEntity> findAll() {
        return dao.findAll();
    }

    /** Скопировать файл в репозиторий и сохранить метаданные */
    public FileEntity uploadFile(File src) throws IOException {
        // 1. генерируем storageKey
        String key = generateStorageKey(src.getName());

        Path dst = repoRoot.resolve(key);
        Files.createDirectories(dst.getParent());
        Files.copy(src.toPath(), dst, StandardCopyOption.REPLACE_EXISTING);

        // 2. собираем сущность
        FileEntity f = new FileEntity();
        f.setStorageKey(key);
        f.setOrigName(src.getName());
        f.setType(detectTypeByExtension(src));
        f.setSizeBytes(src.length());
        f.setAddedAt(LocalDate.now());
        f.setUpdatedAt(LocalDate.now());

        return dao.save(f);
    }

    /* ---------- утилиты -------------------------------------------------- */

    private String generateStorageKey(String originalName) {
        String uuid = UUID.randomUUID().toString().replace('-', '0');
        String ext  = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";
        return uuid.substring(0,2) + "/" + uuid.substring(2,4) + "/" + uuid + ext;
    }

    private String detectTypeByExtension(File file) {
        String ext = "";
        int i = file.getName().lastIndexOf('.');
        if (i >= 0) ext = file.getName().substring(i + 1).toLowerCase();
        return switch (ext) {
            case "pdf"                    -> "PDF";
            case "png", "jpg", "jpeg"     -> "Image";
            case "doc", "docx"            -> "Docx";
            default                       -> ext.toUpperCase();
        };
    }
}
