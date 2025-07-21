package com.iot.stayflowdev.superAdmin.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class NotificationPreferences {
    private static final String PREF_NAME = "superadmin_notifications";
    private static final String KEY_REPORTES_ENABLED = "reportes_enabled";
    private static final String KEY_LOGS_ENABLED = "logs_enabled";
    private static final String KEY_PERIODICIDAD_REPORTES = "periodicidad_reportes";
    private static final String KEY_PERIODICIDAD_LOGS = "periodicidad_logs";
    private static final String KEY_UMBRAL_LOGS = "umbral_logs";
    private static final String KEY_LOGS_COUNT = "logs_count";

    private final SharedPreferences preferences;

    public NotificationPreferences(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isReportesEnabled() {
        return preferences.getBoolean(KEY_REPORTES_ENABLED, false);
    }

    public void setReportesEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_REPORTES_ENABLED, enabled).apply();
    }

    public boolean isLogsEnabled() {
        return preferences.getBoolean(KEY_LOGS_ENABLED, false);
    }

    public void setLogsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_LOGS_ENABLED, enabled).apply();
    }

    public String getPeriodicidadReportes() {
        return preferences.getString(KEY_PERIODICIDAD_REPORTES, "");
    }

    public void setPeriodicidadReportes(String periodicidad) {
        preferences.edit().putString(KEY_PERIODICIDAD_REPORTES, periodicidad).apply();
    }

    public String getPeriodicidadLogs() {
        return preferences.getString(KEY_PERIODICIDAD_LOGS, "Cada 30 minutos");
    }

    public void setPeriodicidadLogs(String periodicidad) {
        preferences.edit().putString(KEY_PERIODICIDAD_LOGS, periodicidad).apply();
    }

    public int getUmbralLogs() {
        return preferences.getInt(KEY_UMBRAL_LOGS, 0);
    }

    public void setUmbralLogs(int umbral) {
        preferences.edit().putInt(KEY_UMBRAL_LOGS, umbral).apply();
    }

    public int getLogsCount() {
        return preferences.getInt(KEY_LOGS_COUNT, 0);
    }

    public void setLogsCount(int count) {
        preferences.edit().putInt(KEY_LOGS_COUNT, count).apply();
    }

    public void incrementLogsCount() {
        int currentCount = getLogsCount();
        setLogsCount(currentCount + 1);
    }

    public void resetLogsCount() {
        setLogsCount(0);
    }

    public void clearPreferences() {
        preferences.edit().clear().apply();
    }
}
