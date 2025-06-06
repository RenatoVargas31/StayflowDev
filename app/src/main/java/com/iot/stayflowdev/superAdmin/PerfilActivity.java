package com.iot.stayflowdev.superAdmin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PerfilActivity extends BaseSuperAdminActivity {
    private static final String TAG = "PerfilActivity";
    private static final String WORK_REPORTES = "work_reportes";
    private static final String WORK_LOGS = "work_logs";

    // UI Components
    private SwitchMaterial switchReportes;
    private SwitchMaterial switchLogs;
    private TextInputLayout layoutPeriodicidadReportes;
    private TextInputLayout layoutUmbralLogs;
    private AutoCompleteTextView dropdownPeriodicidadReportes;
    private MaterialButton buttonGuardar;

    // Utilities
    private NotificationPreferences notificationPreferences;
    private WorkManager workManager;
    private List<String> periodicidades;
    private ArrayAdapter<String> dropdownAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupViews();
        loadSavedPreferences();
    }

    private void initializeComponents() {
        notificationPreferences = new NotificationPreferences(this);
        workManager = WorkManager.getInstance(this);

        periodicidades = Arrays.asList(
            "Cada minuto",
            "Diario",
            "Semanal",
            "Mensual"
        );
    }

    private void setupViews() {
        // Inicializar vistas
        switchReportes = findViewById(R.id.switchReportes);
        switchLogs = findViewById(R.id.switchLogs);
        layoutPeriodicidadReportes = findViewById(R.id.layoutPeriodicidadReportes);
        layoutUmbralLogs = findViewById(R.id.layoutUmbralLogs);
        dropdownPeriodicidadReportes = findViewById(R.id.dropdownPeriodicidadReportes);
        buttonGuardar = findViewById(R.id.buttonGuardar);

        // Configurar estados iniciales
        layoutPeriodicidadReportes.setEnabled(false);
        layoutUmbralLogs.setEnabled(false);

        setupDropdown();
        setupListeners();
    }

    private void setupDropdown() {
        dropdownAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            periodicidades
        );

        dropdownPeriodicidadReportes.setAdapter(dropdownAdapter);
        dropdownPeriodicidadReportes.setThreshold(0);

        // Configurar comportamiento del dropdown
        layoutPeriodicidadReportes.setEndIconOnClickListener(view -> {
            if (dropdownPeriodicidadReportes.isEnabled()) {
                dropdownPeriodicidadReportes.showDropDown();
            }
        });

        dropdownPeriodicidadReportes.setOnClickListener(view -> {
            if (dropdownPeriodicidadReportes.isEnabled()) {
                dropdownPeriodicidadReportes.showDropDown();
            }
        });

        dropdownPeriodicidadReportes.setOnItemClickListener((parent, view, position, id) -> {
            String selected = periodicidades.get(position);
            Log.d(TAG, "Periodicidad seleccionada: " + selected);
            Toast.makeText(this, "Periodicidad configurada: " + selected, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        switchReportes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutPeriodicidadReportes.setEnabled(isChecked);
            dropdownPeriodicidadReportes.setEnabled(isChecked);
            if (!isChecked) {
                dropdownPeriodicidadReportes.setText("", false);
            }
        });

        switchLogs.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutUmbralLogs.setEnabled(isChecked);
            if (layoutUmbralLogs.getEditText() != null) {
                layoutUmbralLogs.getEditText().setEnabled(isChecked);
            }
            if (!isChecked) {
                if (layoutUmbralLogs.getEditText() != null) {
                    layoutUmbralLogs.getEditText().setText("");
                }
            }
        });

        buttonGuardar.setOnClickListener(v -> savePreferences());
    }

    private void loadSavedPreferences() {
        boolean reportesEnabled = notificationPreferences.isReportesEnabled();
        boolean logsEnabled = notificationPreferences.isLogsEnabled();
        String periodicidad = notificationPreferences.getPeriodicidadReportes();
        int umbralLogs = notificationPreferences.getUmbralLogs();

        switchReportes.setChecked(reportesEnabled);
        switchLogs.setChecked(logsEnabled);

        layoutPeriodicidadReportes.setEnabled(reportesEnabled);
        dropdownPeriodicidadReportes.setEnabled(reportesEnabled);
        layoutUmbralLogs.setEnabled(logsEnabled);

        if (periodicidad != null && !periodicidad.isEmpty()) {
            dropdownPeriodicidadReportes.setText(periodicidad, false);
        }

        if (umbralLogs > 0 && layoutUmbralLogs.getEditText() != null) {
            layoutUmbralLogs.getEditText().setText(String.valueOf(umbralLogs));
        }
    }

    private void savePreferences() {
        boolean reportesEnabled = switchReportes.isChecked();
        boolean logsEnabled = switchLogs.isChecked();
        String periodicidad = dropdownPeriodicidadReportes.getText().toString();
        String umbralLogsStr = layoutUmbralLogs.getEditText() != null ?
            layoutUmbralLogs.getEditText().getText().toString() : "";

        // Validaciones
        if (reportesEnabled && periodicidad.isEmpty()) {
            Toast.makeText(this, "Por favor seleccione una periodicidad", Toast.LENGTH_SHORT).show();
            return;
        }

        if (logsEnabled && umbralLogsStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese un umbral de logs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar preferencias
        notificationPreferences.setReportesEnabled(reportesEnabled);
        notificationPreferences.setLogsEnabled(logsEnabled);
        notificationPreferences.setPeriodicidadReportes(periodicidad);

        if (!umbralLogsStr.isEmpty()) {
            notificationPreferences.setUmbralLogs(Integer.parseInt(umbralLogsStr));
        }

        // Programar o cancelar notificaciones
        if (reportesEnabled) {
            scheduleReportesNotification(periodicidad);
        } else {
            workManager.cancelUniqueWork(WORK_REPORTES);
        }

        if (logsEnabled && !umbralLogsStr.isEmpty()) {
            scheduleLogsNotification(Integer.parseInt(umbralLogsStr));
        } else {
            workManager.cancelUniqueWork(WORK_LOGS);
        }

        Toast.makeText(this, "Preferencias guardadas correctamente", Toast.LENGTH_SHORT).show();
    }

    private void scheduleReportesNotification(String periodicidad) {
        long interval = getIntervalFromPeriodicidad(periodicidad);
        if (interval <= 0) return;

        PeriodicWorkRequest reportesWork = new PeriodicWorkRequest.Builder(
            NotificationWorker.class,
            interval,
            TimeUnit.MILLISECONDS)
            .setInputData(new androidx.work.Data.Builder()
                .putString("type", "reportes")
                .build())
            .build();

        workManager.enqueueUniquePeriodicWork(
            WORK_REPORTES,
            ExistingPeriodicWorkPolicy.REPLACE,
            reportesWork
        );
    }

    private void scheduleLogsNotification(int umbral) {
        PeriodicWorkRequest logsWork = new PeriodicWorkRequest.Builder(
            NotificationWorker.class,
            15,
            TimeUnit.MINUTES)
            .setInputData(new androidx.work.Data.Builder()
                .putString("type", "logs")
                .putInt("umbral", umbral)
                .build())
            .build();

        workManager.enqueueUniquePeriodicWork(
            WORK_LOGS,
            ExistingPeriodicWorkPolicy.REPLACE,
            logsWork
        );
    }

    private long getIntervalFromPeriodicidad(String periodicidad) {
        switch (periodicidad) {
            case "Cada minuto":
                return TimeUnit.MINUTES.toMillis(1);
            case "Diario":
                return TimeUnit.DAYS.toMillis(1);
            case "Semanal":
                return TimeUnit.DAYS.toMillis(7);
            case "Mensual":
                return TimeUnit.DAYS.toMillis(30);
            default:
                return 0;
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.superadmin_perfil_superadmin;
    }

    @Override
    protected int getBottomNavigationSelectedItem() {
        return R.id.nav_perfil;
    }

    @Override
    protected String getToolbarTitle() {
        return "Perfil";
    }
}