package com.maryam.aicareerassistant.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maryam.aicareerassistant.R;
import com.maryam.aicareerassistant.model.ResumeAnalysis;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<ResumeAnalysis> items;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public HistoryAdapter(List<ResumeAnalysis> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ResumeAnalysis item = items.get(position);
        holder.tvScore.setText(String.valueOf(item.getScore()));
        holder.tvDate.setText(sdf.format(new Date(item.getTimestamp())));

        List<String> strengths = item.getStrengths();
        if (strengths != null && !strengths.isEmpty()) {
            holder.tvStrength.setText("✓ " + strengths.get(0));
        } else {
            holder.tvStrength.setText("Resume Analysis");
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvScore, tvDate, tvStrength;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvScore = itemView.findViewById(R.id.tvScoreBadge);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvStrength = itemView.findViewById(R.id.tvHistoryStrength);
        }
    }
}