package com.iot.stayflowdev.adminHotel.model;

import com.google.firebase.Timestamp;

public class ChatMessage {

    private String id;
    private String chatId;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverName;
    private String content;
    private Timestamp timestamp;
    private boolean read;

    // Constructor vacío requerido por Firestore
    public ChatMessage() {}

    // Constructor completo para uso al enviar un mensaje
    public ChatMessage(String chatId, String senderId, String senderName,
                       String receiverId, String receiverName, String content,
                       Timestamp timestamp, boolean read) {
        this.chatId = chatId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.content = content;
        this.timestamp = timestamp;
        this.read = read;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getChatId() { return chatId; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getReceiverId() { return receiverId; }
    public String getReceiverName() { return receiverName; }
    public String getContent() { return content; }
    public Timestamp getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }

    public void setRead(boolean read) { this.read = read; }

    // Método para generar ID único de chat entre dos usuarios
    public static String generateChatId(String userId1, String userId2) {
        return (userId1.compareTo(userId2) < 0)
                ? userId1 + "_" + userId2
                : userId2 + "_" + userId1;
    }
}
