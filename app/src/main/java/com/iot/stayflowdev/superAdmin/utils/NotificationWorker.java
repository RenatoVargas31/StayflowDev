package com.iot.stayflowdev.superAdmin.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.superAdmin.GestionActivity;

import java.util.concurrent.TimeUnit;

public class NotificationWorker extends Worker {
    private static final String TAG = "NotificationWorker";
    private static final String CHANNEL_ID = "superadmin_notifications";
    private static final int NOTIFICATION_REPORTES_ID = 1001;
    private static final int NOTIFICATION_LOGS_ID = 1002;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String type = getInputData().getString("type");
        if (type == null) {
            Log.e(TAG, "Tipo de notificación no especificado");
            return Result.failure();
        }

        Log.d(TAG, "Ejecutando worker de tipo: " + type);

        Context context = getApplicationContext();
        createNotificationChannel(context);

        // Extraer valores de configuración
        boolean scheduleNext = getInputData().getBoolean("scheduleNext", false);
        long configuredInterval = getInputData().getLong("intervalConfigured", TimeUnit.MINUTES.toMillis(15));
        Log.d(TAG, "Configuración - Programar siguiente: " + scheduleNext + ", Intervalo: " + (configuredInterval / 60000) + " minutos");

        if ("reportes".equals(type)) {
            showReportesNotification(context);

            // Solo reprogramar si explícitamente se solicita y no es un trabajo periódico
            if (scheduleNext) {
                Log.d(TAG, "Auto-reprogramando próxima notificación de reportes");
                scheduleNextNotification(configuredInterval);
            }
        } else if ("logs".equals(type)) {
            int umbral = getInputData().getInt("umbral", 0);
            Log.d(TAG, "Mostrando notificación de logs con umbral: " + umbral);
            showLogsNotification(context, umbral);
        }

        return Result.success();
    }

    private void scheduleNextNotification(long delayMillis) {
        // Asegurarnos de que el intervalo no sea menor que 15 minutos para evitar problemas con WorkManager
        // excepto si explícitamente se solicita un intervalo menor y es un trabajo OneTime
        long adjustedDelay = Math.max(delayMillis, TimeUnit.MINUTES.toMillis(15));

        // Log para debug - mostrar intervalos originales y ajustados
        Log.d(TAG, "Intervalo original solicitado: " + (delayMillis / 60000) + " minutos");
        Log.d(TAG, "Intervalo ajustado aplicado: " + (adjustedDelay / 60000) + " minutos");

        // Programar la próxima notificación con un retraso
        OneTimeWorkRequest nextWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
            .setInitialDelay(adjustedDelay, TimeUnit.MILLISECONDS)
            .addTag("reportes_notification") // Añadir etiqueta para facilitar la cancelación
            .setInputData(new Data.Builder()
                .putString("type", "reportes")
                .putBoolean("scheduleNext", true)
                .putLong("nextInterval", delayMillis)  // Mantener el intervalo original, no el ajustado
                .build())
            .build();

        WorkManager.getInstance(getApplicationContext())
            .enqueue(nextWork);

        Log.d(TAG, "Próxima notificación programada en " + (adjustedDelay / 60000) + " minutos, intervalo configurado: " + (delayMillis / 60000) + " minutos");
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Notificaciones SuperAdmin",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para notificaciones del SuperAdmin");
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showReportesNotification(Context context) {
        Intent intent = new Intent(context, GestionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Reportes Pendientes")
            .setContentText("Tienes reportes de hoteles pendientes por revisar")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setVibrate(new long[]{0, 500, 200, 500})
            .setLights(0xFF0000FF, 3000, 3000);

        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_REPORTES_ID, builder.build());

        Log.d(TAG, "Notificación de reportes mostrada");
    }

    private void showLogsNotification(Context context, int umbral) {
        Intent intent = new Intent(context, GestionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Logs por Revisar")
            .setContentText("Hay " + umbral + " logs pendientes por revisar")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setVibrate(new long[]{0, 500, 200, 500})
            .setLights(0xFF00FF00, 3000, 3000);

        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_LOGS_ID, builder.build());

        Log.d(TAG, "Notificación de logs mostrada (umbral: " + umbral + ")");
    }
}
