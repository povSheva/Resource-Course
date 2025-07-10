package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class FileEntity {
    private UUID uuid;
    private String origName;
    private String type;
    private Long sizeBytes;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
}