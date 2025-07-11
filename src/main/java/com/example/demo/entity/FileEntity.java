package com.example.demo.entity;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Модель файла, хранящая метаданные из таблицы {@code files} / {@code files_trash}.
 * <p>
 * Важно: поле {@code storageKey} содержит <b>относительный</b> путь / ключ
 * к самому файлу в репозитории. Базовый каталог задаётся в конфигурации
 * приложения (см. {@code settings.REPO_ROOT}).
 */
public class FileEntity {

    /* =============================
     * Поля
     * ===========================*/
    private UUID uuid;              // PK
    private String storageKey;      // относительный путь / ключ
    private String origName;        // оригинальное имя
    private String type;            // pdf, docx, ...
    private long sizeBytes;         // размер в байтах
    private LocalDate addedAt;      // когда добавлен
    private LocalDate updatedAt;    // когда метаданные менялись

    /* =============================
     * Конструкторы
     * ===========================*/

    /** Пустой конструктор для ORM / сериализации */
    public FileEntity() {}

    /**
     * Полный конструктор.
     */
    public FileEntity(UUID uuid,
                      String storageKey,
                      String origName,
                      String type,
                      long sizeBytes,
                      LocalDate addedAt,
                      LocalDate updatedAt) {
        this.uuid        = uuid;
        this.storageKey  = storageKey;
        this.origName    = origName;
        this.type        = type;
        this.sizeBytes   = sizeBytes;
        this.addedAt     = addedAt;
        this.updatedAt   = updatedAt;
    }

    /* =============================
     * Геттеры
     * ===========================*/
    public UUID getUuid()            { return uuid; }
    public String getStorageKey()    { return storageKey; }
    public String getOrigName()      { return origName; }
    public String getType()          { return type; }
    public long getSizeBytes()       { return sizeBytes; }
    public LocalDate getAddedAt()    { return addedAt; }
    public LocalDate getUpdatedAt()  { return updatedAt; }

    /* =============================
     * Сеттеры
     * ===========================*/
    public void setUuid(UUID uuid)                 { this.uuid = uuid; }
    public void setStorageKey(String storageKey)   { this.storageKey = storageKey; }
    public void setOrigName(String origName)       { this.origName = origName; }
    public void setType(String type)               { this.type = type; }
    public void setSizeBytes(long sizeBytes)       { this.sizeBytes = sizeBytes; }
    public void setAddedAt(LocalDate addedAt)      { this.addedAt = addedAt; }
    public void setUpdatedAt(LocalDate updatedAt)  { this.updatedAt = updatedAt; }

    /* =============================
     * Utility
     * ===========================*/
    @Override
    public String toString() {
        return "FileEntity{" +
                "uuid=" + uuid +
                ", storageKey='" + storageKey + '\'' +
                ", origName='" + origName + '\'' +
                ", type='" + type + '\'' +
                ", sizeBytes=" + sizeBytes +
                ", addedAt=" + addedAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
