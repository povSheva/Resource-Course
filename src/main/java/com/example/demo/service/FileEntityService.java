package com.example.demo.service;

import com.example.demo.dao.FileEntityDao;
import com.example.demo.entity.FileEntity;

public class FileEntityService {

    private FileEntityDao fileEntityDao;

    public FileEntity save(FileEntity file){

        return fileEntityDao.save(file);
    }

    public FileEntityService(FileEntityDao fileEntityDao){

        this.fileEntityDao = fileEntityDao;
    }

}
