package com.example.demo.dao;

import com.example.demo.config.ConnectionManager;
import com.example.demo.entity.FileEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO для работы с таблицей files.
 * Теперь поддерживает новое поле storage_key (относительный путь к файлу в репозитории).
 */
public class FileEntityDao implements CrudDao {

    /**
     * Вставка новой записи. Добавили колонку storage_key.
     */
    private static final String INSERT_FILE = """
        INSERT INTO files
          (storage_key, orig_name, type, size_bytes)
        VALUES (?, ?, ?, ?)
        """;

    /**
     * Выбор всех «живых» файлов с учётом storage_key.
     */
    private static final String SELECT_ALL = """
        SELECT uuid,
               storage_key,
               orig_name,
               type,
               size_bytes,
               added_at,
               updated_at
          FROM files
        """;

    @Override
    public FileEntity save(FileEntity file) {
        try (Connection conn = ConnectionManager.open();
             PreparedStatement stmt = conn.prepareStatement(
                     INSERT_FILE,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            // 1) storage_key
            stmt.setString(1, file.getStorageKey());
            // 2) остальные поля как раньше
            stmt.setString(2, file.getOrigName());
            stmt.setString(3, file.getType());
            stmt.setLong(4, file.getSizeBytes());

            int affected = stmt.executeUpdate();
            if (affected != 1) {
                throw new SQLException("Ожидалась вставка 1 строки, вставлено: " + affected);
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    UUID generatedUuid = rs.getObject(1, UUID.class);
                    file.setUuid(generatedUuid);
                }
            }

            return file;
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось сохранить FileEntity", e);
        }
    }

    @Override
    public List<FileEntity> findAll() {
        try (var conn = ConnectionManager.open();
             var stmt = conn.prepareStatement(SELECT_ALL);
             var rs   = stmt.executeQuery()) {

            var list = new ArrayList<FileEntity>();
            while (rs.next()) {
                FileEntity f = new FileEntity();
                f.setUuid(rs.getObject("uuid", UUID.class));
                f.setStorageKey(rs.getString("storage_key"));
                f.setOrigName(rs.getString("orig_name"));
                f.setType(rs.getString("type"));
                f.setSizeBytes(rs.getLong("size_bytes"));
                f.setAddedAt(rs.getTimestamp("added_at")
                        .toLocalDateTime()
                        .toLocalDate());
                f.setUpdatedAt(rs.getTimestamp("updated_at")
                        .toLocalDateTime()
                        .toLocalDate());
                list.add(f);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось получить список FileEntity", e);
        }
    }

    /* Остальные CRUD-методы (findById, deleteById, ...) остаются без изменений
       и, при необходимости, должны быть дополнены работой со storage_key. */
}
