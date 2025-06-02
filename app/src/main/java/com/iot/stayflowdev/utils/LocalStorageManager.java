package com.iot.stayflowdev.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocalStorageManager {
    private static final String PREF_NAME = "StayFlowPrefs";
    private static final String KEY_USER_DATA = "user_data";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_LAST_SYNC = "last_sync";
    private static final String KEY_SETTINGS = "settings";
    private static final String KEY_FILTERS = "filters";
    private static final String KEY_LAST_REPORT_CHECK = "last_report_check";

    private final SharedPreferences preferences;
    private final Gson gson;

    public LocalStorageManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Guardar datos del usuario
    public void saveUserData(UserData userData) {
        String json = gson.toJson(userData);
        preferences.edit().putString(KEY_USER_DATA, json).apply();
    }

    // Obtener datos del usuario
    public UserData getUserData() {
        String json = preferences.getString(KEY_USER_DATA, null);
        if (json == null) return null;
        return gson.fromJson(json, UserData.class);
    }

    // Guardar notificaciones
    public void saveNotifications(List<NotificationItem> notifications) {
        String json = gson.toJson(notifications);
        preferences.edit().putString(KEY_NOTIFICATIONS, json).apply();
    }

    // Obtener notificaciones
    public List<NotificationItem> getNotifications() {
        String json = preferences.getString(KEY_NOTIFICATIONS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<NotificationItem>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // Agregar una nueva notificación
    public void addNotification(NotificationItem notification) {
        List<NotificationItem> notifications = getNotifications();
        notifications.add(0, notification); // Agregar al inicio
        saveNotifications(notifications);
    }

    // Marcar notificación como leída
    public void markNotificationAsRead(int position) {
        List<NotificationItem> notifications = getNotifications();
        if (position >= 0 && position < notifications.size()) {
            notifications.get(position).setRead(true);
            saveNotifications(notifications);
        }
    }

    // Guardar configuración
    public void saveSettings(Settings settings) {
        String json = gson.toJson(settings);
        preferences.edit().putString(KEY_SETTINGS, json).apply();
    }

    // Obtener configuración
    public Settings getSettings() {
        String json = preferences.getString(KEY_SETTINGS, null);
        if (json == null) return new Settings(); // Retornar configuración por defecto
        return gson.fromJson(json, Settings.class);
    }

    // Guardar timestamp de última sincronización
    public void saveLastSyncTime(long timestamp) {
        preferences.edit().putLong(KEY_LAST_SYNC, timestamp).apply();
    }

    // Obtener timestamp de última sincronización
    public long getLastSyncTime() {
        return preferences.getLong(KEY_LAST_SYNC, 0);
    }

    // Guardar filtros
    public void saveFilters(FilterSettings filters) {
        String json = gson.toJson(filters);
        preferences.edit().putString(KEY_FILTERS, json).apply();
    }

    // Obtener filtros
    public FilterSettings getFilters() {
        String json = preferences.getString(KEY_FILTERS, null);
        if (json == null) return new FilterSettings();
        return gson.fromJson(json, FilterSettings.class);
    }

    // Guardar timestamp de última revisión de reportes
    public void saveLastReportCheck(long timestamp) {
        preferences.edit().putLong(KEY_LAST_REPORT_CHECK, timestamp).apply();
    }

    // Obtener timestamp de última revisión de reportes
    public long getLastReportCheck() {
        return preferences.getLong(KEY_LAST_REPORT_CHECK, 0);
    }

    // Limpiar todos los datos
    public void clearAll() {
        preferences.edit().clear().apply();
    }

    // Clase para datos del usuario
    public static class UserData {
        private String userId;
        private String name;
        private String email;
        private String role;
        private boolean isLoggedIn;

        // Constructor, getters y setters
        public UserData() {}

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public boolean isLoggedIn() { return isLoggedIn; }
        public void setLoggedIn(boolean loggedIn) { isLoggedIn = loggedIn; }
    }

    // Clase para notificaciones
    public static class NotificationItem {
        private String id;
        private String title;
        private String message;
        private long timestamp;
        private boolean isRead;
        private String type;

        public NotificationItem() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { isRead = read; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    // Clase para configuración
    public static class Settings {
        private boolean notificationsEnabled;
        private boolean darkMode;
        private String language;
        private boolean autoSync;

        public Settings() {
            // Valores por defecto
            this.notificationsEnabled = true;
            this.darkMode = false;
            this.language = "es";
            this.autoSync = true;
        }

        public boolean isNotificationsEnabled() { return notificationsEnabled; }
        public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
        public boolean isDarkMode() { return darkMode; }
        public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public boolean isAutoSync() { return autoSync; }
        public void setAutoSync(boolean autoSync) { this.autoSync = autoSync; }
    }

    // Clase para configuración de filtros
    public static class FilterSettings {
        private String selectedUserType; // "all", "taxista", "hotel", etc.
        private String selectedStatus; // "all", "active", "inactive", etc.
        private String selectedDateRange; // "today", "week", "month", "all"
        private boolean showOnlyCriticalLogs;
        private String selectedHotel;

        public FilterSettings() {
            // Valores por defecto
            this.selectedUserType = "all";
            this.selectedStatus = "all";
            this.selectedDateRange = "all";
            this.showOnlyCriticalLogs = false;
            this.selectedHotel = "all";
        }

        // Getters y setters
        public String getSelectedUserType() { return selectedUserType; }
        public void setSelectedUserType(String selectedUserType) { this.selectedUserType = selectedUserType; }
        public String getSelectedStatus() { return selectedStatus; }
        public void setSelectedStatus(String selectedStatus) { this.selectedStatus = selectedStatus; }
        public String getSelectedDateRange() { return selectedDateRange; }
        public void setSelectedDateRange(String selectedDateRange) { this.selectedDateRange = selectedDateRange; }
        public boolean isShowOnlyCriticalLogs() { return showOnlyCriticalLogs; }
        public void setShowOnlyCriticalLogs(boolean showOnlyCriticalLogs) { this.showOnlyCriticalLogs = showOnlyCriticalLogs; }
        public String getSelectedHotel() { return selectedHotel; }
        public void setSelectedHotel(String selectedHotel) { this.selectedHotel = selectedHotel; }
    }
} 