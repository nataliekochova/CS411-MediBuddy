package com.medibuddy.service;

import java.util.prefs.Preferences;

import javafx.collections.ObservableList;
import javafx.scene.Scene;

public final class ThemeManager {
    private static final String DARK_THEME_CLASS = "dark-theme";
    private static final String DARK_MODE_KEY = "darkModeEnabled";
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(ThemeManager.class);

    private ThemeManager() {
    }

    public static boolean isDarkModeEnabled() {
        return PREFERENCES.getBoolean(DARK_MODE_KEY, false);
    }

    public static void setDarkModeEnabled(boolean enabled) {
        PREFERENCES.putBoolean(DARK_MODE_KEY, enabled);
    }

    public static void applyTheme(Scene scene) {
        if (scene == null || scene.getRoot() == null) {
            return;
        }

        ObservableList<String> styleClasses = scene.getRoot().getStyleClass();
        boolean darkModeEnabled = isDarkModeEnabled();

        if (darkModeEnabled) {
            if (!styleClasses.contains(DARK_THEME_CLASS)) {
                styleClasses.add(DARK_THEME_CLASS);
            }
        } else {
            styleClasses.remove(DARK_THEME_CLASS);
        }
    }
}
