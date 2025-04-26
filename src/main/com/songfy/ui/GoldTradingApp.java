package main.com.songfy.ui;

import lombok.extern.slf4j.Slf4j;
import main.com.songfy.controller.GoldController;
import main.com.songfy.pojo.GoldSummary;
import main.com.songfy.pojo.GoldTransaction;
import main.com.songfy.pojo.Result;

import javax.swing.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

@Slf4j
public class GoldTradingApp {
    private JFrame frame;
    private JLabel currentPriceLabel;

    private TransactionTableModel soldTransactionTableModel;
    private TransactionTableModel remainTransactionTableModel;
    private JTable soldTransactionTable;
    private JTable remainTransactionTable;

    private JButton refreshButton;
    private final GoldController goldController;
    private final GoldSummary goldSummary;
    private Double currentGoldPrice;
    private JLabel profitLabel;
    private JLabel averagePriceLabel;
    private JLabel avgProfitLabel;
    private JLabel totalQuantityLabel;

    public GoldTradingApp() {
        // 初始化组件
        goldController = new GoldController();
        goldSummary = new GoldSummary();
        goldController.initDatabase();
        initialize();
    }

    private void initialize() {
        // 主窗口
        frame = new JFrame("黄金交易管理系统");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // 布局
        JPanel panel = new JPanel(new BorderLayout());

        // 当前金价部分
        JPanel pricePanel = new JPanel(new FlowLayout());
        pricePanel.add(new JLabel("当前金价："));
        currentPriceLabel = new JLabel("未获取");
        pricePanel.add(currentPriceLabel);
        refreshButton = new JButton("刷新金价");
        pricePanel.add(refreshButton);
        panel.add(pricePanel, BorderLayout.NORTH);

        refreshButton.addActionListener(this::refreshGoldPrice);
        Timer timer = new Timer(1000, this::refreshGoldPrice);
        timer.start();

        // 交易记录部分
        JTabbedPane tabbedPane = new JTabbedPane();

        // 未卖出金记录部分
        JPanel unsoldPanel = new JPanel(new BorderLayout());
        remainTransactionTableModel = new TransactionTableModel(new ArrayList<>());
        remainTransactionTable = new JTable(remainTransactionTableModel);
        TableRowSorter<TransactionTableModel> remainSorter = new TableRowSorter<>(remainTransactionTableModel);
        JScrollPane unsoldScrollPane = new JScrollPane(remainTransactionTable);
        unsoldPanel.add(unsoldScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("未卖出金记录", unsoldPanel);

        // 已卖出金记录部分
        JPanel soldPanel = new JPanel(new BorderLayout());
        soldTransactionTableModel = new TransactionTableModel(new ArrayList<>());
        soldTransactionTable = new JTable(soldTransactionTableModel);
        TableRowSorter<TransactionTableModel> soldSorter = new TableRowSorter<>(soldTransactionTableModel);
        JScrollPane soldScrollPane = new JScrollPane(soldTransactionTable);
        soldPanel.add(soldScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("已卖出金记录", soldPanel);

        // 排序监听
        remainSorter.addRowSorterListener(e -> {
            if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
                int sortColumnIndex = remainSorter.getSortKeys().get(0).getColumn();
                String columnName = remainTransactionTableModel.getColumnName(sortColumnIndex);
                boolean ascending = remainSorter.getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING;
                sortTransactions(false, columnName, ascending);
            }
        });

        soldSorter.addRowSorterListener(e -> {
            if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
                int sortColumnIndex = soldSorter.getSortKeys().get(0).getColumn();
                String columnName = soldTransactionTableModel.getColumnName(sortColumnIndex);
                boolean ascending = soldSorter.getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING;
                sortTransactions(true, columnName, ascending);
            }
        });
        // 添加右键菜单
        JPopupMenu popupSoldMenu = new JPopupMenu();
        JPopupMenu popupRemainSoldMenu = new JPopupMenu();
        JMenuItem toSoldItem = new JMenuItem("卖出");
        toSoldItem.addActionListener(e -> {
            int selectedIndex = remainTransactionTable.getSelectedRow();
            if (selectedIndex != -1) {
                markAsSold(selectedIndex);
            }
        });
        JMenuItem cancelSoldItem = new JMenuItem("取消卖出");
        cancelSoldItem.addActionListener(e -> {
            int selectedIndex = soldTransactionTable.getSelectedRow();
            if (selectedIndex != -1) {
                cancelSold(selectedIndex);
            }
        });
        JMenuItem deleteSoldItem = new JMenuItem("删除记录");
        deleteSoldItem.addActionListener(e -> {
            int selectedIndex = soldTransactionTable.getSelectedRow();
            if (selectedIndex != -1)
                deleteTransaction(soldTransactionTable, selectedIndex);

        });
        JMenuItem deleteRemainItem = new JMenuItem("删除记录");
        deleteRemainItem.addActionListener(e -> {
            int selectedIndex = remainTransactionTable.getSelectedRow();
            if (selectedIndex != -1)
                deleteTransaction(remainTransactionTable, selectedIndex);

        });
        popupSoldMenu.add(cancelSoldItem);
        popupSoldMenu.add(deleteSoldItem);
        popupRemainSoldMenu.add(toSoldItem);
        popupRemainSoldMenu.add(deleteRemainItem);
        remainTransactionTable.setComponentPopupMenu(popupRemainSoldMenu);
        soldTransactionTable.setComponentPopupMenu(popupSoldMenu);

        // 添加交易记录按钮
        JPanel addButtonPanel = new JPanel(new FlowLayout());
        JButton addTransactionButton = new JButton("添加交易记录");
        addButtonPanel.add(addTransactionButton);
        panel.add(addButtonPanel, BorderLayout.SOUTH);

        // 添加交易记录按钮事件
        addTransactionButton.addActionListener(this::addTransaction);

        // 盈利部分
        JPanel profitPanel = new JPanel();
        profitPanel.setLayout(new BoxLayout(profitPanel, BoxLayout.Y_AXIS));

        // 当前总盈利部分
        JPanel profitPanelInner = new JPanel(new BorderLayout());
        profitPanelInner.add(new JLabel("当前总盈利："), BorderLayout.NORTH);
        profitLabel = new JLabel("0.00 元");
        profitPanelInner.add(profitLabel, BorderLayout.CENTER);
        profitPanel.add(profitPanelInner);

        // 均价部分
        JPanel averagePricePanelInner = new JPanel(new BorderLayout());
        averagePricePanelInner.add(new JLabel("平均买入价："), BorderLayout.NORTH);
        averagePriceLabel = new JLabel("0.00 元/g");
        averagePricePanelInner.add(averagePriceLabel, BorderLayout.CENTER);
        profitPanel.add(averagePricePanelInner);

        // 持有金数量部分
        JPanel totalQuantityPanelInner = new JPanel(new BorderLayout());
        totalQuantityPanelInner.add(new JLabel("持有金数量："), BorderLayout.NORTH);
        totalQuantityLabel = new JLabel("0.00 g");
        totalQuantityPanelInner.add(totalQuantityLabel, BorderLayout.CENTER);
        profitPanel.add(totalQuantityPanelInner);

        // 平均盈利部分
        JPanel avgProfitPanelInner = new JPanel(new BorderLayout());
        avgProfitPanelInner.add(new JLabel("均价总盈利："), BorderLayout.NORTH);
        avgProfitLabel = new JLabel("0.00 元");
        avgProfitPanelInner.add(avgProfitLabel, BorderLayout.CENTER);
        profitPanel.add(avgProfitPanelInner);

        panel.add(profitPanel, BorderLayout.EAST);

        // 加载交易记录
        updateTransactions();
        // 设置主面板
        panel.add(tabbedPane, BorderLayout.CENTER);
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }


