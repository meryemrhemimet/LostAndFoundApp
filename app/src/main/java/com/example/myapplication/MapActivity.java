package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.model.LostItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Carte interactive — affiche tous les objets sur Google Maps.
 * Filtre par type (Tout / Perdu / Trouvé). Clic sur marqueur → détail.
 */
public class MapActivity extends BaseActivity {
    private MapView mapView;
    private GoogleMap gMap;
    private DatabaseHelper db;
    private String mapFilter = "all";
    private final Map<Marker, Long> markerItemMap = new HashMap<>();

    @Override protected int getContentLayoutId() { return R.layout.activity_map; }
    @Override protected int getSelectedNavItem() { return R.id.menu_map; }
    @Override protected String getToolbarTitle() { return getString(R.string.toolbar_map); }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DatabaseHelper(this);

        // Chips
        ChipGroup cg = findViewById(R.id.chipGroupMap);
        String[] labels = {getString(R.string.cat_all), getString(R.string.lost), getString(R.string.found)};
        String[] filters = {"all", "lost", "found"};
        for (int i = 0; i < labels.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(labels[i]); chip.setCheckable(true); chip.setChecked(i == 0);
            chip.setChipBackgroundColorResource(R.color.colorSurface);
            chip.setElevation(4);
            final String f = filters[i];
            chip.setOnClickListener(v -> { mapFilter = f; loadMarkers(); });
            cg.addView(chip);
        }

        mapView = findViewById(R.id.mapViewFull);
        mapView.onCreate(b);
        mapView.getMapAsync(map -> {
            gMap = map;
            gMap.getUiSettings().setZoomControlsEnabled(true);
            gMap.setOnInfoWindowClickListener(marker -> {
                Long itemId = markerItemMap.get(marker);
                if (itemId != null) {
                    Intent i = new Intent(this, LostItemDetailActivity.class);
                    i.putExtra("ITEM_ID", itemId);
                    startActivity(i);
                }
            });
            loadMarkers();
        });
    }

    private void loadMarkers() {
        if (gMap == null) return;
        gMap.clear(); markerItemMap.clear();
        List<LostItem> items;
        if ("lost".equals(mapFilter)) items = db.getItemsByType("lost");
        else if ("found".equals(mapFilter)) items = db.getItemsByType("found");
        else items = db.getAllItems();

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        boolean hasPoints = false;
        for (LostItem item : items) {
            if (item.getLatitude() == 0 && item.getLongitude() == 0) continue;
            LatLng pos = new LatLng(item.getLatitude(), item.getLongitude());
            float hue = "lost".equals(item.getType()) ? BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_GREEN;
            Marker m = gMap.addMarker(new MarkerOptions().position(pos).title(item.getTitle())
                .snippet(item.getAddress()).icon(BitmapDescriptorFactory.defaultMarker(hue)));
            if (m != null) markerItemMap.put(m, item.getId());
            bounds.include(pos); hasPoints = true;
        }
        if (hasPoints) {
            gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 80));
        } else {
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(46.6, 2.5), 5));
        }
    }

    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { mapView.onPause(); super.onPause(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onSaveInstanceState(Bundle b) { super.onSaveInstanceState(b); mapView.onSaveInstanceState(b); }
}
