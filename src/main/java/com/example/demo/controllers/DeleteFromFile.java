package com.example.demo.controllers;

import com.example.demo.entity.FileEntity;
import com.example.demo.service.FileEntityService;
import javafx.scene.control.ListView;

public class DeleteFromFile {
    private final FileEntityService fileService;
    private final ListView<FileEntity> fileListView;

    public DeleteFromFile(FileEntityService fileService, ListView<FileEntity> fileListView) {
        this.fileService = fileService;
        this.fileListView = fileListView;
    }

    public void deleteFile() {
        FileEntity selectedFile = fileListView.getSelectionModel().getSelectedItem();
        if (selectedFile != null) {
            fileService.deleteFile(selectedFile.getUuid());
        }
    }
}
