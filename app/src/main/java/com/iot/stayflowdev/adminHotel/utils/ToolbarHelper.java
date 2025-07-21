package com.iot.stayflowdev.adminHotel.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.NotificacionesAdminActivity;
import com.iot.stayflowdev.adminHotel.repository.AdminHotelViewModel;

public class ToolbarHelper {

    public static void configurarToolbarNotificaciones(Activity activity, String titulo) {
        // Configurar título
        TextView toolbarTitle = activity.findViewById(R.id.toolbar_title);
        if (toolbarTitle != null && titulo != null) {
            toolbarTitle.setText(titulo);
        }

        // Configurar icono de notificaciones
        ImageView notificationIcon = activity.findViewById(R.id.notification_icon);
        TextView badgeText = activity.findViewById(R.id.badge_text);

        if (notificationIcon != null) {
            // Click listener para ir a notificaciones
            notificationIcon.setOnClickListener(v -> {
                Intent intent = new Intent(activity, NotificacionesAdminActivity.class);
                activity.startActivity(intent);
            });

            // Configurar badge si la activity implementa ViewModelStoreOwner y LifecycleOwner
            if (activity instanceof ViewModelStoreOwner && activity instanceof LifecycleOwner) {
                AdminHotelViewModel viewModel = new ViewModelProvider((ViewModelStoreOwner) activity)
                        .get(AdminHotelViewModel.class);

                viewModel.getContadorNotificaciones().observe((LifecycleOwner) activity, contador -> {
                    if (badgeText != null) {
                        if (contador != null && contador > 0) {
                            badgeText.setVisibility(View.VISIBLE);
                            badgeText.setText(contador > 99 ? "99+" : String.valueOf(contador));
                        } else {
                            badgeText.setVisibility(View.GONE);
                        }
                    }
                });

                // Cargar notificaciones
                viewModel.cargarNotificacionesCheckoutTiempoReal();
            }
        }
    }

    public static void ocultarBadgeNotificaciones(Activity activity) {
        TextView badgeText = activity.findViewById(R.id.badge_text);
        if (badgeText != null) {
            badgeText.setVisibility(View.GONE);
        }
    }
}