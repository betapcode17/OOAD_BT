package com.calendar;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.calendar.view.MainUI;
import com.formdev.flatlaf.FlatLightLaf;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception e) {
            }
            MainUI mainUI = new MainUI();
            mainUI.setVisible(true);
        });
    }
}