    private void updateTransactions() {
        Result result = goldController.queryAllTransaction();
        if (result.getCode() == 1) {
            @SuppressWarnings("unchecked")
            java.util.List<GoldTransaction> goldTransactions = (java.util.List<GoldTransaction>) result.getData();
            java.util.List<GoldTransaction> remainTransactions = new ArrayList<>();
            java.util.List<GoldTransaction> soldTransactions = new ArrayList<>();
            for (GoldTransaction transaction : goldTransactions) {
                if (transaction.getIsSold()) {
                    soldTransactions.add(transaction);
                } else {
                    remainTransactions.add(transaction);
                }
            }
            remainTransactionTableModel.setTransactions(remainTransactions);
            soldTransactionTableModel.setTransactions(soldTransactions);
        } else {
            JOptionPane.showMessageDialog(frame, "加载交易记录失败：" + result.getMsg(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSummaryUI(GoldSummary goldSummary) {
        if (goldSummary != null) {
            profitLabel.setText(String.format("%.2f 元", goldSummary.getAccumulatedProfit()));
            averagePriceLabel.setText(String.format("%.2f 元/g", goldSummary.getAvgPrice()));
            totalQuantityLabel.setText(String.format("%.2f g", goldSummary.getTotalQuantity()));
            avgProfitLabel.setText(String.format("%.2f 元", goldSummary.getAvgProfit()));
        }
    }

    // 刷新金价
    private void refreshGoldPrice(ActionEvent e) {
        try {
            // 获取数据并进行类型转换
            Object data = this.goldController.getCurrentGoldPrice().getData();
            if (data instanceof Double) {
                currentGoldPrice = (Double) data;
                currentPriceLabel.setText(String.format("%.2f 元/g", currentGoldPrice));
                goldController.querySummary(goldSummary, currentGoldPrice);
                updateSummaryUI(goldSummary);
            } else {
                JOptionPane.showMessageDialog(frame, "获取的金价数据类型不正确", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "获取金价失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }


    // 添加交易记录
    private void addTransaction(ActionEvent e) {
        // 创建一个 JPanel 来容纳两个输入框
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JLabel totalCostLabel = new JLabel("请输入总花费金额（元）：");
        JTextField totalCostField = new JTextField();
        JLabel quantityLabel = new JLabel("请输入购买重量（g）：");
        JTextField quantityField = new JTextField();

        panel.add(totalCostLabel);
        panel.add(totalCostField);
        panel.add(quantityLabel);
        panel.add(quantityField);
        int result = JOptionPane.showConfirmDialog(frame, panel, "添加交易记录", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double totalCost = Double.parseDouble(totalCostField.getText());
                double quantity = Double.parseDouble(quantityField.getText());
                if (quantity < 0) {
                    throw new NumberFormatException("重量必须大于 0！");
                }
                goldController.buyGold(totalCost, quantity);
                updateTransactions();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "输入无效：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // 标记某条记录为已卖出
    private void markAsSold(Integer index) {
        GoldTransaction goldTransaction = remainTransactionTableModel.getTransactions().get(index);
        try {
            String quantityInput = JOptionPane.showInputDialog(frame, "请输入卖出重量（g）：");
            double sellQuantity = Double.parseDouble(quantityInput);
            if (sellQuantity < 0 || sellQuantity > goldTransaction.getQuantity()) {
                throw new NumberFormatException("重量输入有误！");
            }
            goldController.sellGold(goldTransaction.getId(), sellQuantity, currentGoldPrice);
            updateTransactions();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "输入无效：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }


    }

    // 取消卖出某条记录
    private void cancelSold(Integer index) {
        // 做的就是个简便版的交易记录系统，所以不考虑取消卖出后恢复到原来的记录上，就再创建一个新的交易记录
        GoldTransaction goldTransaction = soldTransactionTableModel.getTransactions().get(index);
        goldController.restoreTransaction(goldTransaction.getId());
        updateTransactions();
    }

    private void deleteTransaction(JTable table, int index) {
        GoldTransaction goldTransaction = null;
        if (table == remainTransactionTable) {
            goldTransaction = remainTransactionTableModel.getTransactions().get(index);
        } else if (table == soldTransactionTable) {
            goldTransaction = soldTransactionTableModel.getTransactions().get(index);
        }
        try {
            if (goldTransaction != null) {
                // 弹出框是否确认删除？
                if (JOptionPane.showConfirmDialog(frame, "是否确认删除？", "确认", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    goldController.deleteTransaction(goldTransaction.getId());
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "删除失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
        updateTransactions();
    }

    private void sortTransactions(boolean isSold, String columnName, boolean ascending) {
        Result result = goldController.sortTransactions(isSold, columnName, ascending);
        if (result.getCode() == 1) {
            @SuppressWarnings("unchecked")
            java.util.List<GoldTransaction> goldTransactions = (java.util.List<GoldTransaction>) result.getData();
            if (!isSold) {
                remainTransactionTableModel.setTransactions(goldTransactions);
            } else {
                soldTransactionTableModel.setTransactions(goldTransactions);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "排序失败：" + result.getMsg(), "错误", JOptionPane.ERROR_MESSAGE);
        }
        log.debug("根据{}排序成功", columnName);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new GoldTradingApp());
    }
}
