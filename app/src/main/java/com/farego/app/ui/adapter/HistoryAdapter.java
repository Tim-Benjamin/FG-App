package com.farego.app.ui.adapter;
// ============================================================
// FILE: app/src/main/java/com/farego/app/ui/adapter/HistoryAdapter.java
// PURPOSE: RecyclerView adapter for UserDashboardFragment.
//          Displays origin → destination, transport type, fare,
//          distance, and human-readable date.
// ============================================================

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.farego.app.databinding.ItemHistoryRowBinding;
import com.farego.app.db.entity.SearchHistory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryAdapter extends ListAdapter<SearchHistory, HistoryAdapter.ViewHolder> {

    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault());

    public HistoryAdapter() {
        super(new DiffUtil.ItemCallback<SearchHistory>() {
            @Override
            public boolean areItemsTheSame(@NonNull SearchHistory a, @NonNull SearchHistory b) {
                return a.id == b.id;
            }
            @Override
            public boolean areContentsTheSame(@NonNull SearchHistory a, @NonNull SearchHistory b) {
                return a.timestamp == b.timestamp && a.estimatedFare == b.estimatedFare;
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryRowBinding binding = ItemHistoryRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemHistoryRowBinding binding;

        ViewHolder(ItemHistoryRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SearchHistory item) {
            binding.tvRoute.setText(item.originLabel + "  →  " + item.destinationLabel);
            binding.tvTransportType.setText(item.transportType);
            binding.tvFare.setText(String.format("GH₵ %.0f", item.estimatedFare));
            binding.tvDistance.setText(String.format("%.1f km", item.distanceKm));
            binding.tvDate.setText(SDF.format(new Date(item.timestamp)));
        }
    }
}