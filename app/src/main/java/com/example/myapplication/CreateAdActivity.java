package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.model.LostItem;
import com.example.myapplication.util.NotificationHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Créer une nouvelle déclaration d'objet perdu ou trouvé.
 * Briques : Google Maps (MapView), Caméra (Intent ACTION_IMAGE_CAPTURE via FileProvider),
 *           Galerie (Intent implicite ACTION_PICK), Notification locale.
 */
public class CreateAdActivity extends BaseActivity {
    private MapView mapView;
    private GoogleMap gMap;
    private TextInputEditText edtAddress, edtTitle, edtDesc, edtPhone;
    private AutoCompleteTextView spinnerCategory;
    private RadioGroup rgType;
    private ImageView ivPhoto;
    private DatabaseHelper db;
    private double selectedLat = 48.8566, selectedLng = 2.3522; // Paris par défaut
    private String photoPath = null;

    // Lanceurs d'activité (remplace startActivityForResult deprecated)
    private final ActivityResultLauncher<Intent> galleryLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                photoPath = uri != null ? uri.toString() : null;
                ivPhoto.setImageURI(uri);
                ivPhoto.setVisibility(android.view.View.VISIBLE);
            }
        });

    private final ActivityResultLauncher<Intent> cameraLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && photoPath != null) {
                ivPhoto.setImageURI(Uri.parse("file://" + photoPath));
                ivPhoto.setVisibility(android.view.View.VISIBLE);
            }
        });

    @Override protected int getContentLayoutId() { return R.layout.activity_create_ad; }
    @Override protected int getSelectedNavItem() { return R.id.menu_create_ad; }
    @Override protected String getToolbarTitle() { return getString(R.string.toolbar_create); }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DatabaseHelper(this);

        edtAddress = findViewById(R.id.edtAddress);
        edtTitle = findViewById(R.id.edtItemTitle);
        edtDesc = findViewById(R.id.edtItemDescription);
        edtPhone = findViewById(R.id.edtPhone);
        ivPhoto = findViewById(R.id.ivItemPhoto);
        rgType = findViewById(R.id.rgPostType);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        // Pré-remplir le téléphone depuis SharedPreferences
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        String phone = sp.getString("phone", "");
        if (!phone.isEmpty()) edtPhone.setText(phone);

        // Catégories (dropdown)
        String[] cats = {getString(R.string.cat_electronics), getString(R.string.cat_keys),
            getString(R.string.cat_documents), getString(R.string.cat_clothing),
            getString(R.string.cat_bags), getString(R.string.cat_jewelry),
            getString(R.string.cat_pets), getString(R.string.cat_other)};
        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cats));

        // MapView — Google Maps
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(b);
        mapView.getMapAsync(map -> {
            gMap = map;
            LatLng paris = new LatLng(selectedLat, selectedLng);
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(paris, 12));
            gMap.addMarker(new MarkerOptions().position(paris));

            // Clic sur la carte pour choisir un lieu
            gMap.setOnMapClickListener(latLng -> {
                gMap.clear();
                gMap.addMarker(new MarkerOptions().position(latLng));
                selectedLat = latLng.latitude;
                selectedLng = latLng.longitude;
                reverseGeocode(latLng.latitude, latLng.longitude);
            });
        });

        // Bouton localisation
        findViewById(R.id.btnLocation).setOnClickListener(v -> {
            if (gMap != null) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    gMap.setMyLocationEnabled(true);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                }
            }
        });

        // Bouton galerie — Intent implicite ACTION_PICK
        findViewById(R.id.btnAddPhoto).setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(pick);
        });

        // Bouton caméra — Intent implicite ACTION_IMAGE_CAPTURE + FileProvider
        findViewById(R.id.btnTakePhoto).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 200);
                return;
            }
            takePhoto();
        });

        // Publier
        findViewById(R.id.btnPostAd).setOnClickListener(v -> publishItem());
    }

    private void takePhoto() {
        try {
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(new Date());
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File file = File.createTempFile("IMG_" + ts, ".jpg", dir);
            photoPath = file.getAbsolutePath();
            Uri uri = FileProvider.getUriForFile(this, "com.example.myapplication.fileprovider", file);
            Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cam.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            cameraLauncher.launch(cam);
        } catch (IOException e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void reverseGeocode(double lat, double lng) {
        try {
            Geocoder geo = new Geocoder(this, Locale.FRANCE);
            List<Address> addrs = geo.getFromLocation(lat, lng, 1);
            if (addrs != null && !addrs.isEmpty()) {
                edtAddress.setText(addrs.get(0).getAddressLine(0));
            }
        } catch (IOException ignored) {}
    }

    private void publishItem() {
        String title = edtTitle.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String category = spinnerCategory.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String type = rgType.getCheckedRadioButtonId() == R.id.rbLost ? "lost" : "found";

        if (title.isEmpty() || desc.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        LostItem item = new LostItem();
        item.setUserId(userId);
        item.setTitle(title);
        item.setDescription(desc);
        item.setAddress(address);
        item.setCategory(category);
        item.setType(type);
        item.setPhone(phone);
        item.setPhotoPath(photoPath);
        item.setUserName(userName);
        item.setLatitude(selectedLat);
        item.setLongitude(selectedLng);

        long id = db.insertItem(item);
        if (id != -1) {
            Toast.makeText(this, R.string.success_post, Toast.LENGTH_SHORT).show();
            // Notification locale
            NotificationHelper.notifyNewPost(this, title);

            // === ALERTE MOT-CLÉ : notifier les AUTRES utilisateurs (1 seule fois par user) ===
            java.util.HashSet<String> notifiedUsers = new java.util.HashSet<>();
            android.database.Cursor alertsCursor = db.getMatchingAlerts(userId, title, desc, category);
            String textToMatch = (title + " " + desc + " " + category).toLowerCase();
            while (alertsCursor.moveToNext()) {
                String alertUserId = alertsCursor.getString(alertsCursor.getColumnIndexOrThrow("userId"));
                String keyword = alertsCursor.getString(alertsCursor.getColumnIndexOrThrow("keyword"));
                if (!notifiedUsers.contains(alertUserId) && textToMatch.contains(keyword)) {
                    db.insertNotification(alertUserId,
                        getString(R.string.notif_match_title),
                        String.format(getString(R.string.notif_match_body), title));
                    notifiedUsers.add(alertUserId);
                }
            }
            alertsCursor.close();

            // === ALERTE PERDU/TROUVÉ : si je publie "trouvé", notifier ceux qui ont "perdu" un objet similaire, et vice versa ===
            String oppositeType = "lost".equals(type) ? "found" : "lost";
            java.util.List<LostItem> oppositeItems = db.getItemsByType(oppositeType);
            for (LostItem other : oppositeItems) {
                if (other.getUserId() != null && !other.getUserId().equals(userId) && !notifiedUsers.contains(other.getUserId())) {
                    String otherText = (other.getTitle() + " " + other.getDescription() + " " + other.getCategory()).toLowerCase();
                    String[] myWords = title.toLowerCase().split("\\s+");
                    for (String word : myWords) {
                        if (word.length() >= 3 && otherText.contains(word)) {
                            String msg = "lost".equals(type)
                                ? "Quelqu\u2019un a trouvé un objet similaire : « " + other.getTitle() + " »"
                                : "Votre objet « " + other.getTitle() + " » a peut-être été retrouvé !";
                            db.insertNotification(other.getUserId(), getString(R.string.notif_match_title), msg);
                            notifiedUsers.add(other.getUserId());
                            break;
                        }
                    }
                }
            }
            // Rediriger vers Mes Objets après publication
            Intent goToMyAds = new Intent(this, MyAdsActivity.class);
            goToMyAds.putExtra("USER_ID", userId);
            goToMyAds.putExtra("USER_NAME", userName);
            goToMyAds.putExtra("USER_EMAIL", userEmail);
            startActivity(goToMyAds);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        } else {
            Toast.makeText(this, R.string.error_post, Toast.LENGTH_SHORT).show();
        }
    }

    // Cycle de vie MapView
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { mapView.onPause(); super.onPause(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onSaveInstanceState(Bundle b) { super.onSaveInstanceState(b); mapView.onSaveInstanceState(b); }
}
