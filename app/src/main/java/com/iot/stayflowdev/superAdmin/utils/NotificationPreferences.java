package com.iot.stayflowdev.superAdmin.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class NotificationPreferences {
    private static final String PREF_NAME = "superadmin_notifications";
    private static final String KEY_REPORTES_ENABLED = "reportes_enabled";
    private static final String KEY_LOGS_ENABLED = "logs_enabled";
    private static final String KEY_PERIODICIDAD_REPORTES = "periodicidad_reportes";
    private static final String KEY_UMBRAL_LOGS = "umbral_logs";

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

    public int getUmbralLogs() {
        return preferences.getInt(KEY_UMBRAL_LOGS, 0);
    }

    public void setUmbralLogs(int umbral) {
        preferences.edit().putInt(KEY_UMBRAL_LOGS, umbral).apply();
    }

    public void clearPreferences() {
        preferences.edit().clear().apply();
    }
} 