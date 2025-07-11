package com.example.demo.dao;

import com.example.demo.config.ConnectionManager;
import com.example.demo.entity.FileEntity;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class FileEntityDao implements CrudDao {

    private static final String INSERT_FILE = """
    INSERT INTO files
      (orig_name, type, size_bytes)
    VALUES (?, ?, ?)
    """;

    private static final String SELECT_ALL = """
    SELECT uuid,
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

            stmt.setString(1, file.getOrigName());
            stmt.setString(2, file.getType());
            stmt.setLong(3, file.getSizeBytes());

            int affected = stmt.executeUpdate();
            if (affected != 1) {
                throw new SQLException("Ожидалась вставка 1 строки, вставлено: " + affected);
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    // В PostgreSQL getGeneratedKeys() вернёт uuid, в других СУБД — только автогенерируемый ключ
                    UUID generatedUuid = rs.getObject(1, UUID.class);
                    file.setUuid(generatedUuid);
                }
            }

            return file;
        } catch (SQLException e) {
            e.printStackTrace();
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
                f.setOrigName(rs.getString("orig_name"));
                f.setType(rs.getString("type"));
                f.setSizeBytes(rs.getLong("size_bytes"));

                // СЧИТАЕМ added_at, а не created_at
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
            e.printStackTrace();
            throw new RuntimeException("Не удалось получить список FileEntity", e);
        }
    }

    /*@Override
    public Optional<FileEntity> findById(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public List<FileEntity> findAll() {
        return null;
    }

    @Override
    public void deleteById(UUID uuid) {

    }

    @Override
    public List<FileEntity> findByOrigNameLike(String keyword) {
        return null;
    }

    @Override
    public List<FileEntity> findByType(String type) {
        return null;
    }*/
}
