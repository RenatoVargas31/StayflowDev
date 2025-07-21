package com.iot.stayflowdev.superAdmin.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Sistema reiniciado, restaurando notificaciones");

            NotificationPreferences preferences = new NotificationPreferences(context);

            // Solo reiniciar notificaciones si están habilitadas
            if (preferences.isReportesEnabled() || preferences.isLogsEnabled()) {
                restoreNotifications(context, preferences);
            }
        }
    }

    private void restoreNotifications(Context context, NotificationPreferences preferences) {
        WorkManager workManager = WorkManager.getInstance(context);

        // Restaurar notificaciones de reportes
        if (preferences.isReportesEnabled()) {
            PeriodicWorkRequest reportesWork = new PeriodicWorkRequest.Builder(
                    NotificationWorker.class, 6, TimeUnit.HOURS)
                    .setInputData(new androidx.work.Data.Builder()
                            .putString("type", "reportes")
                            .build())
                    .build();

            workManager.enqueueUniquePeriodicWork(
                    "notification_reportes",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    reportesWork
            );
        }

        // Restaurar notificaciones de logs
        if (preferences.isLogsEnabled()) {
            PeriodicWorkRequest logsWork = new PeriodicWorkRequest.Builder(
                    NotificationWorker.class, 2, TimeUnit.HOURS)
                    .setInputData(new androidx.work.Data.Builder()
                            .putString("type", "logs")
                            .putInt("umbral", preferences.getUmbralLogs())
                            .build())
                    .build();

            workManager.enqueueUniquePeriodicWork(
                    "notification_logs",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    logsWork
            );
        }

        Log.d(TAG, "Notificaciones restauradas exitosamente");
    }
}
