package main.com.songfy;

import lombok.extern.slf4j.Slf4j;
import main.com.songfy.ui.GoldTradingApp;

import java.awt.*;

@Slf4j
public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(GoldTradingApp::new);
    }
}