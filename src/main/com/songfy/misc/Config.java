package main.com.songfy.misc;

import lombok.Data;


public class Config {
    public static String DB_URL_MS = "jdbc:sqlite:./data/Transaction_MS.db"; //民生银行
    public static String DB_URL_ZS = "jdbc:sqlite:./data/Transaction_ZS.db"; //浙商银行
    public static String DB_URL_GS = "jdbc:sqlite:./data/Transaction_GS.db"; //工商银行
    public static final String API_URL_MS = "https://api.jdjygold.com/gw/generic/hj/h5/m/latestPrice";
    public static final String API_URL_ZS = "https://api.jdjygold.com/gw2/generic/jrm/h5/m/stdLatestPrice?productSku=1961543816";
    public static final String API_URL_GS = "https://api.jdjygold.com/gw2/generic/jrm/h5/m/stdLatestPrice";
    public static final double SOLD_FEE_RATIO_MS = 0.004;
    public static final double SOLD_FEE_RATIO_ZS = 0.004;
    public static final double SOLD_FEE_RATIO_GS = 0.005;
}
