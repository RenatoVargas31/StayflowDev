package com.iot.stayflowdev.superAdmin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.superAdmin.model.UserItem;

import java.util.List;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.UserViewHolder> {

    private List<UserItem> usersList;
    private OnUserClickListener onUserClickListener;

    public interface OnUserClickListener {
        void onUserSelected(UserItem user);
    }

    public UsersListAdapter(List<UserItem> usersList, OnUserClickListener listener) {
        this.usersList = usersList;
        this.onUserClickListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_chat_selection, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserItem user = usersList.get(position);
        holder.bind(user, onUserClickListener);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private TextView emailText;
        private TextView roleText;
        private TextView statusText;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            emailText = itemView.findViewById(R.id.emailText);
            roleText = itemView.findViewById(R.id.roleText);
            statusText = itemView.findViewById(R.id.statusText);
        }

        public void bind(UserItem user, OnUserClickListener listener) {
            nameText.setText(user.getName());
            emailText.setText(user.getEmail());
            roleText.setText(user.getRoleDescription());

            // Estado de conexión
            if (user.isConnected()) {
                statusText.setText("🟢 En línea");
                statusText.setTextColor(itemView.getContext().getResources()
                        .getColor(R.color.md_theme_primary, null));
            } else {
                statusText.setText("⚪ Desconectado");
                statusText.setTextColor(itemView.getContext().getResources()
                        .getColor(R.color.md_theme_onSurfaceVariant, null));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserSelected(user);
                }
            });
        }
    }
}
