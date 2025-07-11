package com.example.demo.dao;

import com.example.demo.config.ConnectionManager;
import com.example.demo.entity.FileEntity;

import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;


public class FileEntityDao implements CrudDao {

    private static final String INSERT_FILE = """
    INSERT INTO files
      (orig_name, type, size_bytes)
    VALUES (?, ?, ?)
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
