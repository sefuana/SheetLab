package com.axsaafe.sheetlab;

import androidx.multidex.MultiDexApplication;
import androidx.appcompat.app.AppCompatDelegate;

public class SheetLabApp extends MultiDexApplication {

    public static final String PREF_NAME = "sheetlab_prefs";
    public static final String KEY_DARK_MODE = "dark_mode";

    @Override
    public void onCreate() {
        super.onCreate();
        // Apply saved theme on app start
        boolean darkMode = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getBoolean(KEY_DARK_MODE, true);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
