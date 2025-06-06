package com.iot.stayflowdev.superAdmin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.superAdmin.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private List<User> userListFull;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onDetailsClick(User user);
        void onStatusChanged(User user, boolean isEnabled, String reason);
    }

    public UserAdapter(List<User> userList, OnUserClickListener listener) {
        this.userList = new ArrayList<>(userList);
        this.userListFull = new ArrayList<>(userList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.superadmin_item_user_card, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.textViewUserName.setText(user.getName());
        holder.textViewUserRole.setText(user.getRoleDescription());
        holder.textViewUserStatus.setText(user.isEnabled() ? "Habilitado" : "Deshabilitado");

        // 🔴 Evita que el listener se dispare al hacer setChecked
        holder.switchUserStatus.setOnCheckedChangeListener(null);
        holder.switchUserStatus.setChecked(user.isEnabled());

        // 🟢 Vuelve a asignar el listener después de setChecked
        holder.switchUserStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onStatusChanged(user, isChecked, null);
                // Actualizar el texto del estado
                holder.textViewUserStatus.setText(isChecked ? "Habilitado" : "Deshabilitado");
            }
        });

        // Hacer que toda la tarjeta sea clickeable
        holder.cardUser.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailsClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // Agregar nuevo usuario estructuradamente
    public void addUser(User user) {
        userList.add(0, user);
        userListFull.add(0, user);
        notifyItemInserted(0);
    }

    public void filterByType(String userType) {
        List<User> filteredList = new ArrayList<>();
        
        if (userType.equals("Todos")) {
            filteredList.addAll(userListFull);
        } else {
            for (User user : userListFull) {
                if (user.getRole().equals(userType)) {
                    filteredList.add(user);
                }
            }
        }

        userList.clear();
        userList.addAll(filteredList);
        notifyDataSetChanged();
    }

    public void filterByText(String searchText) {
        List<User> filteredList = new ArrayList<>();
        
        if (searchText.isEmpty()) {
            filteredList.addAll(userListFull);
        } else {
            String searchTextLower = searchText.toLowerCase();
            for (User user : userListFull) {
                if (user.getName().toLowerCase().contains(searchTextLower)) {
                    filteredList.add(user);
                }
            }
        }

        userList.clear();
        userList.addAll(filteredList);
        notifyDataSetChanged();
    }

    public void filterTaxistasByStatus(String status) {
        List<User> filteredList = new ArrayList<>();
        
        for (User user : userListFull) {
            if (user.getRole().equals("Taxista")) {
                if (status.equals("Todos")) {
                    filteredList.add(user);
                } else if (status.equals("Pendientes") && !user.isEnabled()) {
                    filteredList.add(user);
                } else if (status.equals("Habilitados") && user.isEnabled()) {
                    filteredList.add(user);
                }
            }
        }

        userList.clear();
        userList.addAll(filteredList);
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardUser;
        TextView textViewUserName, textViewUserRole, textViewUserStatus;
        SwitchCompat switchUserStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            cardUser = itemView.findViewById(R.id.cardUser);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            textViewUserRole = itemView.findViewById(R.id.textViewUserRole);
            textViewUserStatus = itemView.findViewById(R.id.textViewUserStatus);
            switchUserStatus = itemView.findViewById(R.id.switchUserStatus);
        }
    }
}
