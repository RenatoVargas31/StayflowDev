package com.iot.stayflowdev.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Modelo para mensajes del sistema de chat
 */
public class Message {
    @DocumentId
    private String id;
    private String chatId; // ID único del chat entre dos usuarios
    private String senderId; // ID del usuario que envía
    private String senderName; // Nombre del usuario que envía
    private String receiverId; // ID del usuario que recibe
    private String receiverName; // Nombre del usuario que recibe
    private String content; // Contenido del mensaje
    private String messageType; // text, image, file, etc.
    private boolean isRead; // Si el mensaje ha sido leído

    @ServerTimestamp
    private Timestamp timestamp;

    // Constructor vacío para Firestore
    public Message() {
    }

    // Constructor para crear un nuevo mensaje
    public Message(String chatId, String senderId, String senderName,
                   String receiverId, String receiverName, String content) {
        this.chatId = chatId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.content = content;
        this.messageType = "text";
        this.isRead = false;
    }

    // Método para generar ID de chat único entre dos usuarios
    public static String generateChatId(String userId1, String userId2) {
        // Ordenar alfabéticamente para que siempre sea el mismo ID independientemente del orden
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    // Método para obtener la fecha como Date (útil para la UI)
    public Date getDate() {
        return timestamp != null ? timestamp.toDate() : null;
    }
}
