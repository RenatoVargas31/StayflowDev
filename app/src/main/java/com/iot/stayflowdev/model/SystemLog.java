package com.iot.stayflowdev.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Modelo para logs del sistema que se guardarán en Firebase
 */
public class SystemLog {
    // Categorías de logs
    public static final String CATEGORY_HOTELS = "hotels";
    public static final String CATEGORY_ACCOUNT = "account";
    public static final String CATEGORY_RESERVATION = "reservation";

    // Tipos de acción (específicos para cada categoría)
    public static final String ACTION_USER_CREATED = "user_created";
    public static final String ACTION_USER_LOGIN = "user_login";
    public static final String ACTION_PASSWORD_CHANGE = "password_change";
    public static final String ACTION_PROFILE_UPDATE = "profile_update";

    public static final String ACTION_HOTEL_CREATED = "hotel_created";
    public static final String ACTION_HOTEL_UPDATED = "hotel_updated";

    public static final String ACTION_RESERVATION_CREATED = "reservation_created";
    public static final String ACTION_RESERVATION_CANCELED = "reservation_canceled";
    public static final String ACTION_RESERVATION_UPDATED = "reservation_updated";

    // Campos que se guardarán en Firestore
    @DocumentId
    private String id;
    private String title;
    private String description;
    private String category;
    private String actionType;
    private String userId;  // ID del usuario que realizó la acción
    private String userName;  // Nombre del usuario que realizó la acción
    private String targetId;  // ID del objeto afectado (hotel, usuario, reserva)
    private String targetName;  // Nombre del objeto afectado

    @ServerTimestamp
    private Timestamp timestamp;

    private boolean leido;  // Campo para indicar si el log ha sido leído
    private Map<String, Object> additionalData;  // Datos adicionales específicos de cada tipo de log

    // Constructor vacío requerido para Firestore
    public SystemLog() {
    }

    // Constructor para crear un nuevo log
    public SystemLog(String title, String description, String category,
                    String actionType, String userId, String userName,
                    String targetId, String targetName) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.actionType = actionType;
        this.userId = userId;
        this.userName = userName;
        this.targetId = targetId;
        this.targetName = targetName;
        this.leido = false;  // Por defecto, los logs se crean como no leídos
        this.additionalData = new HashMap<>();
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }

    // Método para añadir datos adicionales al log
    public void addAdditionalData(String key, Object value) {
        if (this.additionalData == null) {
            this.additionalData = new HashMap<>();
        }
        this.additionalData.put(key, value);
    }

    // Método para obtener la fecha como Date (útil para la UI)
    public Date getDate() {
        return timestamp != null ? timestamp.toDate() : null;
    }
}
