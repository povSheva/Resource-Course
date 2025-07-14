package com.example.demo.dao;

import com.example.demo.config.ConnectionManager;
import com.example.demo.entity.FileEntity;

import java.sql.*;
import java.util.*;
import java.util.UUID;

/**
 * DAO для работы с таблицей {@code files}.
 * Добавлены методы findById и deleteById.
 */
public class FileEntityDao implements CrudDao<FileEntity> {

    private static final String INSERT_FILE = """
        INSERT INTO files
          (storage_key, orig_name, type, size_bytes)
        VALUES (?, ?, ?, ?)
        RETURNING uuid, added_at
        """;

    private static final String SELECT_ALL = """
        SELECT uuid,
               storage_key,
               orig_name,
               type,
               size_bytes,
               added_at
          FROM files
        """;

    private static final String SELECT_BY_ID = """
        SELECT uuid,
               storage_key,
               orig_name,
               type,
               size_bytes,
               added_at
          FROM files
         WHERE uuid = ?
        """;

    private static final String DELETE_BY_ID = """
        DELETE FROM files
         WHERE uuid = ?
        """;

    @Override
    public FileEntity save(FileEntity file) {
        try (Connection conn = ConnectionManager.open();
             PreparedStatement stmt = conn.prepareStatement(INSERT_FILE)) {

            stmt.setString(1, file.getStorageKey());
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
        List<FileEntity> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.open();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs   = stmt.executeQuery()) {

            while (rs.next()) {
                FileEntity f = new FileEntity();
                f.setUuid(rs.getObject("uuid", UUID.class));
                f.setStorageKey(rs.getString("storage_key"));
                f.setOrigName(rs.getString("orig_name"));
                f.setType(rs.getString("type"));
                f.setSizeBytes(rs.getLong("size_bytes"));
                f.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());
                list.add(f);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось получить список FileEntity", e);
        }
    }

    /**
     * Возвращает Optional<FileEntity> по UUID.
     */
    public Optional<FileEntity> findById(UUID uuid) {
        try (Connection conn = ConnectionManager.open();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setObject(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    FileEntity f = new FileEntity();
                    f.setUuid(rs.getObject("uuid", UUID.class));
                    f.setStorageKey(rs.getString("storage_key"));
                    f.setOrigName(rs.getString("orig_name"));
                    f.setType(rs.getString("type"));
                    f.setSizeBytes(rs.getLong("size_bytes"));
                    f.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());
                    return Optional.of(f);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось получить FileEntity по UUID", e);
        }
    }

    /**
     * Удаляет запись из таблицы files по UUID.
     */
    public void deleteById(UUID uuid) {
        try (Connection conn = ConnectionManager.open();
             PreparedStatement ps = conn.prepareStatement(DELETE_BY_ID)) {

            ps.setObject(1, uuid);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось удалить FileEntity по UUID", e);
        }
    }
}
