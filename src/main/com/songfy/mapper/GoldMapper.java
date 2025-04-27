package main.com.songfy.mapper;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import main.com.songfy.misc.Config;
import main.com.songfy.pojo.GoldTransaction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoldMapper {
    private static final String DB_URL = Config.DB_URL;

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static int initializeDatabase() {
        String createTransactionsTableSQL = """
                    CREATE TABLE IF NOT EXISTS Transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        totalCost DOUBLE NOT NULL,
                        quantity DOUBLE NOT NULL,
                        buyPrice DOUBLE NOT NULL,
                        soldPrice DOUBLE NOT NULL,
                        isSold BOOLEAN NOT NULL,
                        profit DOUBLE NOT NULL ,
                        updateTime DATETIME NOT NULL
                    );
                """;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTransactionsTableSQL);
        } catch (SQLException e) {
            log.error("创建数据库失败");
            return 0;
        }
        return 1;
    }

    public void insertTransaction(GoldTransaction goldTransaction) {
        String insertSQL = "INSERT INTO Transactions (totalCost, quantity,buyPrice,soldPrice,isSold,profit,updateTime) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            setPstmt(goldTransaction, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("插入数据失败");
        }
    }

    public List<GoldTransaction> queryAllTransaction() {
        List<GoldTransaction> goldTransactions = new ArrayList<>();
        String selectSQL = "SELECT * FROM Transactions;";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                Double totalCost = rs.getDouble("totalCost");
                Double quantity = rs.getDouble("quantity");
                Double buyPrice = rs.getDouble("buyPrice");
                Double soldPrice = rs.getDouble("soldPrice");
                Boolean isSold = rs.getBoolean("isSold");
                Double profit = rs.getDouble("profit");
                LocalDateTime updateTime = rs.getTimestamp("updateTime").toLocalDateTime();
                goldTransactions.add(new GoldTransaction(id, totalCost, quantity, buyPrice, soldPrice, isSold, profit, updateTime));
            }
        } catch (SQLException e) {
            log.error("查询所有数据失败");
        }
        return goldTransactions;
    }

    public void updateTransactionById(GoldTransaction goldTransaction, Integer id) {
        String updateSQL = "UPDATE Transactions SET totalCost = ?, quantity = ?, buyPrice = ?, soldPrice = ?, isSold = ?, profit = ?,updateTime = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            setPstmt(goldTransaction, pstmt);
            pstmt.setInt(8, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("更新数据失败");
        }
    }

    public void deleteTransactionById(Integer id) {
        String deleteSQL = "DELETE FROM Transactions WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error("删除数据失败");
        }
    }

    public GoldTransaction queryTransactionById(Integer id) {
        String selectSQL = "SELECT * FROM Transactions WHERE id=?;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            Double totalCost = rs.getDouble("totalCost");
            Double quantity = rs.getDouble("quantity");
            Double buyPrice = rs.getDouble("buyPrice");
            Double soldPrice = rs.getDouble("soldPrice");
            Boolean isSold = rs.getBoolean("isSold");
            Double profit = rs.getDouble("profit");
            Timestamp timestamp = rs.getTimestamp("updateTime");
            LocalDateTime updateTime = (timestamp != null) ? timestamp.toLocalDateTime() : null;
            log.debug("查询数据成功");
            return new GoldTransaction(id, totalCost, quantity, buyPrice, soldPrice, isSold, profit, updateTime);
        } catch (SQLException e) {
            log.error("查询数据失败");
            return null;
        }

    }

    public Double queryAccumProfit() {
        String selectSQL = "SELECT SUM(profit) AS totalProfit FROM Transactions Where isSold=true;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("totalProfit");
            }
        } catch (SQLException ignored) {
        }
        return -1 * Double.MAX_VALUE;
    }

    public Double queryAvgPriceForRemain() {
        String selectSQL = "SELECT SUM(totalCost) / SUM(quantity) AS averagePrice FROM Transactions WHERE isSold = false;";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            if (rs.next()) {
                return rs.getDouble("averagePrice");

            }
        } catch (SQLException ignored) {
        }
        return -1 * Double.MAX_VALUE;
    }

    public Double queryTotalQuantityForRemain() {
        String selectSQL = "SELECT SUM(quantity) AS totalQuantity FROM Transactions WHERE isSold = false;";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            if (rs.next()) {
                return rs.getDouble("totalQuantity");
            }
        } catch (SQLException e) {
            log.error("查询数据失败", e);
        }
        return -1 * Double.MAX_VALUE;
    }

    private void setPstmt(GoldTransaction goldTransaction, PreparedStatement pstmt) throws SQLException {
        pstmt.setDouble(1, goldTransaction.getTotalCost());
        pstmt.setDouble(2, goldTransaction.getQuantity());
        pstmt.setDouble(3, goldTransaction.getBuyPrice());
        pstmt.setDouble(4, goldTransaction.getSoldPrice());
        pstmt.setBoolean(5, goldTransaction.getIsSold());
        pstmt.setDouble(6, goldTransaction.getProfit());
        pstmt.setTimestamp(7, Timestamp.valueOf(goldTransaction.getUpdateTime()));
    }

    public List<GoldTransaction> querySortedTransactions(boolean isSold, String columnName, boolean ascending) {
        List<GoldTransaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM Transactions  where isSold=? ORDER BY " + columnName + " " + (ascending ? "ASC" : "DESC");
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setBoolean(1, isSold);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                GoldTransaction transaction = new GoldTransaction();
                transaction.setId(rs.getInt("id"));
                transaction.setTotalCost(rs.getDouble("totalCost"));
                transaction.setQuantity(rs.getDouble("quantity"));
                transaction.setBuyPrice(rs.getDouble("buyPrice"));
                transaction.setSoldPrice(rs.getDouble("soldPrice"));
                transaction.setIsSold(rs.getBoolean("isSold"));
                transaction.setProfit(rs.getDouble("profit"));
                transaction.setUpdateTime(rs.getTimestamp("updateTime").toLocalDateTime());
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            log.error("查询排序失败: {}", e.getMessage());
        }
        return transactions;
    }
}
