package com.codebnb.stayflow.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReportsCheckReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        LocalStorageManager localStorageManager = new LocalStorageManager(context);
        
        long lastCheck = localStorageManager.getLastReportCheck();
        long currentTime = System.currentTimeMillis();
        
        // Si han pasado más de 10 minutos desde la última revisión
        if (currentTime - lastCheck >= 10 * 60 * 1000) {
            notificationHelper.showSystemNotification(
                    "Revisión de Reportes Pendiente",
                    "Han pasado más de 10 minutos desde la última revisión de reportes. Por favor, revise los reportes pendientes."
            );
            localStorageManager.saveLastReportCheck(currentTime);
        }
    }
} 