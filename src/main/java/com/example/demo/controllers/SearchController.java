package com.example.demo.controllers;

import com.example.demo.entity.FileEntity;

public class SearchController {

        private final FilterController filter;

        public SearchController(FilterController filter) {
            this.filter = filter;
        }

        public void onSearch(String text) {
            filter.onSearchText(text);
        }

        /** проксируем добавление нового файла */
        public void onAddNew(FileEntity file) {
            filter.onAddNew(file);
        }
    }

