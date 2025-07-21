package com.iot.stayflowdev.superAdmin.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.superAdmin.InicioActivity;

public class NotificationHelper {
    private static final String CHANNEL_ID = "StayFlowChannel";
    private static final String CHANNEL_NAME = "StayFlow Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for StayFlow app";
    private static final int NOTIFICATION_ID = 1;

    private final Context context;
    private final NotificationManagerCompat notificationManager;
    private final LocalStorageManager localStorageManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        this.localStorageManager = new LocalStorageManager(context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showNotification(String title, String message, String type) {
        // Verificar si las notificaciones están habilitadas en la configuración
        if (!localStorageManager.getSettings().isNotificationsEnabled()) {
            return;
        }

        // Verificar si tenemos permiso para mostrar notificaciones
        if (!PermissionHelper.hasNotificationPermission(context)) {
            return;
        }

        // Crear intent para abrir la app cuando se toque la notificación
        Intent intent = new Intent(context, InicioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            // Mostrar la notificación
            notificationManager.notify(NOTIFICATION_ID, builder.build());

            // Guardar la notificación en el almacenamiento local
            LocalStorageManager.NotificationItem notification = new LocalStorageManager.NotificationItem();
            notification.setId(String.valueOf(System.currentTimeMillis()));
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setTimestamp(System.currentTimeMillis());
            notification.setRead(false);
            notification.setType(type);
            localStorageManager.addNotification(notification);
        } catch (SecurityException e) {
            // Si no tenemos permiso para mostrar notificaciones, no hacemos nada
        }
    }

    // Método para mostrar notificación de nueva reserva
    public void showNewReservationNotification(String hotelName, String date) {
        String title = "Nueva Reserva";
        String message = "Se ha realizado una nueva reserva en " + hotelName + " para el " + date;
        showNotification(title, message, "reservation");
    }

    // Método para mostrar notificación de actualización de estado
    public void showStatusUpdateNotification(String hotelName, String status) {
        String title = "Actualización de Estado";
        String message = "El estado de " + hotelName + " ha sido actualizado a: " + status;
        showNotification(title, message, "status");
    }

    // Método para mostrar notificación de sistema
    public void showSystemNotification(String title, String message) {
        showNotification(title, message, "system");
    }

    // Método para mostrar notificación de error
    public void showErrorNotification(String message) {
        String title = "Error";
        showNotification(title, message, "error");
    }

    // Método para mostrar notificación de reportes pendientes
    public void showReportesNotification() {
        String title = "Reportes Pendientes";
        String message = "Tienes reportes de hoteles pendientes por revisar";
        showNotification(title, message, "reports");
    }

    // Método para mostrar notificación de logs pendientes
    public void showLogsNotification(int umbral, int currentCount) {
        String title = "Umbral de Logs Alcanzado";
        String message = "Se han registrado " + currentCount + " logs (umbral: " + umbral + "). Revisa los logs del sistema.";
        showNotification(title, message, "logs");
    }
}