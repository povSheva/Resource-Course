package com.example.demo.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Базовый CRUD-DAO для сущностей с UUID-ключом.
 *
 * @param <E> тип сущности
 */
public interface CrudDao<E> {
    /**
     * Сохраняет новую или обновлённую сущность.
     */
    E save(E e);

    /**
     * Возвращает все записи.
     */
    List<E> findAll();

    /**
     * Ищет запись по UUID.
     */
    //Optional<E> findById(UUID uuid);

    /**
     * Удаляет запись по UUID.
     */
    //void deleteById(UUID uuid);

    /**
     * Ищет сущности по подстроке в оригинальном имени.
     * (реализуйте только в FileEntityDao; в других можно бросать UnsupportedOperationException)
     */
    //List<E> findByOrigNameLike(String keyword);

    /**
     * Ищет сущности по типу.
     * (реализуйте только в FileEntityDao; в других можно бросать UnsupportedOperationException)
     */
    //List<E> findByType(String type);
}
