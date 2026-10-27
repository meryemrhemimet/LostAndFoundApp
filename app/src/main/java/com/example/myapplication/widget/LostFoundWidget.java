package com.example.myapplication.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.example.myapplication.LoginActivity;
import com.example.myapplication.R;
import com.example.myapplication.data.DatabaseHelper;

// Widget d'accueil affichant le nombre d'objets déclarés
public class LostFoundWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        DatabaseHelper db = new DatabaseHelper(ctx);
        int count = db.getItemCount();
        for (int id : ids) {
            RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget_layout);
            views.setTextViewText(R.id.tv_widget_count, String.valueOf(count));
            Intent intent = new Intent(ctx, LoginActivity.class);
            PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_root, pi);
            mgr.updateAppWidget(id, views);
        }
    }
}
