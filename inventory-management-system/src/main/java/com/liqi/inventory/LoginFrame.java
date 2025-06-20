package com.liqi.inventory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import com.formdev.flatlaf.FlatLightLaf;

//ログイン画面
public class LoginFrame extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    //ログイン画面初期化
    public LoginFrame() {
        setTitle("在庫管理システム - ログイン");
        setSize(400, 200);
        setLocationRelativeTo(null); // 窗口居中
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2, 10, 10));
        //入力フィールド
        JLabel userLabel = new JLabel("ユーザー名:");
        usernameField = new JTextField();
        JLabel passLabel = new JLabel("パスワード:");
        passwordField = new JPasswordField();
        JButton loginButton = new JButton("ログイン");
        add(userLabel);
        add(usernameField);
        add(passLabel);
        add(passwordField);
        add(new JLabel()); // 占位
        add(loginButton);
        
        //ログインボタンがクリックされた時
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
    }

    //ログイン処理を実行するメソッド
    private void handleLogin() {
        String username = usernameField.getText();// ユーザー名取得
        String password = new String(passwordField.getPassword());// パスワード取得
        try (Connection conn = DatabaseConnection.connect()) {
            //ユーザーが存在するか
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "ログイン成功！");
            } else {
                JOptionPane.showMessageDialog(this, "ユーザー名またはパスワードが間違っています。");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //エントリーポイント
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}
