package com.example.demo.controllers;

import com.example.demo.entity.FileEntity;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер для операций с ресурсами (файлами).
 * Реализует мгновенный поиск и обновление списка.
 */
public class SearchController {

    private final ObservableList<FileEntity> fileItems;
    private final List<FileEntity> originalList;

    /**
     * @param fileItems отображаемый список файлов
     */
    public SearchController(ObservableList<FileEntity> fileItems) {
        this.fileItems = fileItems;
        this.originalList = new ArrayList<>(fileItems);
    }

    /**
     * Фильтр по подстроке в имени файла, вызывается при каждом изменении текста.
     */
    public void onSearch(String query) {
        if (query == null || query.isBlank()) {
            fileItems.setAll(originalList);
        } else {
            String lower = query.toLowerCase();
            List<FileEntity> filtered = originalList.stream()
                    .filter(f -> f.getOrigName().toLowerCase().contains(lower))
                    .collect(Collectors.toList());
            fileItems.setAll(filtered);
        }
    }

    /**
     * Добавление нового файла: обновляем и исходный, и отображаемый списки.
     */
    public void onAddNew(FileEntity added) {
        originalList.add(added);
        fileItems.add(added);
    }
}
