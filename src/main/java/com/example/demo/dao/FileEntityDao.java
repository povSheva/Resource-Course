package com.example.demo.dao;

import com.example.demo.config.ConnectionManager;
import com.example.demo.entity.FileEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;


public class FileEntityDao implements CrudDao {

    private static final String INSERT_FILE = """
        INSERT INTO files
        (orig_name, type, size_bytes, added_at, updated_at)
        VALUES (?, ?, ?, NOW(), NOW())
      """;
    @Override
    public FileEntity save(FileEntity file) {
        try (Connection conn = ConnectionManager.open();
                PreparedStatement stmt = conn.prepareStatement(INSERT_FILE)) {

            stmt.setString(1, file.getOrigName());
            stmt.setString(2, file.getType());
            stmt.setLong(3, file.getSizeBytes());
            stmt.execute();

            file.setAddedAt(LocalDate.now());
            file.setUpdatedAt(LocalDate.now());

            return file;
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
