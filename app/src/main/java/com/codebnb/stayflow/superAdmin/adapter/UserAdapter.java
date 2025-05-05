package com.codebnb.stayflow.superAdmin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.codebnb.stayflow.R;
import com.codebnb.stayflow.superAdmin.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private List<User> userListFull;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onDetailsClick(int position);
        void onStatusChanged(int position, boolean isEnabled);
    }

    public UserAdapter(List<User> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.userListFull = new ArrayList<>(userList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_card, parent, false);
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
                listener.onStatusChanged(holder.getAdapterPosition(), isChecked);
                holder.textViewUserStatus.setText(isChecked ? "Habilitado" : "Deshabilitado");
            }
        });

        holder.imageViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailsClick(holder.getAdapterPosition());
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
        userList.clear();

        if (userType.equals("Todos")) {
            userList.addAll(userListFull);
        } else {
            for (User user : userListFull) {
                if (user.getRole().equals(userType)) {
                    userList.add(user);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void filterByText(String searchText) {
        userList.clear();

        if (searchText.isEmpty()) {
            userList.addAll(userListFull);
        } else {
            String searchTextLower = searchText.toLowerCase();
            for (User user : userListFull) {
                if (user.getName().toLowerCase().contains(searchTextLower)) {
                    userList.add(user);
                }
            }
        }

        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUserName, textViewUserRole, textViewUserStatus, textViewDetails;
        SwitchCompat switchUserStatus;
        ImageView imageViewDetails;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            textViewUserRole = itemView.findViewById(R.id.textViewUserRole);
            textViewUserStatus = itemView.findViewById(R.id.textViewUserStatus);
            textViewDetails = itemView.findViewById(R.id.textViewDetails);
            switchUserStatus = itemView.findViewById(R.id.switchUserStatus);
            imageViewDetails = itemView.findViewById(R.id.imageViewDetails);
        }
    }
}
