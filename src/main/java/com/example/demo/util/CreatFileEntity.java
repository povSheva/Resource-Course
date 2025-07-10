package com.example.demo.util;

import com.example.demo.entity.FileEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;

public final class CreatFileEntity {

    public static FileEntity createFileEntity(File sourceFile, Path storageDir) throws IOException {
        // 1) Создаём папку, если её нет
        if (Files.notExists(storageDir)) {
            Files.createDirectories(storageDir);
        }

        // 2) Копируем файл
        Path targetPath = storageDir.resolve(sourceFile.getName());
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 3) Собираем метаданные
        LocalDate now = LocalDate.now();

        return new FileEntity(
                UUID.randomUUID(),                   // uuid
                sourceFile.getName(),                // origName
                getExtension(sourceFile.getName()),  // type
                sourceFile.length(),                 // sizeBytes
                now,                                 // addedAt
                now                                  // updatedAt
        );
    }

    // Вспомогательный метод для извлечения расширения
    private static String getExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        System.out.println("GOTOVO");
        return (idx == -1) ? "" : fileName.substring(idx + 1).toLowerCase();
    }

    // Запрещаем создание экземпляров
    private CreatFileEntity() {}
}
