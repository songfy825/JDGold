package main.com.songfy.service.impl;

import com.google.gson.JsonParser;
import main.com.songfy.mapper.GoldMapper;
import main.com.songfy.misc.Config;
import main.com.songfy.pojo.GoldSummary;
import main.com.songfy.pojo.GoldTransaction;
import main.com.songfy.service.GoldService;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class GoldServiceImpl implements GoldService {
    private final GoldMapper goldMapper = new GoldMapper();

    @Override
    public void buyGold(double totalCost, double quantity) {
        synchronized (GoldServiceImpl.class) {
            GoldTransaction goldTransaction = new GoldTransaction(totalCost, quantity);
            goldMapper.insertTransaction(goldTransaction);
        }
    }

    @Override
    public int sellGold(int id, double soldQuantity, double soldPrice) {
        synchronized (GoldServiceImpl.class) {
            GoldTransaction goldTransaction = queryTransactionById(id);
            GoldTransaction soldGoldTransaction = new GoldTransaction(goldTransaction);
            // 卖金判断
            if (soldQuantity > goldTransaction.getQuantity()) {
                return 0;//失败
            }
            // 重量更新
            goldTransaction.setQuantity(goldTransaction.getQuantity() - soldQuantity);
            soldGoldTransaction.setQuantity(soldQuantity);
            // 总花费更新
            goldTransaction.setTotalCost(goldTransaction.getTotalCost() - goldTransaction.getBuyPrice() * soldQuantity);
            soldGoldTransaction.setTotalCost(soldPrice * soldQuantity);
            // 卖出价格更新
            soldGoldTransaction.setSoldPrice(soldPrice);
            soldGoldTransaction.setIsSold(true);
            soldGoldTransaction.setProfit(soldGoldTransaction.calculateProfit(soldPrice));

            goldTransaction.setUpdateTime(LocalDateTime.now());
            soldGoldTransaction.setUpdateTime(LocalDateTime.now());
            if (goldTransaction.getQuantity() == 0)
                goldMapper.deleteTransactionById(id);
            else
                goldMapper.updateTransactionById(goldTransaction, id);
            goldMapper.insertTransaction(soldGoldTransaction);
            return 1;
        }
    }

    @Override
    public List<GoldTransaction> queryRemainTransaction() {

        return goldMapper.queryAllTransaction().stream()
                .filter(goldTransaction -> !goldTransaction.getIsSold())
                .toList();
    }

    @Override
    public List<GoldTransaction> querySoldTransaction() {

        return goldMapper.queryAllTransaction().stream()
                .filter(GoldTransaction::getIsSold)
                .toList();
    }


    @Override
    public GoldTransaction queryTransactionById(int id) {
        return goldMapper.queryTransactionById(id);
    }

    @Override
    public GoldSummary querySummary(GoldSummary goldSummary, double currentGoldPrice) {
        double avgPrice = goldMapper.queryAvgPriceForRemain();
        double totalQuantity = goldMapper.queryTotalQuantityForRemain();
        double accumulatedProfit = goldMapper.queryAccumProfit();
        double avgProfit = totalQuantity * (currentGoldPrice - avgPrice);

        goldSummary.setAvgProfit(avgProfit);
        goldSummary.setAvgPrice(avgPrice);
        goldSummary.setAccumulatedProfit(accumulatedProfit);
        goldSummary.setTotalQuantity(totalQuantity);

        return goldSummary;
    }

    @Override
    public List<GoldTransaction> queryAllTransaction() {
        return goldMapper.queryAllTransaction();
    }

    @Override
    public void updateRemainProfit(double currentGoldPrice) {
        List<GoldTransaction> goldTransactions = queryRemainTransaction();
        goldTransactions.forEach(goldTransaction -> {
            goldTransaction.setProfit(goldTransaction.calculateProfit(currentGoldPrice));

            goldTransaction.setUpdateTime(LocalDateTime.now());
        });
        // 存入数据库
        goldTransactions.forEach(goldTransaction -> {
            goldMapper.updateTransactionById(goldTransaction, goldTransaction.getId());
        });
    }

    @Override
    public Double fetchCurrentGoldPrice() throws Exception {
        StringBuilder response = null;
        try {
            URL url = new URL(Config.API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setConnectTimeout(5000); // 5 秒
            conn.setReadTimeout(5000);    // 5 秒

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("API 请求失败，状态码：" + responseCode);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException(e);
        }
        String prices = JsonParser.parseString(response.toString())
                .getAsJsonObject().
                getAsJsonObject("resultData").
                getAsJsonObject("datas").
                get("price").getAsString();


        return Double.parseDouble(prices);
    }

    @Override
    public int restoreTransaction(int id) {
        GoldTransaction soldTransaction = queryTransactionById(id);
        if (soldTransaction != null) {
            soldTransaction.setIsSold(false);
            soldTransaction.setSoldPrice(0.0);
            soldTransaction.setProfit(0.0);
            soldTransaction.setUpdateTime(LocalDateTime.now());
            goldMapper.updateTransactionById(soldTransaction, id);
            return 1;
        }
        return 0;
    }

    @Override
    public void deleteTransactionById(int id) {
        goldMapper.deleteTransactionById(id);
        log.debug("删除数据成功");
    }

    @Override
    public List<GoldTransaction> sortTransactions(boolean isSold, String columnName, boolean ascending) {
        return goldMapper.querySortedTransactions(isSold, columnName, ascending);

    }
}

