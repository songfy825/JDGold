import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionManager {
    private static final String FILE_PATH = config.FILE_PATH;

    public TransactionManager() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<GoldTransaction> loadTransactions() {
        List<GoldTransaction> goldTransactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    double totalCost = Double.parseDouble(parts[0]);
                    double quantity = Double.parseDouble(parts[1]);
                    double buyPrice = Double.parseDouble(parts[2]);
                    boolean isSold = Boolean.parseBoolean(parts[3]);
                    GoldTransaction goldTransaction = new GoldTransaction(totalCost, quantity);
                    goldTransaction.buyPrice = buyPrice;
                    goldTransaction.isSold = isSold;
                    goldTransactions.add(goldTransaction);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return goldTransactions;
    }

    public void saveTransactions(List<GoldTransaction> goldTransactions) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (GoldTransaction goldTransaction : goldTransactions) {
                writer.write(String.format("%.2f,%.2f,%.2f,%b%n",
                        goldTransaction.totalCost, goldTransaction.quantity, goldTransaction.buyPrice, goldTransaction.isSold));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("保存成功");
    }
}
