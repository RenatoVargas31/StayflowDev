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
import com.iot.stayflowdev.model.User;

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

        // Usar el nombre completo o construirlo a partir de nombres y apellidos
        holder.textViewUserName.setText(user.getName());

        // Mostrar el rol con descripción legible
        holder.textViewUserRole.setText(user.getRoleDescription());

        // Mostrar el estado
        holder.textViewUserStatus.setText(user.isEnabled() ? "Habilitado" : "Deshabilitado");

        // 🔴 Evitar que el listener se dispare al hacer setChecked
        holder.switchUserStatus.setOnCheckedChangeListener(null);
        holder.switchUserStatus.setChecked(user.isEnabled());

        // 🟢 Volver a asignar el listener después de setChecked
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

    // Actualizar la lista completa
    public void updateUserList(List<User> newUserList) {
        userList.clear();
        userList.addAll(newUserList);
        notifyDataSetChanged();
    }

    // Actualizar tanto la lista completa como la lista original (para filtros)
    public void updateFullList(List<User> newUserList) {
        userList.clear();
        userList.addAll(newUserList);
        userListFull.clear();
        userListFull.addAll(newUserList);
        notifyDataSetChanged();
    }

    public void filterByType(String userType) {
        List<User> filteredList = new ArrayList<>();
        
        if (userType.equals("Todos")) {
            filteredList.addAll(userListFull);
        } else {
            for (User user : userListFull) {
                if (user.getRol() != null && user.getRol().equals(userType)) {
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
                if ((user.getNombres() != null && user.getNombres().toLowerCase().contains(searchTextLower)) ||
                    (user.getApellidos() != null && user.getApellidos().toLowerCase().contains(searchTextLower)) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchTextLower))) {
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
        
        // Convertir el tipo de estado (string) a un valor booleano
        boolean estadoValue = "activo".equals(status);

        for (User user : userListFull) {
            if ("driver".equals(user.getRol())) { // Actualizado de "taxista" a "driver"
                if ("Todos".equals(status)) {
                    filteredList.add(user);
                } else if ("pendiente".equals(status) && !user.isEstado()) {
                    // Estado pendiente = false
                    filteredList.add(user);
                } else if ("activo".equals(status) && user.isEstado()) {
                    // Estado activo = true
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
