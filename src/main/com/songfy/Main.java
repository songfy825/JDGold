package main.com.songfy;

import lombok.extern.slf4j.Slf4j;
import main.com.songfy.ui.GoldTradingApp;

import java.awt.*;
import java.io.File;

@Slf4j
public class Main {
    public static void main(String[] args) {
        // 在项目路径创建文件夹 data
        // 获取用户主目录并创建文件夹 data
        String userHome = System.getProperty("user.home");
        File dataDir = new File(userHome, "GoldData"); // 更改为你希望的文件夹名称
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (created) {
                log.info("文件夹 {} 创建成功", dataDir.getAbsolutePath());
            } else {
                log.error("文件夹 {} 创建失败", dataDir.getAbsolutePath());
            }
        }
        EventQueue.invokeLater(GoldTradingApp::new);
    }
}