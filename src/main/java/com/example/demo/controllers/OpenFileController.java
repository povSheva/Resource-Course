package com.example.demo.controllers;

import com.example.demo.entity.FileEntity;
import javafx.application.HostServices;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Сервис открытия файлов/ссылок, хранящихся в репозитории.
 */
public class OpenFileController {

    private final Path repoRoot;      // корневая папка репозитория
    private final HostServices host;  // нужен для открытия URL в браузере

    /** @param repoRoot   абсолютный путь к корню хранилища
     *  @param host       HostServices из Application#getHostServices()
     */
    public OpenFileController(Path repoRoot, HostServices host) {
        this.repoRoot = repoRoot;
        this.host     = host;
    }

    public void open(FileEntity file) throws IOException {

        Path abs = repoRoot.resolve(file.getStorageKey());

        if (!Files.exists(abs)) {
            throw new IOException("Файл не найден: " + abs);
        }

        if ("LINK".equalsIgnoreCase(file.getType())) {
            host.showDocument(abs.toUri().toString());
        } else {
            Desktop.getDesktop().open(abs.toFile());
        }
    }
}
