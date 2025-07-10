package com.example.demo.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {

    public static final Properties PROPERTIES = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties(){
        try (InputStream inputStream = DatabaseConfig.class.getClassLoader().
                getResourceAsStream("application.properties")){
            PROPERTIES.load(inputStream);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static String get(String key){
        return PROPERTIES.getProperty(key);
    }
}
