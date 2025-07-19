package com.iot.stayflowdev.superAdmin.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.Message;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private List<Message> messagesList;
    private String currentUserId;

    public MessagesAdapter(List<Message> messagesList, String currentUserId) {
        this.messagesList = messagesList;
        this.currentUserId = currentUserId;
    }

    // Método para agregar un nuevo mensaje y notificar inmediatamente
    public void addMessage(Message message) {
        messagesList.add(message);
        notifyItemInserted(messagesList.size() - 1);
    }

    // Método para actualizar todos los mensajes
    public void updateMessages(List<Message> newMessages) {
        int oldSize = messagesList.size();
        messagesList.clear();
        messagesList.addAll(newMessages);

        if (oldSize == 0) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeRemoved(0, oldSize);
            notifyItemRangeInserted(0, newMessages.size());
        }
    }

    // Método para actualizar un mensaje específico (útil para cambios de estado de lectura)
    public void updateMessage(int position, Message message) {
        if (position >= 0 && position < messagesList.size()) {
            messagesList.set(position, message);
            notifyItemChanged(position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messagesList.get(position);

        // Debug logging para identificar el problema
        Log.d("MessagesAdapter", "=== MENSAJE " + position + " ===");
        Log.d("MessagesAdapter", "Sender ID: " + message.getSenderId());
        Log.d("MessagesAdapter", "Current User ID: " + currentUserId);
        Log.d("MessagesAdapter", "Son iguales: " + message.getSenderId().equals(currentUserId));
        Log.d("MessagesAdapter", "Sender Name: " + message.getSenderName());
        Log.d("MessagesAdapter", "Content: " + message.getContent());

        if (message.getSenderId().equals(currentUserId)) {
            Log.d("MessagesAdapter", "Tipo: MENSAJE ENVIADO");
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            Log.d("MessagesAdapter", "Tipo: MENSAJE RECIBIDO");
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messagesList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView timeText;
        private TextView senderNameText;
        private TextView readStatusText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timeText);
            senderNameText = itemView.findViewById(R.id.senderNameText);
            readStatusText = itemView.findViewById(R.id.readStatusText);
        }

        public void bind(Message message) {
            // Establecer contenido del mensaje
            messageText.setText(message.getContent());

            // Formato de tiempo
            if (message.getTimestamp() != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                timeText.setText(timeFormat.format(message.getDate()));
            } else {
                timeText.setText("");
            }

            // Mostrar nombre del remitente solo en mensajes recibidos
            if (senderNameText != null) {
                if (!message.getSenderId().equals(currentUserId)) {
                    senderNameText.setText(message.getSenderName());
                    senderNameText.setVisibility(View.VISIBLE);
                } else {
                    senderNameText.setVisibility(View.GONE);
                }
            }

            // Mostrar estado de lectura solo en mensajes enviados
            if (readStatusText != null) {
                if (message.getSenderId().equals(currentUserId)) {
                    if (message.isRead()) {
                        readStatusText.setText("Leído");
                        readStatusText.setVisibility(View.VISIBLE);
                    } else {
                        readStatusText.setText("Enviado");
                        readStatusText.setVisibility(View.VISIBLE);
                    }
                } else {
                    readStatusText.setVisibility(View.GONE);
                }
            }
        }
    }
}
