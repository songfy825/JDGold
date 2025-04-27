package main.com.songfy.service;

import main.com.songfy.pojo.GoldSummary;
import main.com.songfy.pojo.GoldTransaction;

import java.time.LocalDateTime;
import java.util.List;

public interface GoldService {


    void buyGold(double totalCost, double quantity);
    void buyGold(double totalCost, double quantity, LocalDateTime createTime);

    int sellGold(int id, double soldQuantity, double soldPrice);

    List<GoldTransaction> queryRemainTransaction();

    List<GoldTransaction> querySoldTransaction();

    GoldTransaction queryTransactionById(int id);

    GoldSummary querySummary(GoldSummary goldSummary, double currentGoldPrice);

    List<GoldTransaction> queryAllTransaction();

    void updateRemainProfit(double currentGoldPrice);

    Double fetchCurrentGoldPrice() throws Exception;

    int restoreTransaction(int id);

    void deleteTransactionById(int id);

    List<GoldTransaction> sortTransactions(boolean isSold, String columnName, boolean ascending);

    void sellGold(double soldQuantity,double soldPrice);

    void addTransaction(String transactionFilePath);

    void deleteAllTransaction();
}
