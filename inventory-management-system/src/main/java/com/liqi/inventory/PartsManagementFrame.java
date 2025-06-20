package com.liqi.inventory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import com.formdev.flatlaf.FlatLightLaf;

public class PartsManagementFrame extends JFrame {
    private final JTable partsTable;
    private final DefaultTableModel tableModel;
    public PartsManagementFrame() {
        setTitle("在庫管理システム - 部品管理");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tableModel = new DefaultTableModel(new Object[]{"ID", "部品名", "メーカー", "在庫数量", "価格"}, 0);
        partsTable = new JTable(tableModel);
        loadParts();
        JScrollPane scrollPane = new JScrollPane(partsTable);
        add(scrollPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("追加");
        JButton editButton = new JButton("編集");
        JButton deleteButton = new JButton("削除");
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);
        addButton.addActionListener(e -> addPart());
        editButton.addActionListener(e -> editPart());
        deleteButton.addActionListener(e -> deletePart());
    }
    //データベースから部品情報load
    private void loadParts() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT * FROM parts";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("part_name"),
                        rs.getString("manufacturer"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void addPart() {
        JTextField nameField = new JTextField();
        JTextField manufacturerField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField priceField = new JTextField();
        Object[] message = {
                "部品名:", nameField,
                "メーカー:", manufacturerField,
                "在庫数量:", quantityField,
                "価格:", priceField
        };
        int option = JOptionPane.showConfirmDialog(this, message, "部品追加", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.connect()) {
                String sql = "INSERT INTO parts (part_name, manufacturer, quantity, price, updated_at) VALUES (?, ?, ?, ?, datetime('now'))";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, nameField.getText());
                pstmt.setString(2, manufacturerField.getText());
                pstmt.setInt(3, Integer.parseInt(quantityField.getText()));
                pstmt.setDouble(4, Double.parseDouble(priceField.getText()));
                pstmt.executeUpdate();
                loadParts();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    private void editPart() {
        int selectedRow = partsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "編集する部品を選択してください。");
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String currentName = (String) tableModel.getValueAt(selectedRow, 1);
        String currentManufacturer = (String) tableModel.getValueAt(selectedRow, 2);
        int currentQuantity = (int) tableModel.getValueAt(selectedRow, 3);
        double currentPrice = (double) tableModel.getValueAt(selectedRow, 4);
        JTextField nameField = new JTextField(currentName);
        JTextField manufacturerField = new JTextField(currentManufacturer);
        JTextField quantityField = new JTextField(String.valueOf(currentQuantity));
        JTextField priceField = new JTextField(String.valueOf(currentPrice));
        Object[] message = {
                "部品名:", nameField,
                "メーカー:", manufacturerField,
                "在庫数量:", quantityField,
                "価格:", priceField
        };
        int option = JOptionPane.showConfirmDialog(this, message, "部品編集", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.connect()) {
                String sql = "UPDATE parts SET part_name=?, manufacturer=?, quantity=?, price=?, updated_at=datetime('now') WHERE id=?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, nameField.getText());
                pstmt.setString(2, manufacturerField.getText());
                pstmt.setInt(3, Integer.parseInt(quantityField.getText()));
                pstmt.setDouble(4, Double.parseDouble(priceField.getText()));
                pstmt.setInt(5, id);
                pstmt.executeUpdate();
                loadParts();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    private void deletePart() {
        int selectedRow = partsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "削除する部品を選択してください。");
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int option = JOptionPane.showConfirmDialog(this, "本当に削除しますか？", "確認", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.connect()) {
                String sql = "DELETE FROM parts WHERE id=?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadParts();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //テスト用エントリーポイント
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new PartsManagementFrame().setVisible(true));
    }
}
