package com.iot.stayflowdev.superAdmin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
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

public class PerfilSuperAdminActivity extends BaseSuperAdminActivity {
    private static final String TAG = "PerfilSuperAdmin";

    private static final String WORK_REPORTES = "work_reportes";
    private static final String WORK_LOGS = "work_logs";

    private SwitchMaterial switchReportes;
    private SwitchMaterial switchLogs;
    private TextInputLayout layoutPeriodicidadReportes;
    private TextInputLayout layoutUmbralLogs;
    private AutoCompleteTextView dropdownPeriodicidadReportes;
    private MaterialButton buttonGuardar;
    private NotificationPreferences notificationPreferences;
    private WorkManager workManager;
    private List<String> periodicidades;
    private Handler mainHandler;
    private ArrayAdapter<String> dropdownAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Iniciando actividad");

        mainHandler = new Handler(Looper.getMainLooper());

        // Inicializar la lista de periodicidades
        periodicidades = Arrays.asList(
            "Cada minuto",
            "Diario",
            "Semanal",
            "Mensual"
        );

        // Inicializar preferencias de notificaciones y WorkManager
        notificationPreferences = new NotificationPreferences(this);
        workManager = WorkManager.getInstance(this);

        // Inicializar vistas
        initializeViews();
        setupDropdowns();
        loadSavedPreferences();
        setupListeners();
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

    private void initializeViews() {
        Log.d(TAG, "initializeViews: Inicializando vistas");
        switchReportes = findViewById(R.id.switchReportes);
        switchLogs = findViewById(R.id.switchLogs);
        layoutPeriodicidadReportes = findViewById(R.id.layoutPeriodicidadReportes);
        layoutUmbralLogs = findViewById(R.id.layoutUmbralLogs);
        dropdownPeriodicidadReportes = findViewById(R.id.dropdownPeriodicidadReportes);
        buttonGuardar = findViewById(R.id.buttonGuardar);

        // Configurar estado inicial
        layoutPeriodicidadReportes.setEnabled(false);
        dropdownPeriodicidadReportes.setEnabled(false);
        layoutUmbralLogs.setEnabled(false);
        
        Log.d(TAG, "initializeViews: Vistas inicializadas");
    }

    private void setupDropdowns() {
        Log.d(TAG, "setupDropdowns: Configurando dropdown");
        
        // Crear y configurar el adapter
        dropdownAdapter = new ArrayAdapter<>(
            this,
            R.layout.dropdown_item,
            periodicidades
        );
        
        // Establecer el adapter en el AutoCompleteTextView
        dropdownPeriodicidadReportes.setAdapter(dropdownAdapter);
        
        // Verificar que el adapter tenga los datos correctos
        Log.d(TAG, "setupDropdowns: Adapter configurado con " + dropdownAdapter.getCount() + " opciones");
        for (int i = 0; i < dropdownAdapter.getCount(); i++) {
            Log.d(TAG, "setupDropdowns: Item " + i + ": " + dropdownAdapter.getItem(i));
        }

        // Configurar el TextInputLayout
        layoutPeriodicidadReportes.setEndIconOnClickListener(v -> {
            Log.d(TAG, "EndIcon clicked");
            showDropdown();
        });

        // Configurar el AutoCompleteTextView
        dropdownPeriodicidadReportes.setOnClickListener(v -> {
            Log.d(TAG, "AutoCompleteTextView clicked");
            showDropdown();
        });

        dropdownPeriodicidadReportes.setOnItemClickListener((parent, view, position, id) -> {
            String selected = periodicidades.get(position);
            Log.d(TAG, "Item seleccionado: " + selected);
            dropdownPeriodicidadReportes.setText(selected, false);
            Toast.makeText(this, "Notificaciones configuradas para " + selected.toLowerCase(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showDropdown() {
        if (switchReportes.isChecked()) {
            Log.d(TAG, "Intentando mostrar dropdown");
            mainHandler.post(() -> {
                try {
                    if (dropdownAdapter.getCount() > 0) {
                        dropdownPeriodicidadReportes.showDropDown();
                        Log.d(TAG, "Dropdown mostrado exitosamente");
                    } else {
                        Log.e(TAG, "El adapter no tiene items");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al mostrar dropdown", e);
                }
            });
        }
    }

    private void loadSavedPreferences() {
        Log.d(TAG, "loadSavedPreferences: Cargando preferencias");
        boolean reportesEnabled = notificationPreferences.isReportesEnabled();
        boolean logsEnabled = notificationPreferences.isLogsEnabled();
        String periodicidad = notificationPreferences.getPeriodicidadReportes();
        int umbralLogs = notificationPreferences.getUmbralLogs();

        Log.d(TAG, "loadSavedPreferences: reportesEnabled=" + reportesEnabled + 
                   ", periodicidad=" + periodicidad);

        switchReportes.setChecked(reportesEnabled);
        switchLogs.setChecked(logsEnabled);
        
        updateFieldsState(reportesEnabled, logsEnabled);
        
        if (periodicidad != null && !periodicidad.isEmpty()) {
            dropdownPeriodicidadReportes.setText(periodicidad, false);
        }
        
        if (umbralLogs > 0) {
            layoutUmbralLogs.getEditText().setText(String.valueOf(umbralLogs));
        }
    }

    private void updateFieldsState(boolean reportesEnabled, boolean logsEnabled) {
        Log.d(TAG, "updateFieldsState: reportesEnabled=" + reportesEnabled + 
                   ", logsEnabled=" + logsEnabled);
        
        // Actualizar estado de los campos de reportes
        layoutPeriodicidadReportes.setEnabled(reportesEnabled);
        dropdownPeriodicidadReportes.setEnabled(reportesEnabled);
        
        // Actualizar estado de los campos de logs
        layoutUmbralLogs.setEnabled(logsEnabled);
        if (layoutUmbralLogs.getEditText() != null) {
            layoutUmbralLogs.getEditText().setEnabled(logsEnabled);
        }

        // Mostrar dropdown si está habilitado
        if (reportesEnabled) {
            mainHandler.postDelayed(this::showDropdown, 300); // Pequeño delay para asegurar que la UI esté lista
        }
    }

    private void setupListeners() {
        Log.d(TAG, "setupListeners: Configurando listeners");
        
        // Listener para switch de reportes
        switchReportes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "switchReportes changed: " + isChecked);
            updateFieldsState(isChecked, switchLogs.isChecked());
        });

        // Listener para switch de logs
        switchLogs.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "switchLogs changed: " + isChecked);
            updateFieldsState(switchReportes.isChecked(), isChecked);
        });

        // Listener para el botón guardar
        buttonGuardar.setOnClickListener(v -> {
            Log.d(TAG, "buttonGuardar clicked");
            savePreferences();
        });
    }

