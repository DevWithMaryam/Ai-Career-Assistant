package com.maryam.aicareerassistant.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.maryam.aicareerassistant.R;
import com.maryam.aicareerassistant.model.DashboardItem;

import java.util.List;

/**
 * Binds a list of DashboardItem to a RecyclerView grid on the Home screen.
 */
public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.CardViewHolder> {

    public interface OnCardClickListener {
        void onCardClick(DashboardItem item);
    }

    private final List<DashboardItem> items;
    private final OnCardClickListener listener;

    public DashboardAdapter(List<DashboardItem> items, OnCardClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        DashboardItem item = items.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getSubtitle());
        holder.ivIcon.setImageResource(item.getIconRes());

        int tintColor = ContextCompat.getColor(holder.itemView.getContext(), item.getIconTintColorRes());
        holder.iconBackground.setBackgroundTintList(ColorStateList.valueOf(tintColor));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCardClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        View iconBackground;
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvSubtitle;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            iconBackground = itemView.findViewById(R.id.iconBackground);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
        }
    }
}