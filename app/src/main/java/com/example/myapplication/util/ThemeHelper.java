package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

// Applique le thème sombre/clair selon les SharedPreferences
public class ThemeHelper {
    public static void applyTheme(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        boolean isDark = sp.getBoolean("darkMode", false);
        AppCompatDelegate.setDefaultNightMode(
            isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
