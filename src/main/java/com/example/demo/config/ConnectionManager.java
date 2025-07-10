package com.example.demo.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {

    private static final String URL_KEY = "datasource.url";
    private static final String USERNAME_KEY = "datasource.username";
    private static final String PASSWORD_KEY = "datasource.password";

    public static Connection open(){
        try {
            return DriverManager.getConnection(DatabaseConfig.get(URL_KEY),
                                               DatabaseConfig.get(USERNAME_KEY),
                                               DatabaseConfig.get(PASSWORD_KEY));
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось настроить соединение с базой данных" + e);
        }
    }

}
