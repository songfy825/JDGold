import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class GoldTradingApp {
    private JFrame frame;
    private JLabel currentPriceLabel;
    private DefaultListModel<GoldTransaction> transactionListModel;
    private JList<GoldTransaction> transactionList;
    private JButton refreshPriceButton;
    private JLabel profitLabel;
    private JLabel allSoldProfitLabel;

    // 当前金价
    private double currentGoldPrice = 0;
    private TransactionManager transactionManager;
    public GoldTradingApp() {
        // 初始化组件
        transactionManager = new TransactionManager();
        initialize();
    }

    private void initialize() {
        // 主窗口
        frame = new JFrame("黄金交易管理系统");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // 布局
        JPanel panel = new JPanel(new BorderLayout());

        // 当前金价部分
        JPanel pricePanel = new JPanel(new FlowLayout());
        pricePanel.add(new JLabel("当前金价："));
        currentPriceLabel = new JLabel("未获取");
        pricePanel.add(currentPriceLabel);
        refreshPriceButton = new JButton("刷新金价");
        pricePanel.add(refreshPriceButton);
        panel.add(pricePanel, BorderLayout.NORTH);

        // 刷新金价按钮事件
        refreshPriceButton.addActionListener(this::refreshGoldPrice);
        // 定时器设置，每隔 5 秒自动刷新金价
        Timer timer = new Timer(1000, this::refreshGoldPrice);
        timer.start();
        // 交易记录部分
        JPanel listPanel = new JPanel(new BorderLayout());
        transactionListModel = new DefaultListModel<>();
        transactionList = new JList<>(transactionListModel);
        JScrollPane scrollPane = new JScrollPane(transactionList);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(listPanel, BorderLayout.CENTER);
        // 添加右键菜单
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem markAsSoldItem = new JMenuItem("标记为已卖出");
        markAsSoldItem.addActionListener(e -> {
            int selectedIndex = transactionList.getSelectedIndex();
            if (selectedIndex != -1) {
                markAsSold(selectedIndex);
            }
        });
        JMenuItem cancelSoldItem = new JMenuItem("取消卖出");
        cancelSoldItem.addActionListener(e -> {
            int selectedIndex = transactionList.getSelectedIndex();
            if (selectedIndex != -1) {
                GoldTransaction goldTransaction = transactionListModel.get(selectedIndex);
                goldTransaction.isSold = false;
                transactionListModel.set(selectedIndex, goldTransaction); // 更新列表显示
                updateProfit(); // 更新盈利
            }
        });
        popupMenu.add(markAsSoldItem);
        popupMenu.add(cancelSoldItem);
        transactionList.setComponentPopupMenu(popupMenu);

        // 添加交易记录按钮
        JPanel addButtonPanel = new JPanel(new FlowLayout());
        JButton addTransactionButton = new JButton("添加交易记录");
        addButtonPanel.add(addTransactionButton);
        panel.add(addButtonPanel, BorderLayout.SOUTH);

        // 添加交易记录按钮事件
        addTransactionButton.addActionListener(this::addTransaction);

        // 盈利部分
        JPanel profitPanel = new JPanel(new BorderLayout());
        profitPanel.add(new JLabel("当前总盈利："), BorderLayout.NORTH);
        profitLabel = new JLabel("0.00 元");
        profitPanel.add(profitLabel, BorderLayout.CENTER);

        // 均价部分
        JPanel averagePricePanel = new JPanel(new BorderLayout());
        averagePricePanel.add(new JLabel("平均买入价："), BorderLayout.NORTH);
        JLabel averagePriceLabel = new JLabel("0.00 元/g");
        averagePricePanel.add(averagePriceLabel, BorderLayout.CENTER);

        profitPanel.add(averagePricePanel, BorderLayout.SOUTH);
        panel.add(profitPanel, BorderLayout.EAST);

        // 全部卖出总盈利部分
        JPanel allSoldProfitPanel = new JPanel(new BorderLayout());
        allSoldProfitPanel.add(new JLabel("全部卖出总盈利："), BorderLayout.NORTH);
        allSoldProfitLabel = new JLabel("0.00 元");
        allSoldProfitPanel.add(allSoldProfitLabel, BorderLayout.CENTER);

        profitPanel.add(averagePricePanel, BorderLayout.CENTER);
        profitPanel.add(allSoldProfitPanel, BorderLayout.SOUTH);
        panel.add(profitPanel, BorderLayout.EAST);

        // 更新均价显示
        transactionListModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                updateAveragePriceLabel(averagePriceLabel);
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                updateAveragePriceLabel(averagePriceLabel);
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                updateAveragePriceLabel(averagePriceLabel);
            }
        });
        // 加载交易记录
        loadTransactions();
        // 设置主面板
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
    private void loadTransactions() {
        java.util.List<GoldTransaction> goldTransaction = transactionManager.loadTransactions();
        for (GoldTransaction transaction : goldTransaction) {
            transactionListModel.addElement(transaction);
        }
        updateProfit(); // 更新盈利
    }
    private double calculateTotalProfitIfAllSold() {
        double totalProfit = 0;
        for (int i = 0; i < transactionListModel.size(); i++) {
            GoldTransaction goldTransaction = transactionListModel.get(i);
            double sellPrice = currentGoldPrice * (1 - 0.004); // 减去卖出手续费
            totalProfit += (sellPrice - goldTransaction.buyPrice) * goldTransaction.quantity;
        }
        return totalProfit;
    }

    private void updateAllSoldProfitLabel(JLabel allSoldProfitLabel) {
        double totalProfitIfAllSold = calculateTotalProfitIfAllSold();
        allSoldProfitLabel.setText(String.format("%.2f 元", totalProfitIfAllSold));
    }

    private void updateAveragePriceLabel(JLabel averagePriceLabel) {
        double averageBuyPrice = calculateAverageBuyPrice();
        averagePriceLabel.setText(String.format("%.2f 元/g", averageBuyPrice));
    }

    // 刷新金价
    private void refreshGoldPrice(ActionEvent e) {
        try {
            currentGoldPrice = fetchCurrentGoldPrice();
            currentPriceLabel.setText(String.format("%.2f 元/g", currentGoldPrice));
            updateProfit(); // 更新盈利
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "获取金价失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }


    // 获取当前金价（模拟 API 调用）
    private double fetchCurrentGoldPrice() throws Exception {
        // 这里可以替换为实际的 API 调用逻辑
        return PriceFetcher.fetchCurrentGoldPrice(); // 模拟返回的金价
    }

    // 计算平均买入价
    private double calculateAverageBuyPrice() {
        double totalCost = 0;
        double totalQuantity = 0;
        for (int i = 0; i < transactionListModel.size(); i++) {
            GoldTransaction goldTransaction = transactionListModel.get(i);
            totalCost += goldTransaction.totalCost;
            totalQuantity += goldTransaction.quantity;
        }
        return totalQuantity == 0 ? 0 : totalCost / totalQuantity;
    }


    // 添加交易记录
    private void addTransaction(ActionEvent e) {
        String totalCostInput = JOptionPane.showInputDialog(frame, "请输入总花费金额（元）：");
        String quantityInput = JOptionPane.showInputDialog(frame, "请输入购买重量（g）：");

        try {
            double totalCost = Double.parseDouble(totalCostInput);
            double quantity = Double.parseDouble(quantityInput);
            if (quantity <= 0) {
                throw new NumberFormatException("重量必须大于 0！");
            }
            GoldTransaction goldTransaction = new GoldTransaction(totalCost, quantity);
            transactionListModel.addElement(goldTransaction);
            updateProfit(); // 更新盈利
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "输入无效：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 更新盈利
    private void updateProfit() {
        double totalProfit = 0;
        for (int i = 0; i < transactionListModel.size(); i++) {
            GoldTransaction goldTransaction = transactionListModel.get(i);
            totalProfit += goldTransaction.calculateProfit(currentGoldPrice);
        }
        profitLabel.setText(String.format("%.2f 元", totalProfit));
        updateAllSoldProfitLabel(allSoldProfitLabel); // 更新全部卖出总盈利标签
    }

    // 标记某条记录为已卖出
    private void markAsSold(int index) {
        GoldTransaction goldTransaction = transactionListModel.get(index);
        goldTransaction.isSold = true;
        transactionListModel.set(index, goldTransaction); // 更新列表显示
        updateProfit(); // 更新盈利
        saveTransactions(); // 保存交易记录到文件
    }
    private void saveTransactions() {
        java.util.List<GoldTransaction> transactions = new ArrayList<>();
        for (int i = 0; i < transactionListModel.size(); i++) {
            transactions.add(transactionListModel.get(i));
        }
        transactionManager.saveTransactions(transactions);
    }
//    public static void main(String[] args) throws Exception {
////        EventQueue.invokeLater(() -> new GoldTradingApp());
//        System.out.println("Hello World!");
//    }
}