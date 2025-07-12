package com.example.demo.dao;

import com.example.demo.config.ConnectionManager;
import com.example.demo.entity.FileMetadata;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO для работы с таблицей {@code file_metadata}.
 * Реализует только save(...) и findAll().
 */
public class FileMetadataDao implements CrudDao<FileMetadata> {

    /* SQL для file_metadata */
    private static final String INSERT_META = """
        INSERT INTO file_metadata(file_uuid, pinned, tag, additional_info)
        VALUES (?, ?, ?, ?)
        RETURNING updated_at
        """;

    private static final String SELECT_ALL_META = """
        SELECT file_uuid, updated_at, pinned, tag, additional_info
          FROM file_metadata
        """;

    @Override
    public FileMetadata save(FileMetadata meta) {
        try (Connection conn = ConnectionManager.open();
             PreparedStatement ps = conn.prepareStatement(INSERT_META)) {

            ps.setObject(1, meta.getFileUuid());
            ps.setBoolean(2, meta.isPinned());
            ps.setString(3, meta.getTag());
            ps.setString(4, meta.getAdditionalInfo());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    meta.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                }
            }
            return meta;
        } catch (SQLException ex) {
            throw new RuntimeException("Не удалось сохранить FileMetadata", ex);
        }
    }

    @Override
    public List<FileMetadata> findAll() {
        List<FileMetadata> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.open();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_META);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                FileMetadata m = new FileMetadata();
                m.setFileUuid(rs.getObject("file_uuid", UUID.class));
                m.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                m.setPinned(rs.getBoolean("pinned"));
                m.setTag(rs.getString("tag"));
                m.setAdditionalInfo(rs.getString("additional_info"));
                list.add(m);
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException("Не удалось получить список FileMetadata", ex);
        }
    }
}
