package com.example.demo.dao;

import com.example.demo.config.ConnectionManager;
import com.example.demo.entity.FileEntity;
import com.example.demo.entity.FileMetadata;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO для работы с таблицей {@code files}.
 */
public class FileEntityDao implements CrudDao <FileEntity> {

    /* =============================
     * SQL-запросы
     * ===========================*/

    /**
     * Вставка новой записи в files.
     * UUID и added_at генерируются базой автоматически.
     */
    private static final String INSERT_FILE = """
        INSERT INTO files
          (storage_key, orig_name, type, size_bytes)
        VALUES (?, ?, ?, ?)
        RETURNING uuid, added_at
        """;

    /**
     * Выбор всех записей из files (без метаданных).
     */
    private static final String SELECT_ALL = """
        SELECT uuid,
               storage_key,
               orig_name,
               type,
               size_bytes,
               added_at
          FROM files
        """;

    @Override
    public FileEntity save(FileEntity file) {
        try (Connection conn = ConnectionManager.open();
             PreparedStatement stmt = conn.prepareStatement(
                     INSERT_FILE
             )) {

            // 1) storage_key
            stmt.setString(1, file.getStorageKey());
            // 2) остальные поля
            stmt.setString(2, file.getOrigName());
            stmt.setString(3, file.getType());
            stmt.setLong(4, file.getSizeBytes());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    file.setUuid(rs.getObject("uuid", UUID.class));
                    file.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());
                }
            }
            return file;
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось сохранить FileEntity", e);
        }
    }

    @Override
    public List<FileEntity> findAll() {
        try (Connection conn = ConnectionManager.open();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs   = stmt.executeQuery()) {

            List<FileEntity> list = new ArrayList<>();
            while (rs.next()) {
                FileEntity f = new FileEntity();
                f.setUuid(rs.getObject("uuid", UUID.class));
                f.setStorageKey(rs.getString("storage_key"));
                f.setOrigName(rs.getString("orig_name"));
                f.setType(rs.getString("type"));
                f.setSizeBytes(rs.getLong("size_bytes"));
                f.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());
                list.add(f);

                System.out.println("VSE PRAVILNO");
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось получить список FileEntity", e);
        }
    }

    // TODO: реализовать findById, update, deleteById при необходимости
}