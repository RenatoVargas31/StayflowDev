package com.iot.stayflowdev.superAdmin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.superAdmin.utils.NotificationPreferences;
import com.iot.stayflowdev.superAdmin.utils.NotificationWorker;

import java.util.concurrent.TimeUnit;

public class PerfilSuperAdminActivity extends BaseSuperAdminActivity {
    private static final String TAG = "PerfilSuperAdmin";

    // Switches para notificaciones
    private SwitchMaterial switchReportes;
    private SwitchMaterial switchLogs;

    // Campo para umbral de logs
    private TextInputLayout layoutUmbralLogs;

    // Botón para guardar
    private MaterialButton buttonGuardar;

    // Servicios
    private NotificationPreferences preferences;
    private WorkManager workManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Iniciando configuración de notificaciones");

        // Inicializar servicios
        preferences = new NotificationPreferences(this);
        workManager = WorkManager.getInstance(this);

        // Inicializar vistas
        initializeViews();
        loadConfiguration();
        setupListeners();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_superadmin_perfil_simplified;
    }

    @Override
    protected int getBottomNavigationSelectedItem() {
        return R.id.nav_perfil;
    }

    @Override
    protected String getToolbarTitle() {
        return "Configuración de Notificaciones";
    }

    private void initializeViews() {
        switchReportes = findViewById(R.id.switchReportes);
        switchLogs = findViewById(R.id.switchLogs);
        layoutUmbralLogs = findViewById(R.id.layoutUmbralLogs);
        buttonGuardar = findViewById(R.id.buttonGuardar);

        // Estado inicial deshabilitado para el umbral
        layoutUmbralLogs.setEnabled(false);
    }

    private void loadConfiguration() {
        // Cargar configuración guardada
        switchReportes.setChecked(preferences.isReportesEnabled());
        switchLogs.setChecked(preferences.isLogsEnabled());

        int umbral = preferences.getUmbralLogs();
        if (umbral > 0) {
            layoutUmbralLogs.getEditText().setText(String.valueOf(umbral));
        }

        // Actualizar estado de campos
        updateFieldsState();
    }

    private void setupListeners() {
        // Switch de reportes
        switchReportes.setOnCheckedChangeListener((v, isChecked) -> {
            updateFieldsState();
        });

        // Switch de logs
        switchLogs.setOnCheckedChangeListener((v, isChecked) -> {
            updateFieldsState();
        });

        // Botón guardar
        buttonGuardar.setOnClickListener(v -> saveConfiguration());
    }

    private void updateFieldsState() {
        layoutUmbralLogs.setEnabled(switchLogs.isChecked());
        if (layoutUmbralLogs.getEditText() != null) {
            layoutUmbralLogs.getEditText().setEnabled(switchLogs.isChecked());
        }
    }

    private void saveConfiguration() {
        // Obtener valores
        boolean reportesEnabled = switchReportes.isChecked();
        boolean logsEnabled = switchLogs.isChecked();

        String umbralStr = layoutUmbralLogs.getEditText() != null ?
            layoutUmbralLogs.getEditText().getText().toString() : "";

        // Validar umbral de logs si está habilitado
        if (logsEnabled && umbralStr.isEmpty()) {
            Toast.makeText(this, "Ingrese un umbral para las notificaciones de logs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar configuración
        preferences.setReportesEnabled(reportesEnabled);
        preferences.setLogsEnabled(logsEnabled);

        if (!umbralStr.isEmpty()) {
            try {
                int umbral = Integer.parseInt(umbralStr);
                preferences.setUmbralLogs(umbral);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "El umbral debe ser un número válido", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Configurar notificaciones
        configureNotifications(reportesEnabled, logsEnabled);

        Toast.makeText(this, "Configuración guardada correctamente", Toast.LENGTH_SHORT).show();
    }

    private void configureNotifications(boolean reportesEnabled, boolean logsEnabled) {
        // Cancelar trabajos existentes
        workManager.cancelUniqueWork("notification_reportes");
        workManager.cancelUniqueWork("notification_logs");

        // Configurar notificaciones de reportes (cada 6 horas)
        if (reportesEnabled) {
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

        // Configurar notificaciones de logs (cada 2 horas)
        if (logsEnabled) {
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
    }
}

