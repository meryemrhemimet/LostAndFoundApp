package com.example.myapplication.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.core.app.NotificationCompat;
import com.example.myapplication.LoginActivity;
import com.example.myapplication.R;
import com.example.myapplication.data.DatabaseHelper;

/**
 * Notifications : system tray + stockées en DB pour affichage dans le profil.
 */
public class NotificationHelper {
    private static final String CHANNEL_ID = "lostfound_alerts";

    public static void createChannel(Context ctx) {
        NotificationChannel ch = new NotificationChannel(CHANNEL_ID,
            ctx.getString(R.string.notif_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
        ch.setDescription(ctx.getString(R.string.notif_channel_desc));
        ch.enableVibration(true);
        ctx.getSystemService(NotificationManager.class).createNotificationChannel(ch);
    }

    private static boolean isEnabled(Context ctx) {
        return ctx.getSharedPreferences("UserSession", Context.MODE_PRIVATE).getBoolean("notifEnabled", true);
    }

    private static String getUserId(Context ctx) {
        return ctx.getSharedPreferences("UserSession", Context.MODE_PRIVATE).getString("userId", "");
    }

    private static void send(Context ctx, String title, String body, int icon) {
        // Always store in DB
        String uid = getUserId(ctx);
        if (!uid.isEmpty()) {
            new DatabaseHelper(ctx).insertNotification(uid, title, body);
        }
        // System notification only if enabled
        if (!isEnabled(ctx)) return;
        Intent intent = new Intent(ctx, LoginActivity.class);
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(icon).setContentTitle(title).setContentText(body)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true).setContentIntent(pi);
        ctx.getSystemService(NotificationManager.class)
            .notify((int)(System.currentTimeMillis() % Integer.MAX_VALUE), b.build());
    }

    public static void notifyWelcome(Context c, String name) {
        send(c, c.getString(R.string.notif_welcome_title),
            String.format(c.getString(R.string.notif_welcome_body), name), R.drawable.baseline_person_24);
    }
    public static void notifyNewPost(Context c, String title) {
        send(c, c.getString(R.string.notif_post_title),
            String.format(c.getString(R.string.notif_post_body), title), R.drawable.baseline_add_24);
    }
    public static void notifyResolved(Context c, String title) {
        send(c, c.getString(R.string.notif_resolved_title),
            String.format(c.getString(R.string.notif_resolved_body), title), R.drawable.ic_check_circle);
    }
    public static void notifyDeleted(Context c, String title) {
        send(c, c.getString(R.string.notif_deleted_title),
            String.format(c.getString(R.string.notif_deleted_body), title), R.drawable.ic_delete_outline);
    }
    public static void notifyMatch(Context c, String title) {
        send(c, c.getString(R.string.notif_match_title),
            String.format(c.getString(R.string.notif_match_body), title), R.drawable.ic_location);
    }
}
