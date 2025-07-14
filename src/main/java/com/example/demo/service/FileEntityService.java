package com.example.demo.service;

import com.example.demo.dao.FileEntityDao;
import com.example.demo.entity.FileEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с основными файлами (таблица files + файловая система).
 */
public class FileEntityService {

    private final FileEntityDao fileDao;
    private final Path repoRoot;

    /**
     * @param fileDao   DAO для таблицы files
     * @param repoRoot  путь к корневой папке хранения (строка)
     */
    public FileEntityService(FileEntityDao fileDao, String repoRoot) {
        this.fileDao = fileDao;
        this.repoRoot = Paths.get(repoRoot);
        try {
            Files.createDirectories(this.repoRoot);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку репозитория: " + this.repoRoot, e);
        }
    }

    /** Список всех файлов (без метаданных). */
    public List<FileEntity> findAll() {
        return fileDao.findAll();
    }

    /**
     * Загружает файл в репозиторий:
     * - копирует в папку repoRoot,
     * - сохраняет запись в БД,
     * - возвращает сохранённую сущность с установленным UUID и addedAt.
     */
    public FileEntity uploadFile(File source) {
        // Генерируем уникальный storageKey
        String storageKey = UUID.randomUUID() + "_" + source.getName();
        Path dest = repoRoot.resolve(storageKey);

        try {
            Files.copy(source.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось скопировать файл в репозиторий", e);
        }

        // Подготовка и сохранение в БД
        FileEntity entity = new FileEntity();
        entity.setStorageKey(storageKey);
        entity.setOrigName(source.getName());
        entity.setType(detectType(source.getName()));
        entity.setSizeBytes(source.length());

        return fileDao.save(entity);
    }

    /**
     * Удаляет файл и запись:
     * - сперва удаляем сам файл с диска,
     * - затем строку из таблицы files.
     */
    public void deleteFile(UUID fileUuid) {
        // Сначала получаем данные о файле, чтобы знать storageKey
        FileEntity f = fileDao.findById(fileUuid)
                .orElseThrow(() -> new RuntimeException("Файл не найден: " + fileUuid));

        // Удаляем с диска
        try {
            Files.deleteIfExists(repoRoot.resolve(f.getStorageKey()));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось удалить файл с диска", e);
        }

        // Удаляем из БД
        fileDao.deleteById(fileUuid);
    }

    /** Примитивное определение типа по расширению файла. */
    private String detectType(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx + 1).toUpperCase() : "UNKNOWN";
    }
}
