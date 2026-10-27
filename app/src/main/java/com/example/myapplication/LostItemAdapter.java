package com.example.myapplication;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.model.LostItem;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Adaptateur RecyclerView pour la liste d'objets
public class LostItemAdapter extends RecyclerView.Adapter<LostItemAdapter.VH> {
    private List<LostItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener { void onItemClick(LostItem item); }

    public LostItemAdapter(OnItemClickListener listener) { this.listener = listener; }

    public void setItems(List<LostItem> newItems) { this.items = newItems; notifyDataSetChanged(); }
    public List<LostItem> getItems() { return items; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lost_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(items.get(pos), listener); }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivPhoto; TextView tvTitle, tvType, tvCategory, tvAddress, tvDate;
        VH(View v) {
            super(v);
            ivPhoto = v.findViewById(R.id.ivItemPhoto);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvType = v.findViewById(R.id.tvType);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvAddress = v.findViewById(R.id.tvAddress);
            tvDate = v.findViewById(R.id.tvDate);
        }
        void bind(LostItem item, OnItemClickListener listener) {
            Context ctx = itemView.getContext();
            tvTitle.setText(item.getTitle());
            tvCategory.setText(item.getCategory());
            tvAddress.setText(item.getAddress());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);
            tvDate.setText(sdf.format(new Date(item.getCreatedAt())));

            boolean isLost = "lost".equals(item.getType());
            tvType.setText(isLost ? ctx.getString(R.string.lost) : ctx.getString(R.string.found));
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(16f);
            bg.setColor(ContextCompat.getColor(ctx, isLost ? R.color.lostColor : R.color.foundColor));
            tvType.setBackground(bg);
            tvType.setTextColor(ContextCompat.getColor(ctx, R.color.white));

            // Photo
            if (item.getPhotoPath() != null && !item.getPhotoPath().isEmpty()) {
                File f = new File(item.getPhotoPath());
                if (f.exists()) {
                    Glide.with(ctx).load(f).centerCrop().into(ivPhoto);
                } else {
                    Glide.with(ctx).load(Uri.parse(item.getPhotoPath())).centerCrop()
                        .placeholder(R.drawable.ic_placeholder).into(ivPhoto);
                }
            } else { ivPhoto.setImageResource(R.drawable.ic_placeholder); }

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
