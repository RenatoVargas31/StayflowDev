package com.iot.stayflowdev.superAdmin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.iot.stayflowdev.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iot.stayflowdev.superAdmin.utils.PeriodicNotificationManager;
import com.iot.stayflowdev.superAdmin.utils.PermissionHelper;
import com.iot.stayflowdev.superAdmin.utils.NotificationHelper;

public class SuperAdminActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private MaterialToolbar toolbar;
    private PeriodicNotificationManager periodicNotificationManager;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superadmin_activity);

        toolbar = findViewById(R.id.topAppBar);
        toolbar.setTitle("Inicio");

        bottomNav = findViewById(R.id.bottom_navigation);
        notificationHelper = new NotificationHelper(this);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_inicio) {
                startActivity(new Intent(this, InicioActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_gestion) {
                startActivity(new Intent(this, GestionActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_reportes) {
                startActivity(new Intent(this, ReportesActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                startActivity(new Intent(this, PerfilActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });

        // Selecciona "Inicio" como el ítem actual
        bottomNav.setSelectedItemId(R.id.nav_inicio);

        // Verificar y solicitar permisos de notificación
        checkAndRequestNotificationPermission();
    }

    private void checkAndRequestNotificationPermission() {
        if (!PermissionHelper.hasNotificationPermission(this)) {
            if (PermissionHelper.shouldShowNotificationPermissionRationale(this)) {
                showNotificationPermissionRationaleDialog();
            } else {
                PermissionHelper.requestNotificationPermission(this);
            }
        } else {
            startNotificationManager();
            // Mostrar notificación de bienvenida
            showWelcomeNotification();
        }
    }

    private void showWelcomeNotification() {
        notificationHelper.showSystemNotification(
                "Bienvenido a StayFlow",
                "Has iniciado sesión como Super Administrador. Recibirás notificaciones importantes sobre el sistema."
        );
    }

    private void showNotificationPermissionRationaleDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permiso de Notificaciones")
                .setMessage("Las notificaciones son importantes para mantenerte informado sobre eventos críticos y actualizaciones del sistema. Por favor, permite las notificaciones para una mejor experiencia.")
                .setPositiveButton("Permitir", (dialog, which) -> {
                    PermissionHelper.requestNotificationPermission(this);
                })
                .setNegativeButton("No permitir", (dialog, which) -> {
                    Toast.makeText(this, "Las notificaciones están desactivadas. Puedes activarlas más tarde en la configuración.", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHelper.NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startNotificationManager();
                // Mostrar notificación de bienvenida después de conceder el permiso
                showWelcomeNotification();
            } else {
                Toast.makeText(this, "Las notificaciones están desactivadas. Puedes activarlas más tarde en la configuración.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startNotificationManager() {
        // Iniciar las notificaciones periódicas
        periodicNotificationManager = new PeriodicNotificationManager(this);
        periodicNotificationManager.startPeriodicChecks();
    }

    @Override
    public void onBackPressed() {
        // Si quieres evitar volver atrás y cerrar la app directamente:
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (periodicNotificationManager != null) {
            periodicNotificationManager.cleanup();
        }
    }
}

