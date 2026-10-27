package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.util.LocaleHelper;

public abstract class BaseActivity extends AppCompatActivity {
    protected BottomNavigationView bottomNav;
    protected String userId, userName, userEmail;

    @Override protected void attachBaseContext(Context c) { super.attachBaseContext(LocaleHelper.setLocale(c)); }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_base);
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = getIntent().getStringExtra("USER_ID");
        userName = getIntent().getStringExtra("USER_NAME");
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userId == null) userId = sp.getString("userId", "");
        if (userName == null) userName = sp.getString("username", "");
        if (userEmail == null) userEmail = sp.getString("userEmail", "");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(getToolbarTitle());

        findViewById(R.id.btnLogout).setVisibility(View.GONE);

        FrameLayout container = findViewById(R.id.frame_container);
        LayoutInflater.from(this).inflate(getContentLayoutId(), container, true);

        bottomNav = findViewById(R.id.bottom_navigation_menu);
        bottomNav.setSelectedItemId(getSelectedNavItem());
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == getSelectedNavItem()) return true;
            Class<?> target = null;
            if (id == R.id.menu_lost_items) target = LostItemsActivity.class;
            else if (id == R.id.menu_create_ad) target = CreateAdActivity.class;
            else if (id == R.id.menu_map) target = MapActivity.class;
            else if (id == R.id.menu_my_ads) target = MyAdsActivity.class;
            else if (id == R.id.menu_profile) target = ProfileActivity.class;
            if (target != null) { navigateTo(target); return true; }
            return false;
        });

        updateProfileBadge();
    }

    // Badge de notification sur l'icône Profil
    protected void updateProfileBadge() {
        if (userId == null || userId.isEmpty()) return;
        int count = new DatabaseHelper(this).getUnreadNotifCount(userId);
        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.menu_profile);
        if (count > 0) {
            badge.setVisible(true);
            badge.setNumber(count);
            badge.setBackgroundColor(getResources().getColor(R.color.dangerColor, getTheme()));
        } else {
            badge.setVisible(false);
            badge.clearNumber();
        }
    }

    private void navigateTo(Class<?> cls) {
        Intent i = new Intent(this, cls);
        i.putExtra("USER_ID", userId);
        i.putExtra("USER_NAME", userName);
        i.putExtra("USER_EMAIL", userEmail);
        startActivity(i);
        overridePendingTransition(R.anim.fast_fade_in, R.anim.fast_fade_out);
        finish();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }

    protected abstract int getContentLayoutId();
    protected abstract int getSelectedNavItem();
    protected abstract String getToolbarTitle();
}
