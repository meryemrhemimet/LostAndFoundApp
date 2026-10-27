package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.model.LostItem;
import com.example.myapplication.util.NotificationHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * Mes objets — fusionne "Mes annonces" + "Historique".
 * Chips: Tout / En cours / Résolu. Clic long → supprimer/résoudre.
 */
public class MyAdsActivity extends BaseActivity {
    private RecyclerView rv;
    private LostItemAdapter adapter;
    private DatabaseHelper db;
    private EditText etSearch;
    private TextView tvEmpty;
    private String statusFilter = "all";

    @Override protected int getContentLayoutId() { return R.layout.activity_my_ads; }
    @Override protected int getSelectedNavItem() { return R.id.menu_my_ads; }
    @Override protected String getToolbarTitle() { return getString(R.string.toolbar_my_ads); }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DatabaseHelper(this);
        rv = findViewById(R.id.rvMyAds);
        etSearch = findViewById(R.id.etSearch);
        tvEmpty = findViewById(R.id.tvEmptyMyAds);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Status chips
        ChipGroup cg = findViewById(R.id.chipGroupStatus);
        String[] labels = {getString(R.string.cat_all), getString(R.string.status_active), getString(R.string.status_resolved)};
        String[] filters = {"all", "active", "resolved"};
        for (int i = 0; i < labels.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(labels[i]); chip.setCheckable(true); chip.setChecked(i == 0);
            final String f = filters[i];
            chip.setOnClickListener(v -> { statusFilter = f; loadMyItems(); });
            cg.addView(chip);
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int bf, int c) { loadMyItems(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override protected void onResume() { super.onResume(); loadMyItems(); }

    private void loadMyItems() {
        List<LostItem> base;
        if ("all".equals(statusFilter)) base = db.getItemsByUser(userId);
        else base = db.getItemsByStatus(userId, statusFilter);

        String q = etSearch.getText().toString().trim().toLowerCase();
        List<LostItem> items;
        if (q.isEmpty()) { items = base; }
        else {
            items = new ArrayList<>();
            for (LostItem it : base)
                if (it.getTitle().toLowerCase().contains(q) ||
                    (it.getDescription() != null && it.getDescription().toLowerCase().contains(q)))
                    items.add(it);
        }

        adapter = new LostItemAdapter(item -> {
            Intent i = new Intent(this, LostItemDetailActivity.class);
            i.putExtra("ITEM_ID", item.getId()); i.putExtra("USER_ID", userId);
            startActivity(i);
        });
        rv.setAdapter(adapter);
        adapter.setItems(items);

        rv.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override public void onChildViewAttachedToWindow(View v) {
                v.setOnLongClickListener(view -> {
                    int pos = rv.getChildAdapterPosition(view);
                    if (pos >= 0 && pos < adapter.getItems().size()) showItemOptions(adapter.getItems().get(pos));
                    return true;
                });
            }
            @Override public void onChildViewDetachedFromWindow(View v) { v.setOnLongClickListener(null); }
        });

        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        rv.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showItemOptions(LostItem item) {
        String[] options = {getString(R.string.mark_resolved), getString(R.string.delete)};
        new AlertDialog.Builder(this).setItems(options, (d, which) -> {
            if (which == 0) {
                new AlertDialog.Builder(this).setMessage(R.string.confirm_resolve)
                    .setPositiveButton(R.string.yes, (dd, w) -> {
                        db.updateStatus(item.getId(), "resolved");
                        NotificationHelper.notifyResolved(this, item.getTitle());
                        loadMyItems(); updateProfileBadge();
                    }).setNegativeButton(R.string.no, null).show();
            } else {
                new AlertDialog.Builder(this).setMessage(R.string.confirm_delete)
                    .setPositiveButton(R.string.yes, (dd, w) -> {
                        db.deleteItem(item.getId());
                        NotificationHelper.notifyDeleted(this, item.getTitle());
                        Toast.makeText(this, R.string.success_delete, Toast.LENGTH_SHORT).show();
                        loadMyItems(); updateProfileBadge();
                    }).setNegativeButton(R.string.no, null).show();
            }
        }).show();
    }
}
