package com.example.demo.dao;

import com.example.demo.entity.FileEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FileEntityDao implements CrudDao {
    @Override
    public FileEntity save(FileEntity file) {
        return null;
    }

    @Override
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
    }
}
