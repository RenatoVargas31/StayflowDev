package com.iot.stayflowdev.superAdmin.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.core.content.ContextCompat;

public class PeriodicNotificationManager {
    private static final String ACTION_CHECK_REPORTS = "com.iot.stayflowdev.utils.ReportsCheckReceiver";
    private static final String ACTION_CHECK_LOGS = "com.iot.stayflowdev.utils.LogsCheckReceiver";
    private static final long REPORTS_CHECK_INTERVAL = 10 * 60 * 1000; // 10 minutos
    private static final long LOGS_CHECK_INTERVAL = 5 * 60 * 1000; // 5 minutos

    private final Context context;
    private final NotificationHelper notificationHelper;
    private final LocalStorageManager localStorageManager;
    private final AlarmManager alarmManager;

    public PeriodicNotificationManager(Context context) {
        this.context = context;
        this.notificationHelper = new NotificationHelper(context);
        this.localStorageManager = new LocalStorageManager(context);
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // Registrar los receivers
        ContextCompat.registerReceiver(context, reportsReceiver, new IntentFilter(ACTION_CHECK_REPORTS), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(context, logsReceiver, new IntentFilter(ACTION_CHECK_LOGS), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    public void startPeriodicChecks() {
        // Configurar el check de reportes
        Intent reportsIntent = new Intent(context, ReportsCheckReceiver.class);
        reportsIntent.setAction(ACTION_CHECK_REPORTS);
        PendingIntent reportsPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                reportsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Configurar el check de logs
        Intent logsIntent = new Intent(context, LogsCheckReceiver.class);
        logsIntent.setAction(ACTION_CHECK_LOGS);
        PendingIntent logsPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                logsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Programar las alarmas
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + REPORTS_CHECK_INTERVAL,
                REPORTS_CHECK_INTERVAL,
                reportsPendingIntent
        );

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + LOGS_CHECK_INTERVAL,
                LOGS_CHECK_INTERVAL,
                logsPendingIntent
        );
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

    private final BroadcastReceiver reportsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_CHECK_REPORTS.equals(intent.getAction())) {
                long lastCheck = localStorageManager.getLastReportCheck();
                long currentTime = System.currentTimeMillis();

                // Si han pasado más de 10 minutos desde la última revisión
                if (currentTime - lastCheck >= REPORTS_CHECK_INTERVAL) {
                    notificationHelper.showSystemNotification(
                            "Revisión de Reportes Pendiente",
                            "Han pasado más de 10 minutos desde la última revisión de reportes. Por favor, revise los reportes pendientes."
                    );
                    localStorageManager.saveLastReportCheck(currentTime);
                }
            }
        }
    };

    private final BroadcastReceiver logsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_CHECK_LOGS.equals(intent.getAction())) {
                // Implementar la lógica de verificación de logs
                notificationHelper.showSystemNotification(
                        "Verificación de Logs",
                        "Se ha realizado una verificación de logs del sistema."
                );
            }
        }
    };
}
