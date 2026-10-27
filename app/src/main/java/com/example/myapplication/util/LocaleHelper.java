package com.example.myapplication.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

// Force la langue française pour toute l'application
public class LocaleHelper {
    public static Context setLocale(Context context) {
        Locale locale = new Locale("fr");
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }
}
