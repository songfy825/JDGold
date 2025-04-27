package main.com.songfy.ui;

import com.google.gson.JsonObject;
import lombok.Getter;
import main.com.songfy.pojo.GoldTransaction;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class TransactionTableModel extends AbstractTableModel {
//    private final String[] columnNames = {"ID", "totalCost", "quantity", "buyPrice", "soldPrice", "isSold", "profit", "updateTime"};
private final String[] columnNames = {"ID", "总消费", "克数", "买入单价", "卖出单价", "是否已卖出", "总盈利", "更新时间"};

    @Getter
    private List<GoldTransaction> transactions;

    public TransactionTableModel(List<GoldTransaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public int getRowCount() {
        return transactions.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        GoldTransaction transaction = transactions.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return transaction.getId();
            case 1:
                return transaction.getTotalCost();
            case 2:
                return transaction.getQuantity();
            case 3:
                return transaction.getBuyPrice();
            case 4:
                return transaction.getSoldPrice();
            case 5:
                return transaction.getIsSold();
            case 6:
                return transaction.getProfit();
            case 7:
                return transaction.getUpdateTime();
            default:
                return null;
        }
    }

    public void setTransactions(List<GoldTransaction> transactions) {
        this.transactions = transactions;
        fireTableDataChanged();
    }

}
