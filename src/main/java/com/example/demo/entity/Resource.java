package com.example.demo.entity;

import javafx.beans.property.SimpleStringProperty;

public class Resource {
    private final SimpleStringProperty name;
    private final SimpleStringProperty type;
    private final SimpleStringProperty date;

    public Resource(String name, String type, String date) {
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.date = new SimpleStringProperty(date);
    }

    public String getName() { return name.get(); }
    public String getType() { return type.get(); }
    public String getDate() { return date.get(); }
}

