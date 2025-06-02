package com.iot.stayflowdev;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.iot.stayflowdev.superAdmin.InicioActivity;
import com.iot.stayflowdev.superAdmin.GestionActivity;
import com.iot.stayflowdev.superAdmin.ReportesActivity;
import com.iot.stayflowdev.superAdmin.PerfilActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iot.stayflowdev.utils.PeriodicNotificationManager;

public class SuperAdminActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private MaterialToolbar toolbar;
    private PeriodicNotificationManager periodicNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superadmin_activity);

        toolbar = findViewById(R.id.topAppBar);
        toolbar.setTitle("Inicio");

        bottomNav = findViewById(R.id.bottom_navigation);

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

        // Selecciona "Inicio" como el ítem actual (esto es útil si navegas desde otra actividad)
        bottomNav.setSelectedItemId(R.id.nav_inicio);

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
