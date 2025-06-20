package com.liqi.inventory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        JLabel userLabel = new JLabel("ユーザー名:");
        usernameField = new JTextField(15);
        JLabel passLabel = new JLabel("パスワード:");
        passwordField = new JPasswordField(15);
        JButton loginButton = new JButton("ログイン");
        gbc.gridx = 0; gbc.gridy = 0;
        add(userLabel, gbc);
        gbc.gridx = 1;
        add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        add(passLabel, gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        add(loginButton, gbc);
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
    }
    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "ログイン成功！");
                mainFrame.enableMenu();
                mainFrame.showPartsPanel();
            } else {
                JOptionPane.showMessageDialog(this, "ユーザー名またはパスワードが間違っています。");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void clearFields() {
        passwordField.setText("");
    }
}
