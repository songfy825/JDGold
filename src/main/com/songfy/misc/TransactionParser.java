package main.com.songfy.misc;

import main.com.songfy.pojo.TransactionRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TransactionParser {

    public static void main(String[] args) {
        String htmlFilePath = "src/main/resources/trans.html";
        String jsonOutput = parseTransactionsToJson(htmlFilePath);
        System.out.println(jsonOutput);
    }

    public static String parseTransactionsToJson(String htmlFilePath) {
        List<TransactionRecord> records = new ArrayList<>();

        try {
            Document doc = Jsoup.parse(new File(htmlFilePath), "UTF-8");

            // 查找所有交易记录
            Elements transactions = doc.select("div.trans-list-tabulate-td");

            for (Element trans : transactions) {
                // 提取类型和状态
                Element typeDiv = trans.selectFirst("div.trans-list-tabulate-td-box-col-text");
                Element statusDiv = trans.selectFirst("div.trans-list-tabulate-td-box-col-label");

                String transType = typeDiv != null ? typeDiv.text().strip() : "未知";
                String status = statusDiv != null ? statusDiv.text().strip() : "未知";

                // 如果状态包含“失败”，则跳过该条记录
                if (status.contains("失败")) {
                    continue;
                }

                // 提取时间
                Element timeDiv = trans.selectFirst("div.trans-list-tabulate-td-box-col-datetext");
                Element nameDiv = trans.selectFirst("div.trans-list-tabulate-td-box-col-righttext");
                String time = timeDiv != null ? timeDiv.text().strip() : "未知";
                String name = nameDiv != null ? nameDiv.text().strip() : "未知";
                // 检查时间是否包含年份，如果缺少则补全为2025年
                Pattern yearPattern = Pattern.compile("\\d{4}");
                if (!yearPattern.matcher(time).find()) {
                    time = "2025-" + time;
                }

                // 提取克重和金价
                Elements weightDivs = trans.select("div.trans-list-tabulate-td-box-col-amount");
                Elements priceDivs = trans.select("div.trans-list-tabulate-td-box-col-datetext");

                String weightStr = weightDivs.size() > 0 ? weightDivs.get(0).text().strip() : "0克";
                String priceStr = priceDivs.size() > 1 ? priceDivs.get(1).text().strip() : "0元";

                // 提取金额
                String amountStr = weightDivs.size() > 1 ? weightDivs.get(1).text().strip() : "0元";

                // 清理数据
                String weight = weightStr.replaceAll("[^\\d.]", "");
                String price = priceStr.replaceAll("[^\\d.]", "");
                String amount = amountStr.replaceAll("[^\\d.]", "");

                // 转换为数值
                double weightValue = weight.isEmpty() ? 0 : Double.parseDouble(weight);
                double priceValue = price.isEmpty() ? 0 : Double.parseDouble(price);
                double amountValue = amount.isEmpty() ? 0 : Double.parseDouble(amount);

                TransactionRecord record = new TransactionRecord(transType, name, time, weightValue, priceValue, amountValue);
                records.add(0, record);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // 将记录转换为JSON格式
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(records);
    }
}
