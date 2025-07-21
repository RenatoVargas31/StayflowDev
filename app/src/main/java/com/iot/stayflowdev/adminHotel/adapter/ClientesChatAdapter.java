package com.iot.stayflowdev.adminHotel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.model.ClienteItem;

import java.util.List;

public class ClientesChatAdapter extends RecyclerView.Adapter<ClientesChatAdapter.ClienteViewHolder> {

    public interface OnClienteClickListener {
        void onClienteClick(ClienteItem cliente);
    }

    private List<ClienteItem> clientes;
    private OnClienteClickListener listener;

    public ClientesChatAdapter(List<ClienteItem> clientes, OnClienteClickListener listener) {
        this.clientes = clientes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cliente_chat, parent, false);
        return new ClienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClienteViewHolder holder, int position) {
        ClienteItem cliente = clientes.get(position);
        holder.bind(cliente, listener);
    }

    @Override
    public int getItemCount() {
        return clientes.size();
    }

    static class ClienteViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText, emailText, statusText, avatarText;

        public ClienteViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            emailText = itemView.findViewById(R.id.emailText);
            statusText = itemView.findViewById(R.id.statusText);
            avatarText = itemView.findViewById(R.id.avatarText);
        }

        public void bind(ClienteItem cliente, OnClienteClickListener listener) {
            nameText.setText(cliente.getNombre());
            emailText.setText(cliente.getEmail());
            statusText.setText(cliente.isConectado() ? "🟢 En línea" : "⚪ Desconectado");
            statusText.setTextColor(itemView.getResources().getColor(
                    cliente.isConectado() ? R.color.md_theme_primary : R.color.md_theme_onSurfaceVariant,
                    null
            ));
            avatarText.setText(cliente.getNombre().substring(0, 1).toUpperCase());

            itemView.setOnClickListener(v -> listener.onClienteClick(cliente));
        }
    }
    public void updateData(List<ClienteItem> nuevos) {
        this.clientes = nuevos;
        notifyDataSetChanged();
    }
}

