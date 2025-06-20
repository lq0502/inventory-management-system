package com.liqi.inventory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TransactionsPanel extends JPanel {
    private final JComboBox<String> partComboBox;
    private final JTextField quantityField;
    private final DefaultTableModel tableModel;

    public TransactionsPanel() {
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout());
        partComboBox = new JComboBox<>();
        loadPartsIntoComboBox();
        quantityField = new JTextField(5);
        JButton inButton = new JButton("入庫");
        JButton outButton = new JButton("出庫");
        topPanel.add(new JLabel("部品:"));
        topPanel.add(partComboBox);
        topPanel.add(new JLabel("数量:"));
        topPanel.add(quantityField);
        topPanel.add(inButton);
        topPanel.add(outButton);
        add(topPanel, BorderLayout.NORTH);
        tableModel = new DefaultTableModel(new Object[]{"ID", "部品名", "数量", "種類", "日付"}, 0);
        JTable transactionTable = new JTable(tableModel);
        loadTransactions();
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        add(scrollPane, BorderLayout.CENTER);
        inButton.addActionListener(e -> addTransaction("in"));
        outButton.addActionListener(e -> addTransaction("out"));
    }
    private void loadPartsIntoComboBox() {
        partComboBox.removeAllItems();
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT id, part_name FROM parts";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("part_name");
                partComboBox.addItem(id + ":" + name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void loadTransactions() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT t.id, p.part_name, t.quantity, t.type, t.transaction_date " +
                    "FROM transactions t JOIN parts p ON t.part_id = p.id ORDER BY t.transaction_date DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("part_name"),
                        rs.getInt("quantity"),
                        rs.getString("type"),
                        rs.getString("transaction_date")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void addTransaction(String type) {
        String selected = (String) partComboBox.getSelectedItem();
        if (selected == null || quantityField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "部品と数量を入力してください。");
            return;
        }
        int partId = Integer.parseInt(selected.split(":")[0]);
        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "数量は数字で入力してください。");
            return;
        }
        if (quantity <= 0) {
            JOptionPane.showMessageDialog(this, "数量は1以上で入力してください。");
            return;
        }
        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);
            String updateSql = type.equals("in") ?
                    "UPDATE parts SET quantity = quantity + ?, updated_at=datetime('now') WHERE id=?" :
                    "UPDATE parts SET quantity = quantity - ?, updated_at=datetime('now') WHERE id=?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, quantity);
            updateStmt.setInt(2, partId);
            updateStmt.executeUpdate();
            String insertSql = "INSERT INTO transactions (part_id, quantity, type, transaction_date) VALUES (?, ?, ?, datetime('now'))";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, partId);
            insertStmt.setInt(2, quantity);
            insertStmt.setString(3, type);
            insertStmt.executeUpdate();
            conn.commit();
            loadTransactions();
            quantityField.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void refreshPartsList() {
        loadPartsIntoComboBox();
    }
}

