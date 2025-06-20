package com.liqi.inventory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

public class InventoryChartPanel extends JPanel {
    private JTable warningTable;
    private DefaultTableModel warningTableModel;
    public InventoryChartPanel() {
        setLayout(new BorderLayout());
        warningTableModel = new DefaultTableModel(new Object[]{"部品ID", "部品名", "数量", "閾値（ダブルクリックで編集できます）"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        //警告テーブル
        warningTable = new JTable(warningTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                int qty = Integer.parseInt(getValueAt(row, 2).toString());//数量
                int thres = Integer.parseInt(getValueAt(row, 3).toString());//閾値
                if (column == 2 && qty < thres) {
                    comp.setBackground(new Color(255, 200, 200));//警告色red
                } else {
                    comp.setBackground(Color.WHITE);
                }
                comp.setForeground(Color.BLACK);
                return comp;
            }
        };
        // 閾値が編集されたらDBに反映
        warningTableModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();
            if (column == 3) {
                int id = (int) warningTableModel.getValueAt(row, 0);
                int newThreshold;
                try {
                    newThreshold = Integer.parseInt(warningTableModel.getValueAt(row, 3).toString());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "整数で入力してください");
                    loadWarnings();
                    return;
                }
                //DBに閾値を更新
                try (Connection conn = DatabaseConnection.connect()) {
                    String sql = "UPDATE parts SET threshold = ? WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, newThreshold);
                    stmt.setInt(2, id);
                    stmt.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                loadWarnings();
            }
        });
        JScrollPane warningScroll = new JScrollPane(warningTable);
        warningScroll.setPreferredSize(new Dimension(0, 200));
        add(warningScroll, BorderLayout.NORTH);
        loadWarnings();
        //折れ線 円
        JPanel chartContainer = new JPanel(new GridLayout(1, 2));
        //折れ線
        JFreeChart lineChart = ChartFactory.createLineChart(
                "入出庫推移", "日付", "数量", createLineChartDataset(),
                PlotOrientation.VERTICAL, true, true, false
        );
        setJapaneseFont(lineChart);// フォント設定！！
        chartContainer.add(new ChartPanel(lineChart));// 左側に追加
        // 円グラフ
        JFreeChart pieChart = ChartFactory.createPieChart(
                "出庫ランキング", createOutPieDataset(), true, true, false
        );
        setJapaneseFont(pieChart);
        chartContainer.add(new ChartPanel(pieChart));
        add(chartContainer, BorderLayout.CENTER);
    }
    //partsからデータを読み、警告表に反映
    private void loadWarnings() {
        warningTableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT * FROM parts ORDER BY id ASC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            java.util.List<Object[]> warnings = new java.util.ArrayList<>();
            java.util.List<Object[]> normals = new java.util.ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("part_name");
                int qty = rs.getInt("quantity");
                int thres = rs.getInt("threshold");
                Object[] row = new Object[]{id, name, qty, thres};
                if (qty < thres) {
                    warnings.add(row);
                } else {
                    normals.add(row);
                }
            }
            for (Object[] row : warnings) warningTableModel.addRow(row);
            for (Object[] row : normals) warningTableModel.addRow(row);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //折れ線用のデータセットを構築
    private DefaultCategoryDataset createLineChartDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT date(transaction_date) AS 日付, type, SUM(quantity) AS 合計 " +
                    "FROM transactions GROUP BY 日付, type ORDER BY 日付 ASC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String date = rs.getString("日付");
                String type = rs.getString("type");
                int total = rs.getInt("合計");
                if (date != null && type != null) {
                    String label = type.equals("in") ? "入庫" : "出庫";
                    dataset.addValue(total, label, date);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataset;
    }
    //円グラフ用のデータセットを構築
    private PieDataset createOutPieDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT p.part_name, SUM(t.quantity) AS total_out " +
                    "FROM transactions t " +
                    "JOIN parts p ON t.part_id = p.id " +
                    "WHERE t.type = 'out' " +
                    "GROUP BY p.part_name " +
                    "ORDER BY total_out DESC " +
                    "LIMIT 10";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String partName = rs.getString("part_name");
                int totalOut = rs.getInt("total_out");
                dataset.setValue(partName, totalOut);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataset;
    }
    //グラフ用日本語フォント設定！！！！
    private void setJapaneseFont(JFreeChart chart) {
        Font font = new Font("Meiryo", Font.PLAIN, 12);
        chart.getTitle().setFont(font);
        if (chart.getLegend() != null) chart.getLegend().setItemFont(font);
        if (chart.getPlot() instanceof CategoryPlot cp) {
            cp.getDomainAxis().setLabelFont(font);
            cp.getDomainAxis().setTickLabelFont(font);
            cp.getRangeAxis().setLabelFont(font);
            cp.getRangeAxis().setTickLabelFont(font);
        } else if (chart.getPlot() instanceof PiePlot pp) {
            pp.setLabelFont(font);
        }
    }
}
