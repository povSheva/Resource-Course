package com.example.demo.ui.controllers;

import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

// Получилось только так перенести обработку событий, уже лучше)

public class ResourceController {

    public void onExportClick(Button exportBtn) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл для экспорта");

        String exampleFolder = "exports";

        File folder = new File(exampleFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf"),
                new FileChooser.ExtensionFilter("Image files (*.png, *.jpg, *.gif)", "*.png", "*.jpg", "*.gif"),
                new FileChooser.ExtensionFilter("Word files (*.docx)", "*.docx"),
                new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx")
        );

        File selectedFile = fileChooser.showSaveDialog(exportBtn.getScene().getWindow());

        if (selectedFile != null) {

            Path patchOfSelectedFile = Path.of(exampleFolder, selectedFile.getName());

            String fileName = selectedFile.getName();
            String fileType = getFileExtension(fileName);
            String dateAdded = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
            String fileMemory = selectedFile.length() + "M";

            // Так для примера добавлю вывод
            System.out.println(fileName);
            System.out.println(fileType);
            System.out.println(dateAdded);
            System.out.println(fileMemory);

            try {
                Files.copy(selectedFile.toPath(), patchOfSelectedFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Удачно скопировался :)");

            } catch (IOException e) {
                System.out.println("Что-то пошло не так :(");
                System.out.println("Ошибка: " + e);
            }
        }
            else{
                System.out.println("Файл не выбран!");
            }
    }
    // Тут просто вынес метод для получения расширения файла

    private String getFileExtension(String fileName) {

        if (fileName != null && fileName.lastIndexOf(".") != -1) {

            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }

        return "";
    }

}
