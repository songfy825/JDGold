package main.com.songfy.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoldSummary {
    private Double avgPrice;
    private Double accumulatedProfit;
    private Double avgProfit;
    private Double totalQuantity;//持有金数量

}
