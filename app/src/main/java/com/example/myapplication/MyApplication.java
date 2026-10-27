package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import com.example.myapplication.util.LocaleHelper;
import com.example.myapplication.util.NotificationHelper;
import com.example.myapplication.util.ThemeHelper;

public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.setLocale(base));
    }
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeHelper.applyTheme(this);
        NotificationHelper.createChannel(this);
    }
}
