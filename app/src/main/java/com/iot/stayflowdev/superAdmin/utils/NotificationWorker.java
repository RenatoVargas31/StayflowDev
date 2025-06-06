package com.iot.stayflowdev.superAdmin.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.superAdmin.GestionActivity;

public class NotificationWorker extends Worker {
    private static final String CHANNEL_ID = "superadmin_notifications";
    private static final int NOTIFICATION_ID = 1;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String type = getInputData().getString("type");
        if (type == null) {
            return Result.failure();
        }

        Context context = getApplicationContext();
        createNotificationChannel(context);

        if ("reportes".equals(type)) {
            showReportesNotification(context);
        } else if ("logs".equals(type)) {
            int umbral = getInputData().getInt("umbral", 0);
            showLogsNotification(context, umbral);
        }

        return Result.success();
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
        notificationManager.notify(NOTIFICATION_ID, builder.build());
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
            .setLights(0xFF0000FF, 3000, 3000);

        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID + 1, builder.build());
    }
} 