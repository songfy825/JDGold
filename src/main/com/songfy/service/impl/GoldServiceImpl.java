package main.com.songfy.service.impl;

import com.google.gson.*;
import main.com.songfy.mapper.GoldMapper;
import main.com.songfy.misc.Config;
import main.com.songfy.pojo.GoldSummary;
import main.com.songfy.pojo.GoldTransaction;
import main.com.songfy.service.GoldService;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static main.com.songfy.misc.TransactionParser.parseTransactionsToJson;

@Slf4j
public class GoldServiceImpl implements GoldService {

    private GoldMapper goldMapper ;
    @Override
    public void changeBank(String bankName) {
        String bankNameMap = switch (bankName) {
            case "民生银行" -> "MS";
            case "浙商银行" -> "ZS";
            case "工商银行" -> "GS";
            default -> "";
        };
        goldMapper.setBankName(bankNameMap);
        goldMapper.initializeDatabase();
    }

    @Override
    public void buyGold(double totalCost, double quantity) {
        synchronized (GoldServiceImpl.class) {
            GoldTransaction goldTransaction = new GoldTransaction(totalCost, quantity);
            goldMapper.insertTransaction(goldTransaction);
        }
    }

    @Override
    public void buyGold(double totalCost, double quantity, LocalDateTime createTime) {

        GoldTransaction goldTransaction = new GoldTransaction(totalCost, quantity);
        goldTransaction.setCreateTime(createTime);
        goldMapper.insertTransaction(goldTransaction);

    }

    @Override
    public int sellGold(int id, double soldQuantity, double soldPrice) {
        GoldTransaction goldTransaction = queryTransactionById(id);
        GoldTransaction soldGoldTransaction = new GoldTransaction(goldTransaction);
        // 卖金判断
        if (soldQuantity > goldTransaction.getQuantity()) {
            return 0; // 失败
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
        soldGoldTransaction.setProfit(soldGoldTransaction.calculateProfit(soldPrice, goldMapper.getBankName()));
        goldTransaction.setCreateTime(LocalDateTime.now());
        soldGoldTransaction.setCreateTime(LocalDateTime.now());
        goldTransaction.setUpdateTime(LocalDateTime.now());
        soldGoldTransaction.setUpdateTime(LocalDateTime.now());

        if (goldTransaction.getQuantity() == 0) {
            goldMapper.deleteTransactionById(id);
        } else {
            goldMapper.updateTransactionById(goldTransaction, id);
        }
        goldMapper.insertTransaction(soldGoldTransaction);
        return 1;
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
            goldTransaction.setProfit(goldTransaction.calculateProfit(currentGoldPrice, goldMapper.getBankName()));

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
        String apiUrl = switch (goldMapper.getBankName()) {
            case "MS" -> Config.API_URL_MS;
            case "ZS" -> Config.API_URL_ZS;
            case "GS" -> Config.API_URL_GS;
            default -> "";
        };
        log.debug(apiUrl);
        try {
            URL url = new URL(apiUrl);
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

    @Override
    public void sellGold(double soldQuantity, double soldPrice) {
        // 获取未卖出的交易记录，按买入价格升序排列
        List<GoldTransaction> goldTransactions = goldMapper.querySortedTransactions(false, "buyPrice", true);
        double sumQuantity = 0.0;
        List<GoldTransaction> transactionsToSell = new ArrayList<>();

        for (GoldTransaction goldTransaction : goldTransactions) {
            if (sumQuantity + goldTransaction.getQuantity() < soldQuantity) {
                sumQuantity += goldTransaction.getQuantity();
                transactionsToSell.add(goldTransaction);
            } else {
                goldTransaction.setQuantity(soldQuantity - sumQuantity);
                transactionsToSell.add(goldTransaction);
                break;
            }
        }

        // 处理要卖出的交易记录
        for (GoldTransaction transaction : transactionsToSell) {
            double quantityToSell = transaction.getQuantity();
            if (quantityToSell > 0) {
                sellGold(transaction.getId(), quantityToSell, soldPrice);
            }
        }
    }

    @Override
    public void addTransaction(String transactionFilePath) {
        synchronized (GoldServiceImpl.class) {
            String jsonOutput = parseTransactionsToJson(transactionFilePath);
            // 解析JSON字符串
            JsonArray jsonArray = JsonParser.parseString(jsonOutput).getAsJsonArray();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (JsonElement jsonElement : jsonArray) {
                try {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    String createTime = jsonObject.get("createTime").getAsString();
                    if (jsonObject.get("type").getAsString().equals("买金")) {
                        buyGold(jsonObject.get("totalCost").getAsDouble(), jsonObject.get("quantity").getAsDouble(), LocalDateTime.parse(createTime, formatter));
                    } else if (jsonObject.get("type").getAsString().equals("卖金")) {
                        sellGold(jsonObject.get("quantity").getAsDouble(), jsonObject.get("perCost").getAsDouble());
                    }
                } catch (JsonSyntaxException e) {
                    log.error("解析出错：" + e.getMessage());
                }
            }


        }
    }

    @Override
    public void deleteAllTransaction() {
        goldMapper.deleteAllTransaction();
    }

    @Override
    public int initializeDatabase() {
        String userHome = System.getProperty("user.home");
        File goldDataDir = new File(userHome, "GoldData");
        if (!goldDataDir.exists()) {
            boolean created = goldDataDir.mkdirs();
            if (created) {
                log.debug("GoldData 目录创建成功");
            } else {
                log.debug("GoldData 目录创建失败");
            }
        } else {
            log.debug("GoldData 目录已存在");
        }

        // 获取 GoldData 目录的相对路径
        String relativePath = goldDataDir.getPath();
        log.debug("GoldData 目录的相对路径: " + relativePath);
        // 拼接jdbc     public static String DB_URL_MS = "jdbc:sqlite:./data/Transaction_MS.db"; //民生银行
        Config.DB_URL_MS = "jdbc:sqlite:" + relativePath + "/Transaction_MS.db";
        Config.DB_URL_ZS = "jdbc:sqlite:" + relativePath + "/Transaction_ZS.db";
        Config.DB_URL_GS = "jdbc:sqlite:" + relativePath + "/Transaction_GS.db";
        try {
            goldMapper = new GoldMapper("MS");
            goldMapper.initializeDatabase();
        }catch (Exception e){
            log.error("初始化数据库失败");
            return 0;
        }
        return 1;
    }
}

