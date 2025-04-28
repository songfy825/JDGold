package main.com.songfy;

import lombok.extern.slf4j.Slf4j;
import main.com.songfy.ui.GoldTradingApp;

import java.awt.*;
import java.io.File;

@Slf4j
public class Main {
    public static void main(String[] args) {
        // 在项目路径创建文件夹 data
        File dataDir = new File("./data");
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (created) {
                log.info("文件夹 data 创建成功");
            } else {
                log.error("文件夹 data 创建失败");
            }
        }
        EventQueue.invokeLater(GoldTradingApp::new);
    }
}