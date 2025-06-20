package com.liqi.inventory;
// JDBC必要なクラス
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//データベースに接続するメソッド
public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:" + System.getProperty("user.dir") + "/inventory.db";
    public static Connection connect() {
        Connection conn = null;//変数を初期化
        try {
            //SQLiteに接続
            conn = DriverManager.getConnection(DB_URL);
            System.out.println("成功");
        } catch (SQLException e) {
            //デバッグ
            System.out.println("失败: " + e.getMessage());
        }
        return conn;
    }

    //テスト用......
    public static void main(String[] args) {
        connect();
    }
}
