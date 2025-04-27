package main.com.songfy.controller;

import main.com.songfy.mapper.GoldMapper;
import main.com.songfy.pojo.GoldSummary;
import main.com.songfy.pojo.GoldTransaction;
import main.com.songfy.pojo.Result;
import main.com.songfy.service.GoldService;
import main.com.songfy.service.impl.GoldServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GoldController {
    private final GoldService goldService = new GoldServiceImpl();

    public Result buyGold(double totalCost, double quantity) {
        goldService.buyGold(totalCost, quantity);
        return Result.success();
    }

    public Result sellGold(int id, double soldQuantity, double soldPrice) {
        return goldService.sellGold(id, soldQuantity, soldPrice) == 0 ? Result.error("卖出克数有误") : Result.success();
    }

    public Result queryAllTransaction() {
        List<GoldTransaction> allTransaction = goldService.queryAllTransaction();
        return Result.success(allTransaction);
    }

    public Result queryRemainTransaction() {
        List<GoldTransaction> allTransaction = goldService.queryRemainTransaction();
        return Result.success(allTransaction);
    }

    public Result querySoldTransaction() {
        List<GoldTransaction> allTransaction = goldService.querySoldTransaction();
        return Result.success(allTransaction);
    }

    public Result querySummary(GoldSummary goldSummary, double currentGoldPrice) {
        return Result.success(goldService.querySummary(goldSummary, currentGoldPrice));
    }

    public Result updateRemainProfit(double currentGoldPrice) {
        goldService.updateRemainProfit(currentGoldPrice);
        return Result.success();
    }

    public Result initDatabase() {
        return GoldMapper.initializeDatabase() == 0 ? Result.error("初始化数据库失败") : Result.success();
    }

    public Result getCurrentGoldPrice() throws Exception {
        return Result.success(goldService.fetchCurrentGoldPrice());
    }

    public Result queryTransactionById(int id) {
        GoldTransaction transactionById = goldService.queryTransactionById(id);
        return transactionById == null ? Result.error("未找到该记录") : Result.success(transactionById);
    }

    public Result restoreTransaction(int id) {

        return goldService.restoreTransaction(id) == 0 ? Result.error("恢复失败") : Result.success();
    }

    public Result deleteTransaction(int id) {
        goldService.deleteTransactionById(id);
        return Result.success();
    }

    public Result sortTransactions(boolean isSold, String columnName, boolean ascending) {
        return Result.success(goldService.sortTransactions(isSold, columnName, ascending));
    }
    public Result sellGold( Double soldQuantity,Double soldPrice){
        goldService.sellGold(soldQuantity,soldPrice);
        return Result.success();
    }
}
