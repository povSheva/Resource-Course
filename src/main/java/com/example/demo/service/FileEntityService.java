package com.example.demo.service;

import com.example.demo.dao.FileEntityDao;
import com.example.demo.entity.FileEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с файлами без доп. метаданных.
 * <p>
 * Обеспечивает:
 * 1) копирование файлов в репозиторий;
 * 2) сохранение базовой информации через {@link FileEntityDao#save(FileEntity)};
 * 3) получение списка файлов через {@link FileEntityDao#findAll()}.
 */
        public class FileEntityService {

    /* =============================
     * DAO
     * ===========================*/
    private final FileEntityDao fileDao;

    /* =============================
     * Путь к корню репозитория
     * ===========================*/
    private final Path repoRoot;

    /* =============================
     * Конструктор
     * ===========================*/
    /**
     * @param fileDao      DAO для таблицы files
     * @param repoRootPath путь к корню репозитория
     */
    public FileEntityService(FileEntityDao fileDao, String repoRootPath) {
        this.fileDao = fileDao;
        this.repoRoot = Paths.get(repoRootPath);
    }

    /* =============================
     * Методы сервиса
     * ===========================*/
    /**
     * Получить список всех файлов.
     */
    public List<FileEntity> findAll() {
        return fileDao.findAll();
    }

    /**
     * Загрузить файл в репозиторий и сохранить запись в базе.
     *
     * @param src локальный файл-источник
     * @return сохранённый FileEntity с заполненным uuid и addedAt
     * @throws IOException при ошибке I/O
     */
    public FileEntity uploadFile(File src) throws IOException {
        // 1) генерируем уникальный ключ хранения
        String key = generateStorageKey(src.getName());

        // 2) копируем файл в репозиторий
        Path dst = repoRoot.resolve(key);
        Files.createDirectories(dst.getParent());
        Files.copy(src.toPath(), dst, StandardCopyOption.REPLACE_EXISTING);

        // 3) сохраняем базовые метаданные
        FileEntity file = new FileEntity();
        file.setStorageKey(dst.toAbsolutePath().toString());
        file.setOrigName(src.getName());
        file.setType(detectTypeByExtension(src.getName()));
        file.setSizeBytes(src.length());
        file.setAddedAt(LocalDateTime.now());

        return fileDao.save(file);
    }

    /* =============================
     * Вспомогательные методы
     * ===========================*/
    /**
     * Генерирует storage key вида xx/yy/uuid.ext.
     */
    private String generateStorageKey(String originalName) {
        String uuid = UUID.randomUUID().toString().replace('-', '0');
        String ext = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.'))
                : "";
        return uuid.substring(0, 2) + "/"
                + uuid.substring(2, 4) + "/"
                + uuid + ext;
    }

    /**
     * Определяет тип по расширению имени файла.
     */
    private String detectTypeByExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        String ext = (idx >= 0) ? fileName.substring(idx + 1).toLowerCase() : "";
        return switch (ext) {
            case "pdf" -> "PDF";
            case "png", "jpg", "jpeg" -> "Image";
            case "doc", "docx" -> "Docx";
            default -> ext.isEmpty() ? "" : ext.toUpperCase();
        };
    }
}
