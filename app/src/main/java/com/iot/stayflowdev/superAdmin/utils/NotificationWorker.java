package com.iot.stayflowdev.superAdmin.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {
    private static final String TAG = "NotificationWorker";

    private NotificationHelper notificationHelper;
    private LogsManager logsManager;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.notificationHelper = new NotificationHelper(context);
        this.logsManager = LogsManager.getInstance(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            String type = getInputData().getString("type");
            Log.d(TAG, "Procesando notificación tipo: " + type);

            if ("reportes".equals(type)) {
                return processReportesNotification();
            } else if ("logs".equals(type)) {
                return processLogsNotification();
            }

            return Result.failure();
        } catch (Exception e) {
            Log.e(TAG, "Error procesando notificación", e);
            return Result.failure();
        }
    }

    private Result processReportesNotification() {
        try {
            // Enviar recordatorio simple de reportes
            notificationHelper.showReportesNotification();
            Log.d(TAG, "Notificación de reportes enviada");
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error enviando notificación de reportes", e);
            return Result.failure();
        }
    }

    private Result processLogsNotification() {
        try {
            int umbral = getInputData().getInt("umbral", 100);
            int currentCount = logsManager.getLogsCount();

            Log.d(TAG, "Verificando logs: " + currentCount + "/" + umbral);

            // Solo notificar si se alcanzó el umbral
            if (currentCount >= umbral) {
                notificationHelper.showLogsNotification(umbral, currentCount);
                Log.d(TAG, "Notificación de logs enviada");
            } else {
                Log.d(TAG, "Umbral no alcanzado, no se envía notificación");
            }

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error procesando notificación de logs", e);
            return Result.failure();
        }
    }
}
