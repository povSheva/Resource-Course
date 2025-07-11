package com.example.demo.controllers;

import com.example.demo.entity.FileEntity;
import com.example.demo.service.FileEntityService;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Контроллер кнопки «Import / Export». Работает через {@link FileEntityService}:
 * сервис сам копирует файл в репозиторий, рассчитывает {@code storageKey}
 * и сохраняет метаданные в БД.
 */
public class ResourceController {

    private final FileEntityService service;
    private final ObservableList<FileEntity> uiList;

    public ResourceController(FileEntityService service,
                              ObservableList<FileEntity> uiList) {
        this.service = service;
        this.uiList  = uiList;
    }

    @FXML
    public void onExportClick(ActionEvent event) {

        /* ---------- диалог выбора файла -------------------------------- */
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите файл для импорта");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF",    "*.pdf", "*.PDF"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Word",   "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("Excel",  "*.xls", "*.xlsx"),
                new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        File selected = chooser.showOpenDialog(
                ((Node) event.getSource()).getScene().getWindow()
        );
        if (selected == null) return;

        /* ---------- фон: копирование файла + запись в БД --------------- */
        Task<FileEntity> uploadTask = new Task<>() {
            @Override protected FileEntity call() throws Exception {
                return service.uploadFile(selected);
            }
        };

        /* ---------- UI-поток: обновляем список -------------------------- */
        uploadTask.setOnSucceeded(e -> uiList.add(0, uploadTask.getValue()));
        uploadTask.setOnFailed(e -> uploadTask.getException().printStackTrace());

        new Thread(uploadTask, "file-upload-task").start();
    }
}
