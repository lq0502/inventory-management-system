package com.liqi.inventory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import org.jdatepicker.impl.*;

public class TransactionsSearchPanel extends JPanel {
    private final JComboBox<String> partComboBox;
    private final JDatePickerImpl startDatePicker;
    private final JDatePickerImpl endDatePicker;
    private final DefaultTableModel tableModel;

    public TransactionsSearchPanel() {
        setLayout(new BorderLayout());
        JPanel filterPanel = new JPanel(new FlowLayout());
        partComboBox = new JComboBox<>();
        loadPartsIntoComboBox();
        startDatePicker = createDatePicker();
        endDatePicker = createDatePicker();
        JButton searchButton = new JButton("検索");
        filterPanel.add(new JLabel("部品:"));
        filterPanel.add(partComboBox);
        filterPanel.add(new JLabel("開始日:"));
        filterPanel.add(startDatePicker);
        filterPanel.add(new JLabel("終了日:"));
        filterPanel.add(endDatePicker);
        filterPanel.add(searchButton);
        add(filterPanel, BorderLayout.NORTH);
        tableModel = new DefaultTableModel(new Object[]{"ID", "部品名", "数量", "種類", "日付"}, 0);
        JTable transactionTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        add(scrollPane, BorderLayout.CENTER);
        searchButton.addActionListener(e -> searchTransactions());
    }
    private void loadPartsIntoComboBox() {
        partComboBox.removeAllItems();
        partComboBox.addItem("すべて");
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
    private void searchTransactions() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.connect()) {
            StringBuilder sql = new StringBuilder(
                    "SELECT t.id, p.part_name, t.quantity, t.type, t.transaction_date " +
                            "FROM transactions t JOIN parts p ON t.part_id = p.id WHERE 1=1 "
            );
            List<Object> params = new ArrayList<>();
            String selected = (String) partComboBox.getSelectedItem();
            if (selected != null && !selected.equals("すべて")) {
                int partId = Integer.parseInt(selected.split(":")[0]);
                sql.append(" AND t.part_id = ?");
                params.add(partId);
            }
            if (startDatePicker.getModel().getValue() != null) {
                sql.append(" AND t.transaction_date >= ?");
                params.add(getDateString(startDatePicker));
            }
            if (endDatePicker.getModel().getValue() != null) {
                sql.append(" AND t.transaction_date <= ?");
                params.add(getDateString(endDatePicker) + " 23:59:59");
            }
            sql.append(" ORDER BY t.transaction_date DESC");
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
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
    private JDatePickerImpl createDatePicker() {
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "今日");
        p.put("text.month", "月");
        p.put("text.year", "年");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        return new JDatePickerImpl(datePanel, new DateLabelFormatter());
    }
    private String getDateString(JDatePickerImpl picker) {
        java.util.Date selectedDate = (java.util.Date) picker.getModel().getValue();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(selectedDate);
    }
}