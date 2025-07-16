package com.example.demo.controllers;

import com.example.demo.entity.FileEntity;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Централизованная фильтрация: типы + текстовый поиск. */
public class FilterController {
    private final ObservableList<FileEntity> view;
    private final List<FileEntity> masterCopy;

    // 1) Сколько файлов каждого типа «на полке»
    private final Map<String, Integer> typeUsage = new HashMap<>();

    // 2) Свойства для чек-боксов
    private final Map<String, BooleanProperty> typeChecks = new LinkedHashMap<>();

    private Predicate<FileEntity> typePredicate = f -> true;
    private Predicate<FileEntity> searchPredicate = f -> true;

    // 3) Слушатели для добавления и удаления типов
    private final List<Consumer<Map.Entry<String, BooleanProperty>>> typeAddedListeners   = new ArrayList<>();
    private final List<Consumer<String>>                          typeRemovedListeners = new ArrayList<>();

    public FilterController(ObservableList<FileEntity> view) {
        this.view = view;
        this.masterCopy = new ArrayList<>(view);

        // стартовая инициализация: «заполняем» счётчики и чек-боксы
        for (FileEntity f : view) {
            incType(f);
        }
    }

    // API для UI
    public Map<String, BooleanProperty> getTypeChecks() { return typeChecks; }
    public void addTypeAddedListener(Consumer<Map.Entry<String, BooleanProperty>> l) { typeAddedListeners.add(l); }
    public void addTypeRemovedListener(Consumer<String> l)                           { typeRemovedListeners.add(l); }

    /** Новый файл в систему попал (или добавлен в view) */
    public void onAddNew(FileEntity f) {
        masterCopy.add(f);
        incType(f);
        applyAllFilters();
    }

    /** Файл удалили физически (или убрали из view) */
    public void onRemove(FileEntity f) {
        masterCopy.remove(f);
        decType(f);
        applyAllFilters();
    }

    /** Поиск по тексту */
    public void onSearchText(String query) {
        if (query == null || query.isBlank()) {
            searchPredicate = f -> true;
        } else {
            String q = query.toLowerCase();
            searchPredicate = f -> f.getOrigName().toLowerCase().contains(q);
        }
        applyAllFilters();
    }

    // --- внутренняя кухня ---

    private void incType(FileEntity f) {
        String t = f.getType().toUpperCase();
        typeUsage.merge(t, 1, Integer::sum);
        if (!typeChecks.containsKey(t)) {
            BooleanProperty prop = new SimpleBooleanProperty(false);
            prop.addListener((obs, o, n) -> rebuildTypePredicate());
            typeChecks.put(t, prop);
            // уведомляем UI, что появился новый тип
            Map.Entry<String, BooleanProperty> entry = Map.entry(t, prop);
            typeAddedListeners.forEach(l -> l.accept(entry));
        }
    }

    private void decType(FileEntity f) {
        String t = f.getType().toUpperCase();
        int leftover = typeUsage.merge(t, -1, Integer::sum);
        if (leftover <= 0) {
            // больше ни одного файла этого типа
            typeUsage.remove(t);
            BooleanProperty prop = typeChecks.remove(t);
            if (prop != null) {
                // уведомляем UI, что тип нужно убрать
                typeRemovedListeners.forEach(l -> l.accept(t));
            }
            rebuildTypePredicate();
        }
    }

    private void rebuildTypePredicate() {
        List<String> sel = typeChecks.entrySet().stream()
                .filter(e -> e.getValue().get())
                .map(Map.Entry::getKey)
                .toList();
        typePredicate = sel.isEmpty()
                ? f -> true
                : f -> sel.contains(f.getType().toUpperCase());
        applyAllFilters();
    }

    private void applyAllFilters() {
        List<FileEntity> filtered = masterCopy.stream()
                .filter(searchPredicate.and(typePredicate))
                .collect(Collectors.toList());
        view.setAll(filtered);
    }
}