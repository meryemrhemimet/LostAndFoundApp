package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatDelegate;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.data.DatabaseHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Profil — avatar, stats, liste de notifications, paramètres, déconnexion.
 * Badge effacé à l'ouverture (notifications marquées lues).
 */
public class ProfileActivity extends BaseActivity {
    private DatabaseHelper db;
    private NotificationAdapter notifAdapter;
    private RecyclerView rvNotif;
    private TextView tvNoNotif;

    @Override protected int getContentLayoutId() { return R.layout.activity_profile; }
    @Override protected int getSelectedNavItem() { return R.id.menu_profile; }
    @Override protected String getToolbarTitle() { return getString(R.string.toolbar_profile); }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DatabaseHelper(this);
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);

        // Header
        ((TextView) findViewById(R.id.tvProfileName)).setText(sp.getString("username", "-"));
        ((TextView) findViewById(R.id.tvProfileEmail)).setText(sp.getString("userEmail", "-"));
        String phone = sp.getString("phone", "");
        ((TextView) findViewById(R.id.tvProfilePhone)).setText(phone.isEmpty() ? "" : phone);

        // Stats
        int active = db.getCountByUserAndStatus(userId, "active");
        int resolved = db.getCountByUserAndStatus(userId, "resolved");
        ((TextView) findViewById(R.id.tvStatTotal)).setText(String.valueOf(active + resolved));
        ((TextView) findViewById(R.id.tvStatActive)).setText(String.valueOf(active));
        ((TextView) findViewById(R.id.tvStatResolved)).setText(String.valueOf(resolved));

        // Notifications list
        rvNotif = findViewById(R.id.rvNotifications);
        tvNoNotif = findViewById(R.id.tvNoNotif);
        rvNotif.setLayoutManager(new LinearLayoutManager(this));
        notifAdapter = new NotificationAdapter();
        rvNotif.setAdapter(notifAdapter);
        loadNotifications();

        // Mark all as read + clear badge
        db.markAllNotifsRead(userId);
        updateProfileBadge();

        // Clear all notifs
        findViewById(R.id.tvClearNotif).setOnClickListener(v -> {
            db.clearNotifications(userId);
            loadNotifications();
            updateProfileBadge();
        });

        // Notif toggle
        SwitchMaterial sw = findViewById(R.id.switchNotif);
        sw.setChecked(sp.getBoolean("notifEnabled", true));

        sw.setOnCheckedChangeListener((btn, checked) -> sp.edit()
                .putBoolean("notifEnabled", checked).apply());

        // Toggle mode sombre (SharedPreferences)
        com.google.android.material.switchmaterial.SwitchMaterial swDark = findViewById(R.id.switchDarkMode);
        swDark.setChecked(sp.getBoolean("darkMode", false));
        swDark.setOnCheckedChangeListener((btn2, checked2) -> {
            sp.edit().putBoolean("darkMode", checked2).apply();
            AppCompatDelegate.setDefaultNightMode(
                checked2 ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // About
        findViewById(R.id.menuAbout).setOnClickListener(v ->
            new AlertDialog.Builder(this).setTitle(getString(R.string.profile_about))
                .setMessage("Objets Perdus v1.0\n\nApplication communautaire de déclaration d'objets perdus ou trouvés.")
                .setPositiveButton("OK", null).show());

        // Logout → back to welcome page (MainActivity)
        findViewById(R.id.menuLogout).setOnClickListener(v ->
            new AlertDialog.Builder(this).setMessage(R.string.confirm_logout)
                .setPositiveButton(R.string.yes, (d, w) -> {
                    sp.edit().clear().apply();
                    Intent i = new Intent(this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i); finish();
                }).setNegativeButton(R.string.no, null).show());
    }

    private void loadNotifications() {
        Cursor c = db.getNotifications(userId);
        notifAdapter.setCursor(c);
        tvNoNotif.setVisibility(c.getCount() == 0 ? View.VISIBLE : View.GONE);
        rvNotif.setVisibility(c.getCount() == 0 ? View.GONE : View.VISIBLE);
    }
}
