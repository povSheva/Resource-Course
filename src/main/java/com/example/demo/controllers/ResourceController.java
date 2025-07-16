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
 * Контроллер кнопки «Import / Export». Теперь уведомляет FilterController
 * о каждом новом файле, чтобы адаптивно обновлять фильтры.
 */
public class ResourceController {

    private final FileEntityService service;
    private final ObservableList<FileEntity> uiList;
    private final FilterController filterController;

    public ResourceController(FileEntityService service,
                              ObservableList<FileEntity> uiList,
                              FilterController filterController) {
        this.service          = service;
        this.uiList           = uiList;
        this.filterController = filterController;
    }

    @FXML
    public void onExportClick(ActionEvent event) {
        // 1) Показываем диалог выбора файла
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

        // 2) Фон: копируем файл и сохраняем в БД
        Task<FileEntity> uploadTask = new Task<>() {
            @Override
            protected FileEntity call() throws Exception {
                return service.uploadFile(selected);
            }
        };

        // 3) UI-поток: вставляем в список и уведомляем фильтры
        uploadTask.setOnSucceeded(e -> {
            FileEntity newFile = uploadTask.getValue();
            uiList.add(0, newFile);
            filterController.onAddNew(newFile);
        });
        uploadTask.setOnFailed(e -> uploadTask.getException().printStackTrace());

        new Thread(uploadTask, "file-upload-task").start();
    }
}
