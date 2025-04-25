import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PriceFetcher {
    private static final String API_URL = config.API_URL;

    public static double fetchCurrentGoldPrice() throws Exception {
        // 创建 HTTP 连接
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // 设置超时时间（可选）
        conn.setConnectTimeout(5000); // 5 秒
        conn.setReadTimeout(5000);    // 5 秒

        // 检查响应状态码
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("API 请求失败，状态码：" + responseCode);
        }

        // 读取响应内容
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // 手动解析 JSON 数据
        String jsonResponse = response.toString();
        String priceText = extractValueFromJson(jsonResponse, "price");

        // 转换为 double 类型
        return Double.parseDouble(priceText);
    }

    /**
     * 从 JSON 字符串中提取指定键的值
     */
    private static String extractValueFromJson(String json, String key) {
        String targetKey = "\"" + key + "\":\""; // 构造目标键的模式
        int startIndex = json.indexOf(targetKey);
        if (startIndex == -1) {
            throw new RuntimeException("无法找到键 '" + key + "' 的值！");
        }

        startIndex += targetKey.length(); // 跳过键和引号
        int endIndex = json.indexOf("\"", startIndex); // 找到下一个引号的位置
        if (endIndex == -1) {
            throw new RuntimeException("JSON 格式错误！");
        }

        return json.substring(startIndex, endIndex); // 提取值
    }


}