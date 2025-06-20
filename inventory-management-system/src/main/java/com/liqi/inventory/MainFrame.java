package com.liqi.inventory;
import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final LoginPanel loginPanel;
    private final PartsPanel partsPanel;
    private final TransactionsPanel transactionsPanel;
    private final JButton menuButton;
    private final JPanel bottomPanel;
    private final JPanel leftPanel;
    private final JLabel titleLabel;

    public MainFrame() {
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        loginPanel = new LoginPanel(this);
        partsPanel = new PartsPanel();
        transactionsPanel = new TransactionsPanel();
        TransactionsSearchPanel transactionsSearchPanel = new TransactionsSearchPanel();
        InventoryChartPanel inventoryChartPanel = new InventoryChartPanel();
        cardPanel.add(loginPanel, "login");
        cardPanel.add(partsPanel, "parts");
        cardPanel.add(transactionsPanel, "transactions");
        cardPanel.add(transactionsSearchPanel, "transactionsSearch");
        cardPanel.add(inventoryChartPanel, "inventoryChart");
        JPanel mainContainer = new JPanel();
        mainContainer.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 4));
        mainContainer.setBackground(new Color(240, 240, 240));
        mainContainer.setLayout(new BorderLayout());
        JPanel topBar = new JPanel();
        topBar.setBackground(new Color(173, 216, 230));
        topBar.setPreferredSize(new Dimension(0, 50));
        topBar.setLayout(new BorderLayout());
        titleLabel = new JLabel("在庫管理システム");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.DARK_GRAY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        topBar.add(titleLabel, BorderLayout.CENTER);
        mainContainer.add(topBar, BorderLayout.NORTH);
        mainContainer.add(cardPanel, BorderLayout.CENTER);
        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setPreferredSize(new Dimension(0, 50));
        bottomPanel.setBackground(new Color(240, 240, 240));
        mainContainer.add(bottomPanel, BorderLayout.SOUTH);
        menuButton = new JButton("メニュー");
        menuButton.setFocusPainted(false);
        menuButton.setPreferredSize(new Dimension(111, 36));
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem partsItem = new JMenuItem("部品管理");
        partsItem.addActionListener(e -> showPartsPanel());
        popupMenu.add(partsItem);
        JMenuItem transactionsItem = new JMenuItem("入出庫管理");
        transactionsItem.addActionListener(e -> showTransactionsPanel());
        popupMenu.add(transactionsItem);
        JMenuItem searchItem = new JMenuItem("履歴検索");
        searchItem.addActionListener(e -> showTransactionsSearchPanel());
        popupMenu.add(searchItem);
        JMenuItem chartItem = new JMenuItem("在庫分析");
        chartItem.addActionListener(e -> showInventoryChartPanel());
        popupMenu.add(chartItem);
        popupMenu.addSeparator();
        JMenuItem logoutItem = new JMenuItem("ログアウト");
        logoutItem.addActionListener(e -> showLoginPanel());
        popupMenu.add(logoutItem);
        menuButton.addActionListener(e -> {
            popupMenu.show(menuButton, 0, -popupMenu.getPreferredSize().height);
        });
        leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftPanel.add(menuButton);
        setContentPane(mainContainer);
    }
    public void showLoginPanel() {
        disableMenu();
        loginPanel.clearFields();
        titleLabel.setText("在庫管理システム");
        cardLayout.show(cardPanel, "login");
    }
    public void showPartsPanel() {
        partsPanel.refreshParts();
        titleLabel.setText("部品管理");
        cardLayout.show(cardPanel, "parts");
    }
    public void showTransactionsPanel() {
        titleLabel.setText("入出庫管理");
        transactionsPanel.refreshPartsList();
        cardLayout.show(cardPanel, "transactions");
    }
    public void showTransactionsSearchPanel() {
        titleLabel.setText("履歴検索");
        cardLayout.show(cardPanel, "transactionsSearch");
    }
    public void showInventoryChartPanel() {
        titleLabel.setText("在庫分析");
        cardLayout.show(cardPanel, "inventoryChart");
    }
    public void enableMenu() {
        bottomPanel.add(leftPanel, BorderLayout.WEST);
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }
    public void disableMenu() {
        bottomPanel.remove(leftPanel);
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
