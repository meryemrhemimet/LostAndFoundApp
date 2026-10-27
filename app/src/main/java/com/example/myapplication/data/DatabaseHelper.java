package com.example.myapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.myapplication.model.LostItem;
import com.example.myapplication.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "lostfound.db";
    private static final int DB_VERSION = 4;
    private final Context context;

    public DatabaseHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
        this.context = ctx.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, email TEXT UNIQUE, password TEXT, phone TEXT)");
        db.execSQL("CREATE TABLE items (id INTEGER PRIMARY KEY AUTOINCREMENT, userId TEXT, title TEXT, description TEXT, " +
            "address TEXT, category TEXT, type TEXT, status TEXT DEFAULT 'active', photoPath TEXT, phone TEXT, " +
            "userName TEXT, latitude REAL DEFAULT 0, longitude REAL DEFAULT 0, createdAt INTEGER)");
        db.execSQL("CREATE TABLE notifications (id INTEGER PRIMARY KEY AUTOINCREMENT, userId TEXT, title TEXT, body TEXT, " +
            "isRead INTEGER DEFAULT 0, createdAt INTEGER)");
        db.execSQL("CREATE TABLE search_alerts (id INTEGER PRIMARY KEY AUTOINCREMENT, userId TEXT, keyword TEXT, " +
            "createdAt INTEGER, UNIQUE(userId, keyword) ON CONFLICT REPLACE)");
        db.execSQL("INSERT INTO users (username, email, password, phone) VALUES ('Jean Dupont', 'jean@test.com', '123456', '0612345678')");
        insertDemoItems(db);
    }

    private void insertDemoItems(SQLiteDatabase db) {
        int[] rawResIds = { R.raw.cles, R.raw.iphone, R.raw.sac, R.raw.portfeuille, R.raw.chat };
        String[][] items = {
            {"Clés trouvées", "Trousseau de 3 clés avec porte-clés rouge", "Gare de Lyon, Paris", "Clés", "found", "0612345678", "Jean Dupont", "48.8443", "2.3744"},
            {"iPhone 14 perdu", "iPhone 14 noir avec coque bleue", "Métro Châtelet, Paris", "Électronique", "lost", "0612345678", "Jean Dupont", "48.8584", "2.3471"},
            {"Sac à dos trouvé", "Sac Eastpak gris avec livres universitaires", "Jardin du Luxembourg", "Sacs", "found", "0612345678", "Jean Dupont", "48.8462", "2.3372"},
            {"Portefeuille perdu", "Portefeuille marron en cuir, contient carte étudiant", "Place Bellecour, Lyon", "Documents", "lost", "0612345678", "Jean Dupont", "45.7578", "4.8320"},
            {"Chat perdu", "Chat tigré gris, 3 ans, collier bleu", "Quartier Gambetta, Nice", "Animaux", "lost", "0612345678", "Jean Dupont", "43.7102", "7.2620"},
        };
        long now = System.currentTimeMillis();
        for (int i = 0; i < items.length; i++) {
            String photoPath = copyRawPhoto(rawResIds[i], "demo_photo_" + i + ".jpg");
            ContentValues cv = new ContentValues();
            cv.put("userId", "1"); cv.put("title", items[i][0]); cv.put("description", items[i][1]);
            cv.put("address", items[i][2]); cv.put("category", items[i][3]); cv.put("type", items[i][4]);
            cv.put("status", "active"); cv.put("phone", items[i][5]); cv.put("userName", items[i][6]);
            cv.put("latitude", Double.parseDouble(items[i][7])); cv.put("longitude", Double.parseDouble(items[i][8]));
            cv.put("createdAt", now - (i * 3600000L)); cv.put("photoPath", photoPath);
            db.insert("items", null, cv);
        }
    }

    private String copyRawPhoto(int rawResId, String fileName) {
        try {
            File dest = new File(context.getFilesDir(), fileName);
            if (dest.exists()) return dest.getAbsolutePath();
            InputStream in = context.getResources().openRawResource(rawResId);
            FileOutputStream out = new FileOutputStream(dest);
            byte[] buf = new byte[4096]; int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            in.close(); out.close();
            return dest.getAbsolutePath();
        } catch (Exception e) { return null; }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int o, int n) {
        db.execSQL("DROP TABLE IF EXISTS search_alerts");
        db.execSQL("DROP TABLE IF EXISTS notifications");
        db.execSQL("DROP TABLE IF EXISTS items");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // ===== USERS =====
    public boolean registerUser(String username, String email, String password, String phone) {
        ContentValues cv = new ContentValues();
        cv.put("username", username); cv.put("email", email); cv.put("password", password); cv.put("phone", phone);
        return getWritableDatabase().insert("users", null, cv) != -1;
    }
    public boolean emailExists(String email) {
        Cursor c = getReadableDatabase().rawQuery("SELECT id FROM users WHERE email=?", new String[]{email});
        boolean exists = c.getCount() > 0; c.close(); return exists;
    }
    public Cursor loginUser(String email, String password) {
        return getReadableDatabase().rawQuery("SELECT * FROM users WHERE email=? AND password=?", new String[]{email, password});
    }
    public Cursor getUserById(String userId) {
        return getReadableDatabase().rawQuery("SELECT * FROM users WHERE id=?", new String[]{userId});
    }

    // ===== ITEMS =====
    public long insertItem(LostItem item) {
        ContentValues cv = new ContentValues();
        cv.put("userId", item.getUserId()); cv.put("title", item.getTitle()); cv.put("description", item.getDescription());
        cv.put("address", item.getAddress()); cv.put("category", item.getCategory()); cv.put("type", item.getType());
        cv.put("status", "active"); cv.put("photoPath", item.getPhotoPath()); cv.put("phone", item.getPhone());
        cv.put("userName", item.getUserName()); cv.put("latitude", item.getLatitude()); cv.put("longitude", item.getLongitude());
        cv.put("createdAt", System.currentTimeMillis());
        return getWritableDatabase().insert("items", null, cv);
    }
    public List<LostItem> getAllItems() { return queryItems("SELECT * FROM items ORDER BY createdAt DESC", null); }
    public List<LostItem> getItemsByType(String type) {
        return queryItems("SELECT * FROM items WHERE type=? ORDER BY createdAt DESC", new String[]{type});
    }
    public List<LostItem> getItemsByUser(String userId) {
        return queryItems("SELECT * FROM items WHERE userId=? ORDER BY createdAt DESC", new String[]{userId});
    }
    public List<LostItem> searchItems(String query) {
        String q = "%" + query + "%";
        return queryItems("SELECT * FROM items WHERE title LIKE ? OR description LIKE ? OR category LIKE ? ORDER BY createdAt DESC", new String[]{q, q, q});
    }
    public List<LostItem> getItemsByStatus(String userId, String status) {
        return queryItems("SELECT * FROM items WHERE userId=? AND status=? ORDER BY createdAt DESC", new String[]{userId, status});
    }
    public LostItem getItemById(long id) {
        List<LostItem> items = queryItems("SELECT * FROM items WHERE id=?", new String[]{String.valueOf(id)});
        return items.isEmpty() ? null : items.get(0);
    }
    public void updateStatus(long id, String status) {
        ContentValues cv = new ContentValues(); cv.put("status", status);
        getWritableDatabase().update("items", cv, "id=?", new String[]{String.valueOf(id)});
    }
    public void deleteItem(long id) { getWritableDatabase().delete("items", "id=?", new String[]{String.valueOf(id)}); }
    public int getItemCount() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM items", null);
        c.moveToFirst(); int n = c.getInt(0); c.close(); return n;
    }
    public int getCountByUserAndStatus(String userId, String status) {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM items WHERE userId=? AND status=?", new String[]{userId, status});
        c.moveToFirst(); int n = c.getInt(0); c.close(); return n;
    }

    // ===== NOTIFICATIONS =====
    public void insertNotification(String userId, String title, String body) {
        ContentValues cv = new ContentValues();
        cv.put("userId", userId); cv.put("title", title); cv.put("body", body);
        cv.put("isRead", 0); cv.put("createdAt", System.currentTimeMillis());
        getWritableDatabase().insert("notifications", null, cv);
    }

    public Cursor getNotifications(String userId) {
        return getReadableDatabase().rawQuery(
            "SELECT * FROM notifications WHERE userId=? ORDER BY createdAt DESC LIMIT 50", new String[]{userId});
    }

    public int getUnreadNotifCount(String userId) {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT COUNT(*) FROM notifications WHERE userId=? AND isRead=0", new String[]{userId});
        c.moveToFirst(); int n = c.getInt(0); c.close(); return n;
    }

    public void markAllNotifsRead(String userId) {
        ContentValues cv = new ContentValues(); cv.put("isRead", 1);
        getWritableDatabase().update("notifications", cv, "userId=?", new String[]{userId});
    }

    public void clearNotifications(String userId) {
        getWritableDatabase().delete("notifications", "userId=?", new String[]{userId});
    }

    // ===== ALERTES DE RECHERCHE =====
    // Sauvegarde le mot-clé de recherche d'un utilisateur
    public void saveSearchAlert(String userId, String keyword) {
        ContentValues cv = new ContentValues();
        cv.put("userId", userId); cv.put("keyword", keyword.toLowerCase().trim());
        cv.put("createdAt", System.currentTimeMillis());
        getWritableDatabase().insertWithOnConflict("search_alerts", null, cv,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Trouve tous les utilisateurs AUTRES que l'auteur dont un mot-clé matche le nouvel objet
    public Cursor getMatchingAlerts(String excludeUserId, String title, String description, String category) {
        String text = (title + " " + description + " " + category).toLowerCase();
        // Récupère tous les mots-clés des autres utilisateurs
        return getReadableDatabase().rawQuery(
            "SELECT DISTINCT userId, keyword FROM search_alerts WHERE userId != ?",
            new String[]{excludeUserId});
    }

    private List<LostItem> queryItems(String sql, String[] args) {
        List<LostItem> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(sql, args);
        while (c.moveToNext()) {
            LostItem item = new LostItem();
            item.setId(c.getLong(c.getColumnIndexOrThrow("id")));
            item.setUserId(c.getString(c.getColumnIndexOrThrow("userId")));
            item.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
            item.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
            item.setAddress(c.getString(c.getColumnIndexOrThrow("address")));
            item.setCategory(c.getString(c.getColumnIndexOrThrow("category")));
            item.setType(c.getString(c.getColumnIndexOrThrow("type")));
            item.setStatus(c.getString(c.getColumnIndexOrThrow("status")));
            item.setPhotoPath(c.getString(c.getColumnIndexOrThrow("photoPath")));
            item.setPhone(c.getString(c.getColumnIndexOrThrow("phone")));
            item.setUserName(c.getString(c.getColumnIndexOrThrow("userName")));
            item.setLatitude(c.getDouble(c.getColumnIndexOrThrow("latitude")));
            item.setLongitude(c.getDouble(c.getColumnIndexOrThrow("longitude")));
            item.setCreatedAt(c.getLong(c.getColumnIndexOrThrow("createdAt")));
            list.add(item);
        }
        c.close(); return list;
    }
}
