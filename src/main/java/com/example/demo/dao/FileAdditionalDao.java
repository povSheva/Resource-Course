package com.example.demo.dao;

import com.example.demo.config.ConnectionManager;
import com.example.demo.entity.FileAdditionalEntity;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DAO для работы с таблицей {@code file_metadata}.
 * Теперь поддерживает UPSERT, поиск по file_uuid и удаление.
 */
public class FileAdditionalDao implements CrudDao<FileAdditionalEntity> {

    // UPSERT: вставка или обновление, возвращает новое updated_at
    private static final String UPSERT_META = """
        INSERT INTO file_metadata(file_uuid, pinned, tag, additional_info)
        VALUES (?, ?, ?, ?)
        ON CONFLICT (file_uuid) DO UPDATE
          SET pinned = EXCLUDED.pinned,
              tag   = EXCLUDED.tag,
              additional_info = EXCLUDED.additional_info
        RETURNING updated_at
        """;

    private static final String SELECT_ALL_META = """
        SELECT file_uuid, updated_at, pinned, tag, additional_info
          FROM file_metadata
        """;

    private static final String SELECT_BY_UUID = """
        SELECT file_uuid, updated_at, pinned, tag, additional_info
          FROM file_metadata
         WHERE file_uuid = ?
        """;

    private static final String DELETE_BY_UUID = """
        DELETE FROM file_metadata
         WHERE file_uuid = ?
        """;

    @Override
    public FileAdditionalEntity save(FileAdditionalEntity meta) {
        try (Connection conn = ConnectionManager.open();
             PreparedStatement ps = conn.prepareStatement(UPSERT_META)) {

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
            throw new RuntimeException("Не удалось сохранить FileMetadata (upsert)", ex);
        }
    }

    @Override
    public List<FileAdditionalEntity> findAll() {
        List<FileAdditionalEntity> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.open();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_META);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                FileAdditionalEntity m = new FileAdditionalEntity();
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

    /**
     * Ищет метаданные по UUID файла.
     * @return Optional.of(meta) если есть, иначе Optional.empty()
     */
    public Optional<FileAdditionalEntity> findByFileUuid(UUID fileUuid) {
        try (Connection conn = ConnectionManager.open();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_UUID)) {

            ps.setObject(1, fileUuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    FileAdditionalEntity m = new FileAdditionalEntity();
                    m.setFileUuid(rs.getObject("file_uuid", UUID.class));
                    m.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    m.setPinned(rs.getBoolean("pinned"));
                    m.setTag(rs.getString("tag"));
                    m.setAdditionalInfo(rs.getString("additional_info"));
                    return Optional.of(m);
                }
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new RuntimeException("Не удалось получить FileMetadata по UUID", ex);
        }
    }

    /**
     * Удаляет запись метаданных по UUID файла.
     */
    public void deleteByFileUuid(UUID fileUuid) {
        try (Connection conn = ConnectionManager.open();
             PreparedStatement ps = conn.prepareStatement(DELETE_BY_UUID)) {

            ps.setObject(1, fileUuid);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Не удалось удалить FileMetadata по UUID", ex);
        }
    }
}
