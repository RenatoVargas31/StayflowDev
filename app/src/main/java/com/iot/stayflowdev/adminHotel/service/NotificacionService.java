package com.iot.stayflowdev.adminHotel.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.NotificacionesAdminActivity;
import com.iot.stayflowdev.adminHotel.model.NotificacionCheckout;

import java.text.NumberFormat;
import java.util.Locale;

public class NotificacionService {

    private static final String CHANNEL_ID = "checkout_notifications";
    private static final String CHANNEL_NAME = "Notificaciones de Checkout";
    private static final String CHANNEL_DESC = "Notificaciones para recordar checkouts pendientes";

    private Context context;
    private NotificationManagerCompat notificationManager;

    public NotificacionService(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        crearCanalNotificacion();
    }

    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public void mostrarNotificacionCheckout(NotificacionCheckout notificacion) {
        Intent intent = new Intent(context, NotificacionesAdminActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificacion.getReservaId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(notificacion.getTituloNotificacion())
                .setContentText(notificacion.getMensajeNotificacion())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(crearTextoExpandido(notificacion)))
                .setPriority(obtenerPrioridad(notificacion.getTipoNotificacion()))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        // Configurar color según tipo
        builder.setColor(context.getResources().getColor(notificacion.getColorPrioridad()));

        notificationManager.notify(
                notificacion.getReservaId().hashCode(),
                builder.build()
        );
    }

    private String crearTextoExpandido(NotificacionCheckout notificacion) {
        StringBuilder texto = new StringBuilder();
        texto.append("Huésped: ").append(notificacion.getNombreHuesped()).append("\n");

        // Mostrar información de habitaciones
        String habitacionInfo = notificacion.getCantidadHabitaciones() + " " +
                notificacion.getTipoHabitacion().toLowerCase();
        if (notificacion.getCantidadHabitaciones() > 1) {
            habitacionInfo += "s";
        }
        texto.append("Habitaciones: ").append(habitacionInfo).append("\n");
        texto.append("Monto: S/. ").append(notificacion.getCostoTotal());

        if ("CHECKOUT_VENCIDO".equals(notificacion.getTipoNotificacion())) {
            texto.append("\n⚠️ URGENTE: Checkout vencido");
        } else if ("CHECKOUT_HOY".equals(notificacion.getTipoNotificacion())) {
            if (notificacion.getEstadoReserva().equals("checkin")) {
                texto.append("\n🏨 Cliente debe hacer checkout hoy");
            } else {
                texto.append("\n📅 Reserva vence hoy");
            }
        }

        return texto.toString();
    }

    private int obtenerPrioridad(String tipoNotificacion) {
        switch (tipoNotificacion) {
            case "CHECKOUT_VENCIDO":
                return NotificationCompat.PRIORITY_MAX;
            case "CHECKOUT_HOY":
                return NotificationCompat.PRIORITY_HIGH;
            case "PENDIENTE_PAGO":
                return NotificationCompat.PRIORITY_DEFAULT;
            default:
                return NotificationCompat.PRIORITY_LOW;
        }
    }

    public void cancelarNotificacion(String reservaId) {
        notificationManager.cancel(reservaId.hashCode());
    }

    public void mostrarResumenNotificaciones(int cantidadCheckouts, int cantidadVencidos) {
        Intent intent = new Intent(context, NotificacionesAdminActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String titulo = "Checkouts pendientes";
        String mensaje = cantidadCheckouts + " checkout" + (cantidadCheckouts > 1 ? "s" : "") + " pendiente" + (cantidadCheckouts > 1 ? "s" : "");

        if (cantidadVencidos > 0) {
            mensaje += " (" + cantidadVencidos + " vencido" + (cantidadVencidos > 1 ? "s" : "") + ")";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setNumber(cantidadCheckouts)
                .setPriority(cantidadVencidos > 0 ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (cantidadVencidos > 0) {
            builder.setColor(context.getResources().getColor(android.R.color.holo_red_light));
        }

        notificationManager.notify(999, builder.build());
    }
}