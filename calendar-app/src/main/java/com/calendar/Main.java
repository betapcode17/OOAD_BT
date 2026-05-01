package com.calendar;

import com.calendar.view.MainUI;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception e) {
                e.printStackTrace();
            }
            MainUI mainUI = new MainUI();
            mainUI.setVisible(true);
        });
    }
}
