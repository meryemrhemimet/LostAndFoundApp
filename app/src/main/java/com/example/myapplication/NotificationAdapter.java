package com.example.myapplication;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {
    private Cursor cursor;

    public void setCursor(Cursor c) {
        if (this.cursor != null) this.cursor.close();
        this.cursor = c;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        if (cursor != null && cursor.moveToPosition(pos)) {
            h.tvTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            h.tvBody.setText(cursor.getString(cursor.getColumnIndexOrThrow("body")));
            long ts = cursor.getLong(cursor.getColumnIndexOrThrow("createdAt"));
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE);
            h.tvTime.setText(sdf.format(new Date(ts)));
            boolean unread = cursor.getInt(cursor.getColumnIndexOrThrow("isRead")) == 0;
            h.dot.setVisibility(unread ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override public int getItemCount() { return cursor != null ? cursor.getCount() : 0; }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvBody, tvTime; View dot;
        VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvNotifTitle);
            tvBody = v.findViewById(R.id.tvNotifBody);
            tvTime = v.findViewById(R.id.tvNotifTime);
            dot = v.findViewById(R.id.dotUnread);
        }
    }
}
