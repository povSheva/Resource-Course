package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Модель файла, хранящая «чистые» метаданные из таблицы {@code files}.
 * Важно: поле {@code storageKey} содержит <b>относительный</b> путь / ключ
 * к самому файлу в репозитории. Базовый каталог задаётся в конфигурации
 * приложения (см. {@code settings.REPO_ROOT}).
 */
public class FileEntity {

    /* =============================
     * Поля (из таблицы files)
     * ===========================*/
    private UUID uuid;              // UNIQUE PRIMARY KEY
    private String storageKey;      // относительный путь / ключ
    private String origName;        // оригинальное имя файла
    private String type;            // pdf, docx, link, excel…
    private long sizeBytes;         // размер в байтах
    private LocalDateTime addedAt;  // когда добавлен (TIMESTAMP)

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
                      LocalDateTime addedAt) {
        this.uuid        = uuid;
        this.storageKey  = storageKey;
        this.origName    = origName;
        this.type        = type;
        this.sizeBytes   = sizeBytes;
        this.addedAt     = addedAt;
    }

    /* =============================
     * Геттеры
     * ===========================*/
    public UUID getUuid()              { return uuid; }
    public String getStorageKey()      { return storageKey; }
    public String getOrigName()        { return origName; }
    public String getType()            { return type; }
    public long getSizeBytes()         { return sizeBytes; }
    public LocalDateTime getAddedAt()  { return addedAt; }

    /* =============================
     * Сеттеры
     * ===========================*/
    public void setUuid(UUID uuid)                     { this.uuid = uuid; }
    public void setStorageKey(String storageKey)       { this.storageKey = storageKey; }
    public void setOrigName(String origName)           { this.origName = origName; }
    public void setType(String type)                   { this.type = type; }
    public void setSizeBytes(long sizeBytes)           { this.sizeBytes = sizeBytes; }
    public void setAddedAt(LocalDateTime addedAt)      { this.addedAt = addedAt; }

    /* =============================
     * Utility
     * ===========================*/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileEntity)) return false;
        FileEntity that = (FileEntity) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "FileEntity{" +
                "uuid=" + uuid +
                ", storageKey='" + storageKey + '\'' +
                ", origName='" + origName + '\'' +
                ", type='" + type + '\'' +
                ", sizeBytes=" + sizeBytes +
                ", addedAt=" + addedAt +
                '}';
    }
}
