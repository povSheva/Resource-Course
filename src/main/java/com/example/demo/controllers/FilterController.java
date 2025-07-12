package com.example.demo.controllers;

import com.example.demo.entity.FileEntity;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Централизованная фильтрация: типы + текстовый поиск. */
public class FilterController {

    private final ObservableList<FileEntity> view;
    private final List<FileEntity> masterCopy;

    private final Map<String, BooleanProperty> typeChecks = new LinkedHashMap<>();



    private Predicate<FileEntity> typePredicate = f -> true;

    // ---------- текстовый поиск ----------
    private Predicate<FileEntity> searchPredicate = f -> true;

    // слушатели, которым сообщаем о появлении нового типаа файла
    private final List<Consumer<Map.Entry<String, BooleanProperty>>> listeners = new ArrayList<>();

    public void addTypeAddedListener(Consumer<Map.Entry<String, BooleanProperty>> l) {
        listeners.add(l);
    }

    public FilterController(ObservableList<FileEntity> view) {
        this.view = view;
        this.masterCopy = new ArrayList<>(view);

        masterCopy.stream()
                .map(f -> f.getType().toUpperCase())
                .distinct()
                .forEach(this::ensureType);
    }

    /** вызов из SearchController при добавлении нового файла */
    public void onAddNew(FileEntity f) {
        masterCopy.add(f);
        ensureType(f.getType());        // вдруг это новый формат
        applyAllFilters();
    }

    /** Вызывается при каждом изменении текста в строке поиска. */
    public void onSearchText(String query) {
        if (query == null || query.isBlank()) {
            searchPredicate = f -> true;
        } else {
            String q = query.toLowerCase();
            searchPredicate = f -> f.getOrigName().toLowerCase().contains(q);
        }
        applyAllFilters();
    }

    /** Доступ к BooleanProperty чек-боксов для биндинга в UI. */
    public Map<String, BooleanProperty> getTypeChecks() { return typeChecks; }

    /* ------------ приватная «кухня» -------------------------------------- */

    private void rebuildTypePredicate() {
        List<String> selected = typeChecks.entrySet().stream()
                .filter(e -> e.getValue().get())
                .map(Map.Entry::getKey)
                .toList();

        typePredicate = selected.isEmpty()
                ? f -> true
                : f -> selected.contains(f.getType().toUpperCase());

        applyAllFilters();
    }

    private void applyAllFilters() {
        List<FileEntity> filtered = masterCopy.stream()
                .filter(searchPredicate.and(typePredicate))
                .collect(Collectors.toList());
        view.setAll(filtered);
    }

    /** Если такого типа ещё нет — создаём Property и сообщаем в UI. */

    private void ensureType(String rawType) {
        String type = rawType.toUpperCase();
        if (typeChecks.containsKey(type)) return;

        BooleanProperty prop = new SimpleBooleanProperty(false);
        prop.addListener((obs, o, n) -> rebuildTypePredicate());
        typeChecks.put(type, prop);

        // уведомляем всех подписчиков (UI) о новом типе
        Map.Entry<String, BooleanProperty> entry = Map.entry(type, prop);
        listeners.forEach(l -> l.accept(entry));
    }
}
