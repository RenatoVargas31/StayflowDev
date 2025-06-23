package com.iot.stayflowdev.superAdmin.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.concurrent.TimeUnit;

public class PeriodicNotificationManager {
    private static final String TAG = "PeriodicNotificationManager";
    private static final String ACTION_CHECK_REPORTS = "com.iot.stayflowdev.utils.ReportsCheckReceiver";
    private static final String ACTION_CHECK_LOGS = "com.iot.stayflowdev.utils.LogsCheckReceiver";

    // Estos intervalos por defecto ya no se usan directamente
    // Se utilizarán las preferencias del usuario, y estos valores solo como fallback
    private static final long DEFAULT_REPORTS_CHECK_INTERVAL = 15 * 60 * 1000; // 15 minutos por defecto
    private static final long DEFAULT_LOGS_CHECK_INTERVAL = 15 * 60 * 1000; // 15 minutos por defecto

    private final Context context;
    private final NotificationHelper notificationHelper;
    private final LocalStorageManager localStorageManager;
    private final AlarmManager alarmManager;
    private final NotificationPreferences notificationPreferences;

    public PeriodicNotificationManager(Context context) {
        this.context = context;
        this.notificationHelper = new NotificationHelper(context);
        this.localStorageManager = new LocalStorageManager(context);
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.notificationPreferences = new NotificationPreferences(context);

        // Registrar los receivers
        ContextCompat.registerReceiver(context, reportsReceiver, new IntentFilter(ACTION_CHECK_REPORTS), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(context, logsReceiver, new IntentFilter(ACTION_CHECK_LOGS), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    public void startPeriodicChecks() {
        // Verificar si las notificaciones están habilitadas según las preferencias
        boolean reportesEnabled = notificationPreferences.isReportesEnabled();
        boolean logsEnabled = notificationPreferences.isLogsEnabled();

        Log.d(TAG, "Iniciando verificaciones periódicas. Reportes habilitados: " + reportesEnabled +
              ", Logs habilitados: " + logsEnabled);

        if (reportesEnabled) {
            scheduleReportsCheck();
        }

        if (logsEnabled) {
            scheduleLogsCheck();
        }
    }

    private void scheduleReportsCheck() {
        // Obtener el intervalo desde las preferencias
        long interval = getIntervalFromPreferences();
        Log.d(TAG, "Programando verificación de reportes cada " + (interval / 60000) + " minutos");

        Intent reportsIntent = new Intent(context, ReportsCheckReceiver.class);
        reportsIntent.setAction(ACTION_CHECK_REPORTS);
        PendingIntent reportsPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                reportsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Cancelar cualquier alarma existente antes de programar una nueva
        alarmManager.cancel(reportsPendingIntent);

        // Programar las alarmas con el intervalo configurado por el usuario
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + interval,
                interval,
                reportsPendingIntent
        );
    }

    private void scheduleLogsCheck() {
        // Obtener el intervalo desde las preferencias (mismo que para reportes)
        long interval = getIntervalFromPreferences();
        Log.d(TAG, "Programando verificación de logs cada " + (interval / 60000) + " minutos");

        Intent logsIntent = new Intent(context, LogsCheckReceiver.class);
        logsIntent.setAction(ACTION_CHECK_LOGS);
        PendingIntent logsPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                logsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Cancelar cualquier alarma existente antes de programar una nueva
        alarmManager.cancel(logsPendingIntent);

        // Programar las alarmas con el intervalo configurado por el usuario
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + interval,
                interval,
                logsPendingIntent
        );
    }

    // Método para convertir la periodicidad textual a milisegundos
    private long getIntervalFromPreferences() {
        String periodicidad = notificationPreferences.getPeriodicidadReportes();

        if (periodicidad == null || periodicidad.isEmpty()) {
            return DEFAULT_REPORTS_CHECK_INTERVAL; // Valor por defecto
        }

        switch (periodicidad) {
            case "Cada 15 minutos":
                return TimeUnit.MINUTES.toMillis(15);
            case "Cada 2 horas":
                return TimeUnit.HOURS.toMillis(2);
            case "Cada 4 horas":
                return TimeUnit.HOURS.toMillis(4);
            case "Cada 6 horas":
                return TimeUnit.HOURS.toMillis(6);
            default:
                return DEFAULT_REPORTS_CHECK_INTERVAL; // 15 minutos por defecto
        }
    }

    public void cleanup() {
        stopPeriodicChecks();
        try {
            context.unregisterReceiver(reportsReceiver);
            context.unregisterReceiver(logsReceiver);
        } catch (IllegalArgumentException e) {
            // Los receivers ya están desregistrados
        }
    }

    public void stopPeriodicChecks() {
        Intent reportsIntent = new Intent(context, ReportsCheckReceiver.class);
        reportsIntent.setAction(ACTION_CHECK_REPORTS);
        PendingIntent reportsPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                reportsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent logsIntent = new Intent(context, LogsCheckReceiver.class);
        logsIntent.setAction(ACTION_CHECK_LOGS);
        PendingIntent logsPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                logsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(reportsPendingIntent);
        alarmManager.cancel(logsPendingIntent);
    }

    private BroadcastReceiver reportsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_CHECK_REPORTS.equals(intent.getAction())) {
                // Verificar si todavía está habilitado antes de mostrar la notificación
                if (notificationPreferences.isReportesEnabled()) {
                    notificationHelper.showReportesNotification();
                } else {
                    Log.d(TAG, "Notificaciones de reportes deshabilitadas, no se muestra notificación");
                }
            }
        }
    };

    private BroadcastReceiver logsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_CHECK_LOGS.equals(intent.getAction())) {
                // Verificar si todavía está habilitado antes de mostrar la notificación
                if (notificationPreferences.isLogsEnabled()) {
                    int umbral = notificationPreferences.getUmbralLogs();
                    if (umbral > 0) {
                        notificationHelper.showLogsNotification(umbral);
                    }
                } else {
                    Log.d(TAG, "Notificaciones de logs deshabilitadas, no se muestra notificación");
                }
            }
        }
    };
}
