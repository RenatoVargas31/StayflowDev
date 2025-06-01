package com.codebnb.stayflow.superAdmin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codebnb.stayflow.R;
import com.codebnb.stayflow.superAdmin.model.LogItem;

import java.util.ArrayList;
import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogViewHolder> {

    private final List<LogItem> allLogItems;
    private List<LogItem> filteredLogItems;
    private String currentFilter = LogItem.CATEGORY_ALL;

    public LogsAdapter(List<LogItem> logList) {
        this.allLogItems = new ArrayList<>(logList);
        this.filteredLogItems = new ArrayList<>(logList);
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.superadmin_log_item, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogItem item = filteredLogItems.get(position);
        holder.title.setText(item.title);
        holder.timestamp.setText(item.timestamp);
        holder.description.setText(item.description);

        // Establecer el ícono según la categoría
        holder.icon.setImageResource(item.iconResId);
    }

    @Override
    public int getItemCount() {
        return filteredLogItems.size();
    }

    public void filterByCategory(String category) {
        currentFilter = category;
        filteredLogItems.clear();

        if (category.equals(LogItem.CATEGORY_ALL)) {
            filteredLogItems.addAll(allLogItems);
        } else {
            for (LogItem item : allLogItems) {
                if (category.equals(item.category)) {
                    filteredLogItems.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateData(List<LogItem> newLogItems) {
        allLogItems.clear();
        allLogItems.addAll(newLogItems);
        filterByCategory(currentFilter); // Re-apply current filter
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView title, timestamp, description;
        ImageView icon;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.logTitle);
            timestamp = itemView.findViewById(R.id.logTimestamp);
            description = itemView.findViewById(R.id.logDescription);
            icon = itemView.findViewById(R.id.logIcon);
        }
    }
}