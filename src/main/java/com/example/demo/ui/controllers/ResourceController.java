package com.example.demo.ui.controllers;

import com.example.demo.entity.FileEntity;
import com.example.demo.util.CreatFileEntity;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;

public class ResourceController {

    // Каталог для хранения скопированных файлов
    private static final Path EXPORT_FOLDER = Path.of("exports");

    /**
     * Вызывается при клике на кнопку «Экспорт» в вашем FXML.
     * Берёт выбранный файл и передаёт его в сервис, получая обратно FileEntity.
     */
    @FXML
    public void onExportClick(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите файл для экспорта");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"),
                new FileChooser.ExtensionFilter("Images (*.png, *.jpg, *.gif)", "*.png", "*.jpg", "*.gif"),
                new FileChooser.ExtensionFilter("Word (*.docx)", "*.docx"),
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx")
        );

        // здесь мы создаём переменную selectedFile
        File selectedFile = chooser.showOpenDialog(
                ((Node) event.getSource()).getScene().getWindow()
        );

        if (selectedFile == null) {
            System.out.println("Файл не выбран");
            return;
        }

        try {
            // передаём именно selectedFile
            FileEntity entity = CreatFileEntity.createFileEntity(selectedFile, EXPORT_FOLDER);
            System.out.println("Импорт завершён: " + entity);
            // → тут сохраняем entity в БД или передаём дальше
        } catch (Exception ex) {
            System.err.println("Ошибка при импорте файла: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
