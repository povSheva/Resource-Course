package com.example.demo.service;

import com.example.demo.dao.FileAdditionalDao;
import com.example.demo.entity.FileAdditionalEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с дополнительными метаданными файлов (таблица file_metadata).
 */
public class FileAdditionalEntityService {

    private final FileAdditionalDao additionalDao;

    public FileAdditionalEntityService(FileAdditionalDao additionalDao) {
        this.additionalDao = additionalDao;
    }

    /**
     * Возвращает все метаданные.
     */
    public List<FileAdditionalEntity> findAllMetadata() {
        return additionalDao.findAll();
    }

    /**
     * Ищет метаданные по UUID файла.
     */
    public Optional<FileAdditionalEntity> findMetadataByFileUuid(UUID fileUuid) {
        return additionalDao.findByFileUuid(fileUuid);
    }

    /**
     * Возвращает существующие метаданные или создаёт дефолтные, если записи нет.
     */
    public FileAdditionalEntity getOrCreateMetadata(UUID fileUuid) {
        return additionalDao.findByFileUuid(fileUuid)
                .orElseGet(() -> new FileAdditionalEntity(
                        fileUuid,
                        LocalDateTime.now(),
                        false,
                        "",
                        ""
                ));
    }

    /**
     * Сохраняет (upsert) метаданные.
     */
    public FileAdditionalEntity saveMetadata(FileAdditionalEntity meta) {
        return additionalDao.save(meta);
    }

    /**
     * Удаляет метаданные по UUID файла.
     */
    public void deleteMetadata(UUID fileUuid) {
        additionalDao.deleteByFileUuid(fileUuid);
    }
}
