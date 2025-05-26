package com.codebnb.stayflow.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

public class PeriodicNotificationManager {
    private static final String ACTION_CHECK_REPORTS = "com.codebnb.stayflow.CHECK_REPORTS";
    private static final String ACTION_CHECK_LOGS = "com.codebnb.stayflow.CHECK_LOGS";
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
        context.registerReceiver(reportsReceiver, new IntentFilter(ACTION_CHECK_REPORTS));
        context.registerReceiver(logsReceiver, new IntentFilter(ACTION_CHECK_LOGS));
    }

    public void startPeriodicChecks() {
        // Verificar si las notificaciones están habilitadas
        if (!localStorageManager.getSettings().isNotificationsEnabled()) {
            return;
        }

        // Programar verificación de reportes
        Intent reportsIntent = new Intent(context, ReportsCheckReceiver.class);
        reportsIntent.setAction(ACTION_CHECK_REPORTS);
        PendingIntent reportsPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                reportsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Enviar notificación inmediatamente
        context.sendBroadcast(reportsIntent);

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + REPORTS_CHECK_INTERVAL,
                REPORTS_CHECK_INTERVAL,
                reportsPendingIntent
        );

        // Programar verificación de logs
        Intent logsIntent = new Intent(context, LogsCheckReceiver.class);
        logsIntent.setAction(ACTION_CHECK_LOGS);
        PendingIntent logsPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                logsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Enviar notificación inmediatamente
        context.sendBroadcast(logsIntent);

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + LOGS_CHECK_INTERVAL,
                LOGS_CHECK_INTERVAL,
                logsPendingIntent
        );
    }

    public void stopPeriodicChecks() {
        Intent reportsIntent = new Intent(context, ReportsCheckReceiver.class);
        PendingIntent reportsPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                reportsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent logsIntent = new Intent(context, LogsCheckReceiver.class);
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
                // Aquí deberías implementar la lógica para verificar logs críticos
                // Este es un ejemplo de cómo podría ser
                checkCriticalLogs();
            }
        }
    };

    private void checkCriticalLogs() {
        // Ejemplo de log crítico
        String criticalLog = "Error crítico en el sistema de pagos: Fallo en la transacción #12345";
        if (criticalLog != null) {
            notificationHelper.showSystemNotification(
                    "Log Crítico Detectado",
                    criticalLog
            );
        }
    }

    public void cleanup() {
        try {
            context.unregisterReceiver(reportsReceiver);
            context.unregisterReceiver(logsReceiver);
        } catch (IllegalArgumentException e) {
            // Los receivers ya están desregistrados
        }
    }
} 