package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.model.LostItem;
import com.example.myapplication.util.AsyncTaskHelper;
import com.example.myapplication.util.RetrofitService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Liste principale des objets perdus/trouvés.
 * Briques : RecyclerView, AsyncTask, Retrofit (bouchonné), Chips filtres,
 *           Intent explicite vers le détail.
 */
public class LostItemsActivity extends BaseActivity {
    private RecyclerView rv;
    private LostItemAdapter adapter;
    private DatabaseHelper db;
    private EditText etSearch;
    private TextView tvEmpty;
    private String currentFilter = "all"; // all, lost, found

    @Override protected int getContentLayoutId() { return R.layout.activity_lost_items; }
    @Override protected int getSelectedNavItem() { return R.id.menu_lost_items; }
    @Override protected String getToolbarTitle() { return getString(R.string.toolbar_items); }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DatabaseHelper(this);
        rv = findViewById(R.id.rvLostItems);
        etSearch = findViewById(R.id.etSearch);
        tvEmpty = findViewById(R.id.tvEmptyList);

        // RecyclerView
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LostItemAdapter(item -> {
            // Intent explicite vers le détail
            Intent intent = new Intent(this, LostItemDetailActivity.class);
            intent.putExtra("ITEM_ID", item.getId());
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_NAME", userName);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        rv.setAdapter(adapter);

        // Chips de filtre (Tout / Perdu / Trouvé)
        ChipGroup chipGroup = findViewById(R.id.chipGroupType);
        String[] labels = {getString(R.string.cat_all), getString(R.string.lost), getString(R.string.found)};
        String[] filters = {"all", "lost", "found"};
        for (int i = 0; i < labels.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(labels[i]);
            chip.setCheckable(true);
            chip.setChecked(i == 0);
            final String filter = filters[i];
            chip.setOnClickListener(v -> { currentFilter = filter; loadItems(); });
            chipGroup.addView(chip);
        }

        // Recherche
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { loadItems(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Démo Retrofit bouchonné (appel ignoré — juste pour montrer l'usage)
        demoRetrofit();

        // Chargement via AsyncTask (deprecated, avec alternative)
        loadWithAsyncTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    // Chargement normal depuis SQLite
    private void loadItems() {
        String query = etSearch.getText().toString().trim();
        List<LostItem> items;
        if (!query.isEmpty()) {
            items = db.searchItems(query);
            // Sauvegarder uniquement les mots-clés significatifs (3+ caractères)
            if (query.length() >= 3) {
                db.saveSearchAlert(userId, query);
            }
        } else if ("lost".equals(currentFilter)) {
            items = db.getItemsByType("lost");
        } else if ("found".equals(currentFilter)) {
            items = db.getItemsByType("found");
        } else {
            items = db.getAllItems();
        }
        adapter.setItems(items);
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        rv.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
    }

    // Démo AsyncTask (deprecated) — charge les données en arrière-plan
    @SuppressWarnings("deprecation")
    private void loadWithAsyncTask() {
        new AsyncTaskHelper.LoadItemsTask(result -> loadItems()).execute();
    }

    // Démo Retrofit avec intercepteur bouchonné
    private void demoRetrofit() {
        RetrofitService.getApi().getItems().enqueue(new Callback<List<Object>>() {
            @Override public void onResponse(Call<List<Object>> call, Response<List<Object>> r) {
                // Réponse mock reçue — on utilise SQLite dans l'app réelle
            }
            @Override public void onFailure(Call<List<Object>> call, Throwable t) {}
        });
    }
}
