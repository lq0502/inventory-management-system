package com.liqi.inventory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:" + System.getProperty("user.dir") + "/inventory.db";
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            System.out.println("数据库连接成功！");
        } catch (SQLException e) {
            System.out.println("数据库连接失败: " + e.getMessage());
        }
        return conn;
    }
    public static void main(String[] args) {
        connect();
    }
}
