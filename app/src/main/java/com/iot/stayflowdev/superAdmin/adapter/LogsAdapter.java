package com.iot.stayflowdev.superAdmin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.superAdmin.model.LogItem;

import java.util.ArrayList;
import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogViewHolder> {

    private final List<LogItem> allLogItems;
    private List<LogItem> filteredLogItems;
    private String currentFilter = LogItem.CATEGORY_ALL;

    // Interfaz para manejar los clics en los items
    public interface OnItemClickListener {
        void onItemClick(LogItem logItem);
    }

    private OnItemClickListener mListener;

    // Método para establecer el listener desde la actividad
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public LogsAdapter(List<LogItem> logList) {
        this.allLogItems = new ArrayList<>(logList);
        this.filteredLogItems = new ArrayList<>(logList);
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.superadmin_log_item, parent, false);
        return new LogViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogItem item = filteredLogItems.get(position);
        holder.title.setText(item.title);
        holder.timestamp.setText(item.timestamp);
        holder.description.setText(item.description);

        // Establecer el ícono según la categoría
        holder.icon.setImageResource(item.iconResId);

        // Configurar el item actual en el ViewHolder para accederlo desde el click listener
        holder.bindLogItem(item);
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

    /**
     * Obtiene la lista de elementos filtrados actualmente
     * @return Lista de LogItems según el filtro actual
     */
    public List<LogItem> getFilteredItems() {
        return new ArrayList<>(filteredLogItems);
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView title, timestamp, description;
        ImageView icon;
        LogItem logItem;

        public LogViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            title = itemView.findViewById(R.id.logTitle);
            timestamp = itemView.findViewById(R.id.logTimestamp);
            description = itemView.findViewById(R.id.logDescription);
            icon = itemView.findViewById(R.id.logIcon);

            // Configurar el click listener en el itemView
            itemView.setOnClickListener(v -> {
                if (listener != null && logItem != null) {
                    listener.onItemClick(logItem);
                }
            });
        }

        // Método para asignar el LogItem al ViewHolder
        public void bindLogItem(LogItem item) {
            this.logItem = item;
        }
    }
}