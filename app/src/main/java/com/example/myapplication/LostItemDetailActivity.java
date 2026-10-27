package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.model.LostItem;
import com.example.myapplication.util.LocaleHelper;
import com.example.myapplication.util.NotificationHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Détail d'un objet — avec boutons supprimer/résoudre pour ses propres annonces.
 * Briques : Fragment (SupportMapFragment), Intents implicites (appel, SMS, partage).
 */
public class LostItemDetailActivity extends AppCompatActivity {
    private LostItem item;
    private DatabaseHelper db;
    private static final int REQ_CALL = 301;

    @Override protected void attachBaseContext(android.content.Context c) { super.attachBaseContext(LocaleHelper.setLocale(c)); }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_base);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.toolbar_detail));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        findViewById(R.id.btnLogout).setVisibility(View.GONE);
        findViewById(R.id.bottom_navigation_menu).setVisibility(View.GONE);

        FrameLayout container = findViewById(R.id.frame_container);
        getLayoutInflater().inflate(R.layout.activity_lost_item_detail, container, true);

        db = new DatabaseHelper(this);
        long itemId = getIntent().getLongExtra("ITEM_ID", -1);
        item = db.getItemById(itemId);
        if (item == null) { finish(); return; }

        // Get current user ID
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        String currentUserId = sp.getString("userId", "");

        bindData();
        setupMap();
        setupContactButtons();
        setupShareButton();
        setupOwnerActions(currentUserId);
    }

    private void bindData() {
        ImageView ivPhoto = findViewById(R.id.ivDetailPhoto);
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvType = findViewById(R.id.tvDetailType);
        TextView tvStatus = findViewById(R.id.tvDetailStatus);
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        TextView tvDate = findViewById(R.id.tvDetailDate);
        TextView tvDesc = findViewById(R.id.tvDetailDescription);
        TextView tvAddress = findViewById(R.id.tvDetailAddress);
        TextView tvUserName = findViewById(R.id.tvDetailUserName);

        tvTitle.setText(item.getTitle());
        tvDesc.setText(item.getDescription());
        tvAddress.setText(item.getAddress());
        tvUserName.setText(item.getUserName());
        tvCategory.setText(item.getCategory());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRANCE);
        tvDate.setText(sdf.format(new Date(item.getCreatedAt())));

        boolean isLost = "lost".equals(item.getType());
        tvType.setText(isLost ? getString(R.string.lost) : getString(R.string.found));
        GradientDrawable bgType = new GradientDrawable();
        bgType.setCornerRadius(24f);
        bgType.setColor(ContextCompat.getColor(this, isLost ? R.color.lostColor : R.color.foundColor));
        tvType.setBackground(bgType);

        boolean active = "active".equals(item.getStatus());
        tvStatus.setText(active ? getString(R.string.status_active) : getString(R.string.status_resolved));
        GradientDrawable bgStat = new GradientDrawable();
        bgStat.setCornerRadius(24f);
        bgStat.setColor(ContextCompat.getColor(this, active ? R.color.colorPrimary : R.color.resolvedColor));
        tvStatus.setBackground(bgStat);

        if (item.getPhotoPath() != null && !item.getPhotoPath().isEmpty()) {
            File f = new File(item.getPhotoPath());
            if (f.exists()) Glide.with(this).load(f).centerCrop().into(ivPhoto);
            else Glide.with(this).load(Uri.parse(item.getPhotoPath())).centerCrop()
                .placeholder(R.drawable.ic_placeholder).into(ivPhoto);
        }
    }

    private void setupMap() {
        if (item.getLatitude() != 0 || item.getLongitude() != 0) {
            SupportMapFragment mapFrag = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.mapContainer, mapFrag).commit();
            mapFrag.getMapAsync(map -> {
                LatLng pos = new LatLng(item.getLatitude(), item.getLongitude());
                map.addMarker(new MarkerOptions().position(pos).title(item.getTitle()));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
                map.getUiSettings().setAllGesturesEnabled(false);
            });
        } else {
            findViewById(R.id.tvMapError).setVisibility(View.VISIBLE);
        }
    }

    private void setupContactButtons() {
        findViewById(R.id.btnCall).setOnClickListener(v -> {
            String phone = item.getPhone();
            if (phone == null || phone.isEmpty()) { Toast.makeText(this, R.string.no_phone, Toast.LENGTH_SHORT).show(); return; }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQ_CALL);
            else startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
        });

        findViewById(R.id.btnSms).setOnClickListener(v -> {
            String phone = item.getPhone();
            if (phone == null || phone.isEmpty()) { Toast.makeText(this, R.string.no_phone, Toast.LENGTH_SHORT).show(); return; }
            Intent sms = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone));
            sms.putExtra("sms_body", String.format(getString(R.string.sms_body), item.getTitle()));
            startActivity(sms);
        });
    }

    private void setupShareButton() {
        findViewById(R.id.btnShare).setOnClickListener(v -> {
            String text = String.format(getString(R.string.share_text),
                "lost".equals(item.getType()) ? getString(R.string.lost) : getString(R.string.found),
                item.getTitle(), item.getAddress());
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(share, getString(R.string.btn_share)));
        });
    }

    // Show delete/resolve buttons if user owns this item
    private void setupOwnerActions(String currentUserId) {
        LinearLayout ownerActions = findViewById(R.id.ownerActions);
        if (item.getUserId() != null && item.getUserId().equals(currentUserId)) {
            ownerActions.setVisibility(View.VISIBLE);

            // Resolve button
            findViewById(R.id.btnResolve).setOnClickListener(v ->
                new AlertDialog.Builder(this)
                    .setMessage(R.string.confirm_resolve)
                    .setPositiveButton(R.string.yes, (d, w) -> {
                        db.updateStatus(item.getId(), "resolved");
                        NotificationHelper.notifyResolved(this, item.getTitle());
                        Toast.makeText(this, R.string.status_resolved, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton(R.string.no, null).show());

            // Delete button
            findViewById(R.id.btnDelete).setOnClickListener(v ->
                new AlertDialog.Builder(this)
                    .setMessage(R.string.confirm_delete)
                    .setPositiveButton(R.string.yes, (d, w) -> {
                        db.deleteItem(item.getId());
                        NotificationHelper.notifyDeleted(this, item.getTitle());
                        Toast.makeText(this, R.string.success_delete, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton(R.string.no, null).show());

            // Hide resolve if already resolved
            if ("resolved".equals(item.getStatus())) {
                findViewById(R.id.btnResolve).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == REQ_CALL && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED)
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + item.getPhone())));
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
