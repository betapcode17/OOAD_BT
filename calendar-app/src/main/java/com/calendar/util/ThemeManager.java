package com.calendar.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class ThemeManager {
    private static boolean isDarkMode = false;

    public static void setLightTheme() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            isDarkMode = false;
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    public static void setDarkTheme() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            isDarkMode = true;
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    public static void toggleTheme() {
        if (isDarkMode) {
            setLightTheme();
        } else {
            setDarkTheme();
        }
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void initializeDefaultTheme() {
        setLightTheme();
    }
}
