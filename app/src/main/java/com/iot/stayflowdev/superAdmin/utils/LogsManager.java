package com.iot.stayflowdev.superAdmin.utils;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogsManager {
    private static final String TAG = "LogsManager";
    private static LogsManager instance;
    private final NotificationPreferences notificationPreferences;
    private final Context context;

    // Tipos de logs que podemos rastrear
    public enum LogType {
        ERROR,
        WARNING,
        INFO,
        DEBUG,
        SYSTEM
    }

    public static class LogEntry {
        private String id;
        private String message;
        private LogType type;
        private long timestamp;
        private String source;
        private boolean reviewed;

        public LogEntry(String message, LogType type, String source) {
            this.id = String.valueOf(System.currentTimeMillis());
            this.message = message;
            this.type = type;
            this.source = source;
            this.timestamp = System.currentTimeMillis();
            this.reviewed = false;
        }

        // Getters y setters
        public String getId() { return id; }
        public String getMessage() { return message; }
        public LogType getType() { return type; }
        public long getTimestamp() { return timestamp; }
        public String getSource() { return source; }
        public boolean isReviewed() { return reviewed; }
        public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }
    }

    private LogsManager(Context context) {
        this.context = context;
        this.notificationPreferences = new NotificationPreferences(context);
    }

    public static synchronized LogsManager getInstance(Context context) {
        if (instance == null) {
            instance = new LogsManager(context.getApplicationContext());
        }
        return instance;
    }

    // Método para registrar un nuevo log
    public void addLog(String message, LogType type, String source) {
        Log.d(TAG, "Agregando log: " + message + " - Tipo: " + type);

        // Incrementar el contador de logs
        notificationPreferences.incrementLogsCount();

        // Crear entrada de log
        LogEntry entry = new LogEntry(message, type, source);

        // Aquí podrías guardar el log en una base de datos o archivo
        // Por ahora solo incrementamos el contador

        int currentCount = notificationPreferences.getLogsCount();
        Log.d(TAG, "Contador de logs actual: " + currentCount);
    }

    // Método para obtener el conteo actual de logs
    public int getLogsCount() {
        return notificationPreferences.getLogsCount();
    }

    // Método para resetear el contador de logs
    public void resetLogsCount() {
        notificationPreferences.resetLogsCount();
        Log.d(TAG, "Contador de logs reseteado");
    }

    // Métodos de conveniencia para registrar diferentes tipos de logs
    public void logError(String message, String source) {
        addLog(message, LogType.ERROR, source);
    }

    public void logWarning(String message, String source) {
        addLog(message, LogType.WARNING, source);
    }

    public void logInfo(String message, String source) {
        addLog(message, LogType.INFO, source);
    }

    public void logSystem(String message, String source) {
        addLog(message, LogType.SYSTEM, source);
    }

    // Método específico para eventos del sistema (sin necesidad de especificar source)
    public void logSystemEvent(String message) {
        addLog(message, LogType.SYSTEM, "SuperAdminActivity");
    }

    // Método para logs de debug
    public void logDebug(String message, String source) {
        addLog(message, LogType.DEBUG, source);
    }

    // Métodos adicionales de conveniencia con source automático
    public void logError(String message) {
        addLog(message, LogType.ERROR, "System");
    }

    public void logWarning(String message) {
        addLog(message, LogType.WARNING, "System");
    }

    public void logInfo(String message) {
        addLog(message, LogType.INFO, "System");
    }
}
