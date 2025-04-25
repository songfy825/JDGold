
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class GoldTransaction {
    double totalCost; // 总花费
    double quantity;  // 购买重量（g）
    double buyPrice;  // 买入单价（元/g）
    double sellPrice; // 卖出单价（元/g）
    boolean isSold;   // 是否已卖出

    public GoldTransaction(double totalCost, double quantity) {
        this.totalCost = totalCost;
        this.quantity = quantity;
        this.buyPrice = totalCost / quantity; // 计算买入单价
        this.isSold = false;
    }

    // 计算每笔交易的盈利
    public double calculateProfit(double currentGoldPrice) {
        double sellPrice = currentGoldPrice * (1 - 0.004); // 减去卖出手续费
        return (sellPrice - buyPrice) * quantity;

    }

    @Override
    public String toString() {
        if (isSold)
            return String.format("总花费: %.2f 元, 重量: %.2f g, 买入价: %.2f 元/g,盈利:%.2f 已卖出: %s",
                    totalCost, quantity, buyPrice, 2.0,isSold ? "是" : "否");
        return "";
    }
}