package com.example.demo.entity;

import java.time.LocalDate;
import java.util.UUID;

public class FileEntity {
    private UUID uuid;
    private String origName;
    private String type;
    private Long sizeBytes;
    private LocalDate addedAt;
    private LocalDate updatedAt;

    // Конструктор для всех полей
    public FileEntity(UUID uuid,
                      String origName,
                      String type,
                      Long sizeBytes,
                      LocalDate addedAt,
                      LocalDate updatedAt) {
        this.uuid      = uuid;
        this.origName  = origName;
        this.type      = type;
        this.sizeBytes = sizeBytes;
        this.addedAt   = addedAt;
        this.updatedAt = updatedAt;
    }

    // Геттеры
    public UUID getUuid()            { return uuid; }
    public String getOrigName()      { return origName; }
    public String getType()          { return type; }
    public Long getSizeBytes()       { return sizeBytes; }
    public LocalDate getAddedAt()    { return addedAt; }
    public LocalDate getUpdatedAt()  { return updatedAt; }

    // Сеттеры (если нужны)
    public void setAddedAt(LocalDate d)   { this.addedAt = d; }
    public void setUpdatedAt(LocalDate d) { this.updatedAt = d; }

    @Override
    public String toString() {
        return "FileEntity{" +
                "uuid=" + uuid +
                ", origName='" + origName + '\'' +
                ", type='" + type + '\'' +
                ", sizeBytes=" + sizeBytes +
                ", addedAt=" + addedAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