    private void savePreferences() {
        boolean reportesEnabled = switchReportes.isChecked();
        boolean logsEnabled = switchLogs.isChecked();
        String periodicidad = dropdownPeriodicidadReportes.getText().toString();
        String umbralLogsStr = layoutUmbralLogs.getEditText() != null ? 
            layoutUmbralLogs.getEditText().getText().toString() : "";

        if (reportesEnabled && periodicidad.isEmpty()) {
            Toast.makeText(this, "Por favor seleccione una periodicidad", Toast.LENGTH_SHORT).show();
            return;
        }

        if (logsEnabled && umbralLogsStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese un umbral de logs", Toast.LENGTH_SHORT).show();
            return;
        }

        notificationPreferences.setReportesEnabled(reportesEnabled);
        notificationPreferences.setLogsEnabled(logsEnabled);
        notificationPreferences.setPeriodicidadReportes(periodicidad);
        
        if (!umbralLogsStr.isEmpty()) {
            notificationPreferences.setUmbralLogs(Integer.parseInt(umbralLogsStr));
        }

        if (reportesEnabled) {
            scheduleReportesNotification(periodicidad);
        } else {
            workManager.cancelUniqueWork(WORK_REPORTES);
        }

        if (logsEnabled) {
            scheduleLogsNotification(Integer.parseInt(umbralLogsStr));
        } else {
            workManager.cancelUniqueWork(WORK_LOGS);
        }

        Toast.makeText(this, "Preferencias guardadas", Toast.LENGTH_SHORT).show();
    }

    private void scheduleReportesNotification(String periodicidad) {
        workManager.cancelUniqueWork(WORK_REPORTES);

        long interval = getIntervalFromPeriodicidad(periodicidad);
        if (interval <= 0) {
            Toast.makeText(this, "Error al configurar la periodicidad", Toast.LENGTH_SHORT).show();
            return;
        }

        PeriodicWorkRequest reportesWork = new PeriodicWorkRequest.Builder(
            NotificationWorker.class, interval, TimeUnit.MILLISECONDS)
            .setInputData(new androidx.work.Data.Builder()
                .putString("type", "reportes")
                .build())
            .setInitialDelay(interval, TimeUnit.MILLISECONDS)
            .build();

        workManager.enqueueUniquePeriodicWork(
            WORK_REPORTES,
            ExistingPeriodicWorkPolicy.REPLACE,
            reportesWork
        );

        String mensaje = "Notificaciones programadas para ";
        switch (periodicidad) {
            case "Cada minuto":
                mensaje += "cada 1 minuto";
                break;
            case "Diario":
                mensaje += "cada 24 horas";
                break;
            case "Semanal":
                mensaje += "cada 7 días";
                break;
            case "Mensual":
                mensaje += "cada 30 días";
                break;
        }
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
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

    private void scheduleLogsNotification(int umbral) {
        workManager.cancelUniqueWork(WORK_LOGS);

        PeriodicWorkRequest logsWork = new PeriodicWorkRequest.Builder(
            NotificationWorker.class, 15, TimeUnit.MINUTES)
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
}



