package com.iot.stayflowdev.superAdmin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.SystemLog;
import com.iot.stayflowdev.superAdmin.adapter.LogsAdapter;
import com.iot.stayflowdev.superAdmin.model.LogItem;
import com.iot.stayflowdev.superAdmin.utils.LogExportManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LogsActivity extends BaseSuperAdminActivity {

    private static final String TAG = "LogsActivity";
    private static final String COLLECTION_LOGS = "system_logs";
    private static final int PERMISSION_REQUEST_STORAGE = 1001;

    private LogsAdapter logsAdapter;
    private RecyclerView recyclerView;
    private CircularProgressIndicator progressIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private FirebaseFirestore db;
    private List<LogItem> logItems = new ArrayList<>();

    // Variable para mantener el filtro de categoría actual
    private String currentCategoryFilter = LogItem.CATEGORY_ALL;

    // Variables para guardar la operación pendiente
    private boolean exportPdfPending = false;
    private boolean exportCsvPending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        recyclerView = findViewById(R.id.logsRecyclerView);
        ChipGroup chipGroup = findViewById(R.id.chipGroup);
        progressIndicator = findViewById(R.id.progressIndicator);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        emptyView = findViewById(R.id.emptyView);

        if (progressIndicator == null) {
            // Si no existe en el layout, lo creamos programáticamente
            progressIndicator = new CircularProgressIndicator(this);
            progressIndicator.setIndeterminate(true);
        }

        // Configurar SwipeRefreshLayout si existe en el layout
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadLogsFromFirestore);
            swipeRefreshLayout.setColorSchemeResources(
                R.color.blue_500,
                R.color.md_theme_inversePrimary_highContrast,
                R.color.md_theme_onBackground
            );
        }

        // Set up filter chips
        Chip chipAll = findViewById(R.id.chipAll);
        Chip chipHotels = findViewById(R.id.chipHotels);
        Chip chipAccount = findViewById(R.id.chipAccount);
        Chip chipReservation = findViewById(R.id.chipReservation);

        // Initialize the adapter with empty list (will be populated from Firestore)
        logsAdapter = new LogsAdapter(logItems);
        recyclerView.setAdapter(logsAdapter);

        // Configurar el listener para mostrar el diálogo al hacer clic en un log
        logsAdapter.setOnItemClickListener(this::showLogDetailsDialog);

        // Set up chip listeners
        chipAll.setOnClickListener(v -> {
            currentCategoryFilter = LogItem.CATEGORY_ALL;
            logsAdapter.filterByCategory(currentCategoryFilter);
        });

        chipHotels.setOnClickListener(v -> {
            currentCategoryFilter = LogItem.CATEGORY_HOTELS;
            logsAdapter.filterByCategory(currentCategoryFilter);
        });

        chipAccount.setOnClickListener(v -> {
            currentCategoryFilter = LogItem.CATEGORY_ACCOUNT;
            logsAdapter.filterByCategory(currentCategoryFilter);
        });

        chipReservation.setOnClickListener(v -> {
            currentCategoryFilter = LogItem.CATEGORY_RESERVATION;
            logsAdapter.filterByCategory(currentCategoryFilter);
        });

        // Alternatively, you can use ChipGroup's selection listener
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                currentCategoryFilter = LogItem.CATEGORY_ALL;
                logsAdapter.filterByCategory(currentCategoryFilter);
            } else if (checkedId == R.id.chipHotels) {
                currentCategoryFilter = LogItem.CATEGORY_HOTELS;
                logsAdapter.filterByCategory(currentCategoryFilter);
            } else if (checkedId == R.id.chipAccount) {
                currentCategoryFilter = LogItem.CATEGORY_ACCOUNT;
                logsAdapter.filterByCategory(currentCategoryFilter);
            } else if (checkedId == R.id.chipReservation) {
                currentCategoryFilter = LogItem.CATEGORY_RESERVATION;
                logsAdapter.filterByCategory(currentCategoryFilter);
            }
        });

        // Cargar logs desde Firestore
        loadLogsFromFirestore();

        // NO marcar automáticamente todos los logs como leídos al abrir la vista
        // Los logs se marcarán como leídos individualmente cuando se abran sus detalles
    }

    private void loadLogsFromFirestore() {
        showLoading(true);

        // Limpiar la lista actual
        logItems.clear();

        // Consultar Firestore para obtener los logs ordenados por fecha (más recientes primero)
        db.collection(COLLECTION_LOGS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100) // Limitar a 100 logs para mejorar rendimiento
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Limpiar la lista antes de añadir nuevos elementos
                    logItems.clear();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Convertir documento de Firestore a SystemLog
                        SystemLog systemLog = document.toObject(SystemLog.class);
                        // Establecer el ID del documento
                        systemLog.setId(document.getId());

                        // Convertir SystemLog a LogItem (para compatibilidad con el adapter actual)
                        LogItem logItem = convertSystemLogToLogItem(systemLog);
                        logItems.add(logItem);
                    }

                    // Notificar al adapter que los datos han cambiado
                    logsAdapter.updateData(logItems);

                    // Mostrar mensaje si no hay logs
                    updateEmptyView();
                } else {
                    Log.e(TAG, "Error obteniendo logs de Firestore", task.getException());
                }

                showLoading(false);

                // Detener animación de recarga si está activa
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
    }

    /**
     * Convierte un SystemLog a LogItem para mantener compatibilidad con el adapter existente
     */
    private LogItem convertSystemLogToLogItem(SystemLog systemLog) {
        // Formato para la fecha
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String formattedTime = systemLog.getTimestamp() != null && systemLog.getTimestamp().toDate() != null ?
            timeFormat.format(systemLog.getTimestamp().toDate()) : "N/A";

        // Convertir categoría
        String category;
        int iconResId;

        // Determinar categoría e icono
        switch (systemLog.getCategory()) {
            case SystemLog.CATEGORY_HOTELS:
                category = LogItem.CATEGORY_HOTELS;
                iconResId = R.drawable.ic_hotel;
                break;
            case SystemLog.CATEGORY_ACCOUNT:
                category = LogItem.CATEGORY_ACCOUNT;
                iconResId = R.drawable.ic_perfil;
                break;
            case SystemLog.CATEGORY_RESERVATION:
                category = LogItem.CATEGORY_RESERVATION;
                iconResId = R.drawable.ic_calendar;
                break;
            default:
                category = LogItem.CATEGORY_ALL;
                iconResId = R.drawable.ic_layers;
                break;
        }

        return new LogItem(
            systemLog.getId(),
            systemLog.getTitle(),
            formattedTime,
            systemLog.getDescription(),
            category,
            iconResId,
            systemLog.isLeido()
        );
    }

    private void updateEmptyView() {
        if (emptyView != null) {
            emptyView.setVisibility(logItems.isEmpty() ? View.VISIBLE : View.GONE);
        }

        if (recyclerView != null) {
            recyclerView.setVisibility(logItems.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        // Si hay carga en progreso, no mostrar el mensaje de vacío
        if (isLoading && emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.superadmin_logs_fragment;
    }

    @Override
    protected int getBottomNavigationSelectedItem() {
        // No hay un ítem específico para logs en la navegación inferior
        // Normalmente podría devolverse el ítem desde donde se llegó, o un valor por defecto
        return R.id.nav_inicio; // Podemos usar inicio como predeterminado
    }

    // Añadimos también el método para establecer el título de la barra superior
    @Override
    protected String getToolbarTitle() {
        return "Registros del Sistema";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logs_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_export_pdf) {
            checkPermissionAndExportPdf();
            return true;
        }
        else if (id == R.id.action_export_csv) {
            checkPermissionAndExportCsv();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Verifica los permisos antes de exportar a PDF
     */
    private void checkPermissionAndExportPdf() {
        // Para Android 10 (API 29) y superior, no necesitamos el permiso WRITE_EXTERNAL_STORAGE
        // porque usamos el almacenamiento específico de la aplicación (getExternalFilesDir)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            exportLogsAsPdf();
            return;
        }

        // Para Android 9 (API 28) y anteriores, verificar permiso
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Marcar que estamos esperando exportar a PDF
            exportPdfPending = true;
            exportCsvPending = false;

            // Solicitar el permiso
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_STORAGE);
        } else {
            // Ya tenemos el permiso
            exportLogsAsPdf();
        }
    }

    /**
     * Verifica los permisos antes de exportar a CSV
     */
    private void checkPermissionAndExportCsv() {
        // Para Android 10 (API 29) y superior, no necesitamos el permiso WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            exportLogsAsCsv();
            return;
        }

        // Para Android 9 (API 28) y anteriores, verificar permiso
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Marcar que estamos esperando exportar a CSV
            exportCsvPending = true;
            exportPdfPending = false;

            // Solicitar el permiso
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_STORAGE);
        } else {
            // Ya tenemos el permiso
            exportLogsAsCsv();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, proceder con la exportación pendiente
                if (exportPdfPending) {
                    exportLogsAsPdf();
                } else if (exportCsvPending) {
                    exportLogsAsCsv();
                }
            } else {
                // Permiso denegado
                Toast.makeText(this, "Se requiere permiso de almacenamiento para exportar archivos",
                        Toast.LENGTH_LONG).show();
            }

            // Resetear banderas
            exportPdfPending = false;
            exportCsvPending = false;
        }
    }

    /**
     * Exporta los logs actualmente filtrados a formato PDF
     */
    private void exportLogsAsPdf() {
        // Verificar si hay logs para exportar
        if (logsAdapter == null || logsAdapter.getFilteredItems().isEmpty()) {
            Toast.makeText(this, "No hay logs para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Obtener la lista filtrada actual
            List<LogItem> filteredLogs = logsAdapter.getFilteredItems();

            // Mostrar un mensaje de que se está generando el archivo
            Toast.makeText(this, "Generando PDF...", Toast.LENGTH_SHORT).show();

            // Usar el LogExportManager para generar el PDF
            LogExportManager.exportToPdf(this, filteredLogs, currentCategoryFilter);
        } catch (Exception e) {
            Log.e(TAG, "Error al exportar a PDF", e);
            Toast.makeText(this, "Error al exportar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Exporta los logs actualmente filtrados a formato CSV
     */
    private void exportLogsAsCsv() {
        // Verificar si hay logs para exportar
        if (logsAdapter == null || logsAdapter.getFilteredItems().isEmpty()) {
            Toast.makeText(this, "No hay logs para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Obtener la lista filtrada actual
            List<LogItem> filteredLogs = logsAdapter.getFilteredItems();

            // Mostrar un mensaje de que se está generando el archivo
            Toast.makeText(this, "Generando CSV...", Toast.LENGTH_SHORT).show();

            // Usar el LogExportManager para generar el CSV
            LogExportManager.exportToCsv(this, filteredLogs, currentCategoryFilter);
        } catch (Exception e) {
            Log.e(TAG, "Error al exportar a CSV", e);
            Toast.makeText(this, "Error al exportar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Muestra un diálogo con los detalles del log seleccionado
     */
    private void showLogDetailsDialog(LogItem logItem) {
        // Marcar este log específico como leído si no lo está ya
        if (!logItem.isRead && logItem.id != null) {
            markLogAsRead(logItem.id);
            logItem.isRead = true; // Actualizar el estado local
        }

        // Crear un diálogo personalizado
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_log_details, null);
        builder.setView(dialogView);

        // Obtener referencias a las vistas del diálogo
        ImageView iconView = dialogView.findViewById(R.id.dialogLogIcon);
        TextView titleView = dialogView.findViewById(R.id.dialogLogTitle);
        TextView timestampView = dialogView.findViewById(R.id.dialogLogTimestamp);
        TextView categoryView = dialogView.findViewById(R.id.dialogLogCategory);
        TextView descriptionView = dialogView.findViewById(R.id.dialogLogDescription);
        Button closeButton = dialogView.findViewById(R.id.btnCloseDialog);

        // Configurar las vistas con la información del log
        iconView.setImageResource(logItem.iconResId);
        titleView.setText(logItem.title);
        timestampView.setText(logItem.timestamp);

        // Convertir el código de categoría a texto más legible
        String categoryText;
        switch (logItem.category) {
            case LogItem.CATEGORY_HOTELS:
                categoryText = "Hoteles";
                break;
            case LogItem.CATEGORY_ACCOUNT:
                categoryText = "Cuentas de Usuario";
                break;
            case LogItem.CATEGORY_RESERVATION:
                categoryText = "Reservaciones";
                break;
            default:
                categoryText = "General";
                break;
        }
        categoryView.setText(categoryText);

        // Mostrar la descripción completa
        descriptionView.setText(logItem.description);

        // Crear y mostrar el diálogo
        final androidx.appcompat.app.AlertDialog dialog = builder.create();

        // Configurar el botón de cerrar
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Mostrar el diálogo
        dialog.show();
    }

    /**
     * Marca un log específico como leído en Firestore
     */
    private void markLogAsRead(String logId) {
        db.collection(COLLECTION_LOGS)
            .document(logId)
            .update("leido", true)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Log marcado como leído: " + logId);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al marcar log como leído: " + logId, e);
            });
    }

    /**
     * Marca todos los logs sin leer como leídos cuando se abre la vista de logs
     */
    private void markAllLogsAsRead() {
        db.collection(COLLECTION_LOGS)
            .whereEqualTo("leido", false)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    // Usar WriteBatch para actualizar múltiples documentos eficientemente
                    var batch = db.batch();

                    for (var document : queryDocumentSnapshots) {
                        batch.update(document.getReference(), "leido", true);
                    }

                    // Ejecutar el batch
                    batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Todos los logs marcados como leídos: " + queryDocumentSnapshots.size());
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error al marcar logs como leídos", e);
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al buscar logs sin leer", e);
            });
    }
}
