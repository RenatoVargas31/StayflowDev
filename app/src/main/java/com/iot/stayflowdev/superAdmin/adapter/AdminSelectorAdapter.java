package com.iot.stayflowdev.superAdmin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.User;

import java.util.List;

public class AdminSelectorAdapter extends RecyclerView.Adapter<AdminSelectorAdapter.AdminViewHolder> {

    public interface OnAdminSelectedListener {
        void onAdminSelected(User admin);
    }

    private List<User> adminList;
    private OnAdminSelectedListener listener;

    public AdminSelectorAdapter(List<User> adminList, OnAdminSelectedListener listener) {
        this.adminList = adminList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_selector, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        User admin = adminList.get(position);
        // Usar los nombres corretos de los campos según la clase User
        String fullName = admin.getNombres() + " " + admin.getApellidos();
        holder.textViewAdminName.setText(fullName);
        holder.textViewAdminEmail.setText(admin.getEmail());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAdminSelected(admin);
            }
        });
    }

    @Override
    public int getItemCount() {
        return adminList.size();
    }

    static class AdminViewHolder extends RecyclerView.ViewHolder {
        TextView textViewAdminName;
        TextView textViewAdminEmail;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAdminName = itemView.findViewById(R.id.textViewAdminName);
            textViewAdminEmail = itemView.findViewById(R.id.textViewAdminEmail);
        }
    }
}
