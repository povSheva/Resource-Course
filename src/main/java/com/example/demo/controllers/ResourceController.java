package com.example.demo.controllers;

import com.example.demo.dao.FileEntityDao;
import com.example.demo.entity.FileEntity;
import com.example.demo.service.FileEntityService;
import com.example.demo.util.CreatFileEntity;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Path;


public class ResourceController {

    private final FileEntityService service;
    private final ObservableList<FileEntity> uiList;   // ← тот самый список

    public ResourceController(FileEntityService service,
                              ObservableList<FileEntity> uiList) {
        this.service = service;
        this.uiList  = uiList;
    }

    public static final Path EXPORT_FOLDER = Path.of("exports");

    @FXML
    public void onExportClick(ActionEvent event) {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите файл для экспорта");
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

        // --- фон: сохраняем файл и запись -------------------------------------------------
        Task<FileEntity> saveTask = new Task<>() {
            @Override protected FileEntity call() throws Exception {
                FileEntity entity = CreatFileEntity.createFileEntity(selected, EXPORT_FOLDER);
                return service.save(entity);
            }
        };

        // --- UI-поток: добавляем в ObservableList -----------------------------------------
        saveTask.setOnSucceeded(e -> {
            FileEntity saved = saveTask.getValue();
            uiList.add(0, saved);
        });

        saveTask.setOnFailed(e -> {
            Throwable ex = saveTask.getException();
            System.err.println("Exception import: " + ex.getMessage());
            ex.printStackTrace();
        });

        new Thread(saveTask, "import-task").start();
    }
}
