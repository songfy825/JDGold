package main.com.songfy.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRecord {
    String type;
    String createTime;
    double quantity;
    double perCost;
    double totalCost;


}