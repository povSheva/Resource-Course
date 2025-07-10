package com.example.demo.dao;


import com.example.demo.entity.FileEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CrudDao <E, ID> {
    FileEntity save(FileEntity file);

    Optional<FileEntity> findById(UUID uuid);

    List<FileEntity> findAll();

    void deleteById(UUID uuid);

    List<FileEntity> findByOrigNameLike(String keyword);

    List<FileEntity> findByType(String type);
}
