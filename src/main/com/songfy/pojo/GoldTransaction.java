package main.com.songfy.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GoldTransaction {
    private int id;
    private Double totalCost; // 总花费
    private Double quantity;  // 购买重量（g）
    private Double buyPrice;  // 买入单价（元/g）
    private Double soldPrice; // 卖出单价（元/g）
    private Boolean isSold;   // 是否已卖出
    private Double profit;
    private LocalDateTime createTime;
    private LocalDateTime updateTime; // 更新时间


    public GoldTransaction(Double totalCost, Double quantity) {
        this.totalCost = totalCost;
        this.quantity = quantity;
        this.buyPrice = totalCost / quantity; // 计算买入单价
        this.soldPrice = 0.0;
        this.isSold = false;
        this.profit = 0.0;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();

    }

    public GoldTransaction(GoldTransaction copyTransaction) {
        this.id = copyTransaction.id;
        this.totalCost = copyTransaction.totalCost;
        this.quantity = copyTransaction.quantity;
        this.buyPrice = copyTransaction.buyPrice;
        this.soldPrice = copyTransaction.soldPrice;
        this.isSold = copyTransaction.isSold;
        this.updateTime = copyTransaction.updateTime;
    }

    // 计算每笔交易的盈利
    public Double calculateProfit(Double currentGoldPrice) {
        return (currentGoldPrice * (1 - main.com.songfy.misc.Config.SOLD_FEE_RATIO) - buyPrice) * quantity;
    }



}
