package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Модель дополнительных метаданных файла из таблицы {@code file_metadata}.
 * Хранит информацию о времени обновления, флаге «закреплён», теге и произвольной
 * дополнительной информации.
 */
public class FileMetadata {

    /* =============================
     * Поля (из таблицы file_metadata)
     * ===========================*/
    private UUID fileUuid;             // PRIMARY KEY и FK → files.uuid
    private LocalDateTime updatedAt;   // когда метаданные были обновлены
    private boolean pinned;            // флаг «закреплён»
    private String tag;                // тег
    private String additionalInfo;     // произвольная доп. информация

    /* =============================
     * Конструкторы
     * ===========================*/

    /** Пустой конструктор для ORM / сериализации */
    public FileMetadata() {}

    /**
     * Полный конструктор.
     */
    public FileMetadata(UUID fileUuid,
                        LocalDateTime updatedAt,
                        boolean pinned,
                        String tag,
                        String additionalInfo) {
        this.fileUuid       = fileUuid;
        this.updatedAt      = updatedAt;
        this.pinned         = pinned;
        this.tag            = tag;
        this.additionalInfo = additionalInfo;
    }

    /* =============================
     * Геттеры
     * ===========================*/
    public UUID getFileUuid()                { return fileUuid; }
    public LocalDateTime getUpdatedAt()      { return updatedAt; }
    public boolean isPinned()                { return pinned; }
    public String getTag()                   { return tag; }
    public String getAdditionalInfo()        { return additionalInfo; }

    /* =============================
     * Сеттеры
     * ===========================*/
    public void setFileUuid(UUID fileUuid)                 { this.fileUuid = fileUuid; }
    public void setUpdatedAt(LocalDateTime updatedAt)      { this.updatedAt = updatedAt; }
    public void setPinned(boolean pinned)                  { this.pinned = pinned; }
    public void setTag(String tag)                         { this.tag = tag; }
    public void setAdditionalInfo(String additionalInfo)   { this.additionalInfo = additionalInfo; }

    /* =============================
     * Utility
     * ===========================*/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileMetadata)) return false;
        FileMetadata that = (FileMetadata) o;
        return Objects.equals(fileUuid, that.fileUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileUuid);
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "fileUuid=" + fileUuid +
                ", updatedAt=" + updatedAt +
                ", pinned=" + pinned +
                ", tag='" + tag + '\'' +
                ", additionalInfo='" + additionalInfo + '\'' +
                '}';
    }
}
