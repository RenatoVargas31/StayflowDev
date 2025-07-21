package com.iot.stayflowdev.superAdmin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.User;
import com.iot.stayflowdev.superAdmin.utils.NotificationPreferences;
import com.iot.stayflowdev.superAdmin.utils.NotificationWorker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PerfilActivity extends BaseSuperAdminActivity {
    private static final String TAG = "PerfilActivity";
    private static final String WORK_REPORTES = "work_reportes";
    private static final String WORK_LOGS = "work_logs";

    // UI Components para datos personales
    private ImageView imageViewProfile;
    private MaterialButton buttonChangePicture;
    private TextView textViewName;
    private TextView textViewEmail;
    private TextView textViewTipoDocumento;
    private TextView textViewNumeroDocumento;
    private TextView textViewFechaNacimiento;
    private TextView textViewTelefono;
    private TextView textViewDomicilio;
    private TextView textViewEstado;
    private CircularProgressIndicator progressIndicator;

    // UI Components para notificaciones
    private SwitchMaterial switchReportes;
    private SwitchMaterial switchLogs;
    private TextInputLayout layoutPeriodicidadReportes;
    private TextInputLayout layoutPeriodicidadLogs;
    private TextInputLayout layoutUmbralLogs;
    private AutoCompleteTextView dropdownPeriodicidadReportes;
    private AutoCompleteTextView dropdownPeriodicidadLogs;
    private MaterialButton buttonGuardarReportes;
    private MaterialButton buttonGuardarLogs;
    private MaterialButton buttonResetLogs;
    private MaterialButton buttonLogout;
    private TextView textViewLogsCounter;

    // Utilities
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private NotificationPreferences notificationPreferences;
    private WorkManager workManager;
    private List<String> periodicidades;
    private ArrayAdapter<String> dropdownAdapterReportes;
    private ArrayAdapter<String> dropdownAdapterLogs;
    private User currentUser;

    // Lanzador para seleccionar imagen
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupViews();
        setupImagePickerLauncher();
        loadUserData();
        loadSavedPreferences();
    }

    private void initializeComponents() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        notificationPreferences = new NotificationPreferences(this);
        workManager = WorkManager.getInstance(this);

        // Actualizar opciones de periodicidad a intervalos más específicos
        periodicidades = Arrays.asList(
            "Cada 15 minutos",
            "Cada 2 horas",
            "Cada 4 horas",
            "Cada 6 horas"
        );
    }

    private void setupViews() {
        // Inicializar vistas de datos personales
        imageViewProfile = findViewById(R.id.imageViewProfile);
        buttonChangePicture = findViewById(R.id.buttonChangePicture);
        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewTipoDocumento = findViewById(R.id.textViewTipoDocumento);
        textViewNumeroDocumento = findViewById(R.id.textViewNumeroDocumento);
        textViewFechaNacimiento = findViewById(R.id.textViewFechaNacimiento);
        textViewTelefono = findViewById(R.id.textViewTelefono);
        textViewDomicilio = findViewById(R.id.textViewDomicilio);
        textViewEstado = findViewById(R.id.textViewEstado);
        progressIndicator = findViewById(R.id.progressIndicator);

        if (progressIndicator == null) {
            // Si no existe en el layout, creamos uno programáticamente
            progressIndicator = new CircularProgressIndicator(this);
            progressIndicator.setIndeterminate(true);
            progressIndicator.setVisibility(View.GONE);
        }

        // Inicializar vistas de notificaciones
        switchReportes = findViewById(R.id.switchReportes);
        switchLogs = findViewById(R.id.switchLogs);
        layoutPeriodicidadReportes = findViewById(R.id.layoutPeriodicidadReportes);
        layoutPeriodicidadLogs = findViewById(R.id.layoutPeriodicidadLogs);
        layoutUmbralLogs = findViewById(R.id.layoutUmbralLogs);
        dropdownPeriodicidadReportes = findViewById(R.id.dropdownPeriodicidadReportes);
        dropdownPeriodicidadLogs = findViewById(R.id.dropdownPeriodicidadLogs);
        buttonGuardarReportes = findViewById(R.id.buttonGuardarReportes);
        buttonGuardarLogs = findViewById(R.id.buttonGuardarLogs);
        buttonResetLogs = findViewById(R.id.buttonResetLogs);
        buttonLogout = findViewById(R.id.buttonLogout);
        textViewLogsCounter = findViewById(R.id.textViewLogsCounter);

        // Configurar estados iniciales
        layoutPeriodicidadReportes.setEnabled(false);
        layoutPeriodicidadLogs.setEnabled(false);
        layoutUmbralLogs.setEnabled(false);

        // Configurar botón para cambiar foto
        buttonChangePicture.setOnClickListener(v -> openImagePicker());

        setupDropdown();
        setupListeners();
    }

    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadProfileImage(imageUri);
                    }
                }
            }
        );
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        if (currentUser == null || mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "No se pudo identificar al usuario actual", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        try {
            // Comprimir la imagen para reducir tamaño
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageData = baos.toByteArray();

            // Definir la ruta de almacenamiento (reemplazará la imagen existente si usa el mismo nombre)
            String userId = mAuth.getCurrentUser().getUid();
            String imageName = userId + ".jpg";
            StorageReference profileImageRef = storageRef.child("fotos_perfil/" + imageName);

            // Subir la imagen
            UploadTask uploadTask = profileImageRef.putBytes(imageData);
            uploadTask
                .addOnSuccessListener(taskSnapshot -> {
                    // Obtener URL de descarga
                    profileImageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        // Actualizar la URL en Firestore
                        updateProfileImageUrl(downloadUri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(PerfilActivity.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al subir imagen", e);
                });
        } catch (IOException e) {
            showLoading(false);
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error al procesar imagen", e);
        }
    }

    private void updateProfileImageUrl(String imageUrl) {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("usuarios").document(userId)
            .update("fotoPerfilUrl", imageUrl)
            .addOnSuccessListener(aVoid -> {
                showLoading(false);
                Toast.makeText(PerfilActivity.this, "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show();

                // Actualizar la imagen en la interfaz
                Glide.with(PerfilActivity.this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(imageViewProfile);

                // Actualizar el objeto usuario
                if (currentUser != null) {
                    currentUser.setFotoPerfilUrl(imageUrl);
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(PerfilActivity.this, "Error al actualizar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error al actualizar URL de imagen en Firestore", e);
            });
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario autenticado");
            Toast.makeText(this, "Error: No se pudo obtener la información del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        showLoading(true);

        db.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                showLoading(false);
                if (documentSnapshot.exists()) {
                    updateUIWithUserData(documentSnapshot);
                } else {
                    Log.e(TAG, "El documento del usuario no existe en Firestore");
                    Toast.makeText(this, "No se encontró la información del usuario", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e(TAG, "Error al obtener datos del usuario", e);
                Toast.makeText(this, "Error al cargar los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void updateUIWithUserData(DocumentSnapshot document) {
        this.currentUser = document.toObject(User.class);
        if (currentUser == null) {
            Log.e(TAG, "Error al convertir documento a objeto User");
            return;
        }

        // Mostrar nombres y apellidos
        String nombreCompleto = "";
        if (currentUser.getNombres() != null && !currentUser.getNombres().isEmpty()) {
            nombreCompleto += currentUser.getNombres();
        }
        if (currentUser.getApellidos() != null && !currentUser.getApellidos().isEmpty()) {
            nombreCompleto += " " + currentUser.getApellidos();
        }
        if (nombreCompleto.isEmpty()) {
            nombreCompleto = "Super Admin";
        }
        textViewName.setText(nombreCompleto);

        // Mostrar email
        if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
            textViewEmail.setText(currentUser.getEmail());
        } else {
            textViewEmail.setText("No disponible");
        }

        // Mostrar tipo de documento
        if (currentUser.getTipoDocumento() != null && !currentUser.getTipoDocumento().isEmpty()) {
            textViewTipoDocumento.setText(currentUser.getTipoDocumento());
        } else {
            textViewTipoDocumento.setText("No disponible");
        }

        // Mostrar número de documento
        if (currentUser.getNumeroDocumento() != null && !currentUser.getNumeroDocumento().isEmpty()) {
            textViewNumeroDocumento.setText(currentUser.getNumeroDocumento());
        } else {
            textViewNumeroDocumento.setText("No disponible");
        }

        // Mostrar fecha de nacimiento
        if (currentUser.getFechaNacimiento() != null && !currentUser.getFechaNacimiento().isEmpty()) {
            textViewFechaNacimiento.setText(currentUser.getFechaNacimiento());
        } else {
            textViewFechaNacimiento.setText("No disponible");
        }

        // Mostrar teléfono
        if (currentUser.getTelefono() != null && !currentUser.getTelefono().isEmpty()) {
            textViewTelefono.setText(currentUser.getTelefono());
        } else {
            textViewTelefono.setText("No disponible");
        }

        // Mostrar domicilio
        if (currentUser.getDomicilio() != null && !currentUser.getDomicilio().isEmpty()) {
            textViewDomicilio.setText(currentUser.getDomicilio());
        } else {
            textViewDomicilio.setText("No disponible");
        }

        // Mostrar estado usando el método isEstado()
        boolean estado = currentUser.isEstado();
        textViewEstado.setText(estado ? "Activo" : "Inactivo");

        // Cargar foto de perfil si existe
        if (currentUser.getFotoPerfilUrl() != null && !currentUser.getFotoPerfilUrl().isEmpty()) {
            // Quitar el tinte de la imagen cuando hay una foto de perfil real
            imageViewProfile.setImageTintList(null);
            imageViewProfile.setPadding(0, 0, 0, 0);

            Glide.with(this)
                .load(currentUser.getFotoPerfilUrl())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(imageViewProfile);
        }
    }

    // Muestra un indicador de carga
    private void showLoading(boolean isLoading) {
        // Mostrar u ocultar el indicador de progreso
        if (progressIndicator != null) {
            progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        // Si está cargando, deshabilitar navegación y controles; de lo contrario, habilitarlos
        setNavigationAndControlsEnabled(!isLoading);
    }

    private void setupDropdown() {
        dropdownAdapterReportes = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            periodicidades
        );

        dropdownPeriodicidadReportes.setAdapter(dropdownAdapterReportes);
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

        // SOLO GUARDAR LA PREFERENCIA, NO PROGRAMAR NOTIFICACIONES
        dropdownPeriodicidadReportes.setOnItemClickListener((parent, view, position, id) -> {
            String selected = periodicidades.get(position);
            Log.d(TAG, "Periodicidad seleccionada: " + selected);

            // Solo guardar la preferencia, las notificaciones se programan al guardar
            notificationPreferences.setPeriodicidadReportes(selected);
            Toast.makeText(this, "Periodicidad seleccionada: " + selected, Toast.LENGTH_SHORT).show();
        });

        // Configurar dropdown para logs
        dropdownAdapterLogs = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            periodicidades
        );

        dropdownPeriodicidadLogs.setAdapter(dropdownAdapterLogs);
        dropdownPeriodicidadLogs.setThreshold(0);

        // Comportamiento del dropdown de logs
        layoutPeriodicidadLogs.setEndIconOnClickListener(view -> {
            if (dropdownPeriodicidadLogs.isEnabled()) {
                dropdownPeriodicidadLogs.showDropDown();
            }
        });

        dropdownPeriodicidadLogs.setOnClickListener(view -> {
            if (dropdownPeriodicidadLogs.isEnabled()) {
                dropdownPeriodicidadLogs.showDropDown();
            }
        });

        // SOLO GUARDAR LA PREFERENCIA, NO PROGRAMAR NOTIFICACIONES
        dropdownPeriodicidadLogs.setOnItemClickListener((parent, view, position, id) -> {
            String selected = periodicidades.get(position);
            Log.d(TAG, "Periodicidad de logs seleccionada: " + selected);

            // Solo guardar la preferencia, las notificaciones se programan al guardar
            notificationPreferences.setPeriodicidadLogs(selected);
            Toast.makeText(this, "Periodicidad de logs seleccionada: " + selected, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        switchReportes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutPeriodicidadReportes.setEnabled(isChecked);
            dropdownPeriodicidadReportes.setEnabled(isChecked);
            buttonGuardarReportes.setEnabled(isChecked);
            if (!isChecked) {
                dropdownPeriodicidadReportes.setText("", false);
                // Cancelar notificaciones si se deshabilita
                workManager.cancelUniqueWork(WORK_REPORTES);
                workManager.cancelUniqueWork(WORK_REPORTES + "_periodic");
            }
        });

        switchLogs.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutPeriodicidadLogs.setEnabled(isChecked);
            dropdownPeriodicidadLogs.setEnabled(isChecked);
            layoutUmbralLogs.setEnabled(isChecked);
            buttonGuardarLogs.setEnabled(isChecked);
            if (layoutUmbralLogs.getEditText() != null) {
                layoutUmbralLogs.getEditText().setEnabled(isChecked);
            }
            if (!isChecked) {
                dropdownPeriodicidadLogs.setText("", false);
                if (layoutUmbralLogs.getEditText() != null) {
                    layoutUmbralLogs.getEditText().setText("");
                }
                // Cancelar notificaciones si se deshabilita
                workManager.cancelUniqueWork(WORK_LOGS);
                workManager.cancelUniqueWork(WORK_LOGS + "_periodic");
            }
        });

        buttonGuardarReportes.setOnClickListener(v -> saveReportesPreferences());
        buttonGuardarLogs.setOnClickListener(v -> saveLogsPreferences());
        buttonResetLogs.setOnClickListener(v -> resetLogsCounter());
        buttonLogout.setOnClickListener(v -> cerrarSesion());
    }

    private void cerrarSesion() {
        // 1. Cerrar sesión en Firebase
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();

        // 2. Limpiar SharedPreferences
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        // 3. Mostrar mensaje
        android.widget.Toast.makeText(this, "Sesión cerrada correctamente", android.widget.Toast.LENGTH_SHORT).show();

        // 4. Redirigir al login y limpiar la pila de actividades
        android.content.Intent intent = new android.content.Intent(this, com.iot.stayflowdev.LoginFireBaseActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadSavedPreferences() {
        boolean reportesEnabled = notificationPreferences.isReportesEnabled();
        boolean logsEnabled = notificationPreferences.isLogsEnabled();
        String periodicidadReportes = notificationPreferences.getPeriodicidadReportes();
        String periodicidadLogs = notificationPreferences.getPeriodicidadLogs();
        int umbralLogs = notificationPreferences.getUmbralLogs();

        switchReportes.setChecked(reportesEnabled);
        switchLogs.setChecked(logsEnabled);

        layoutPeriodicidadReportes.setEnabled(reportesEnabled);
        dropdownPeriodicidadReportes.setEnabled(reportesEnabled);
        buttonGuardarReportes.setEnabled(reportesEnabled);

        layoutPeriodicidadLogs.setEnabled(logsEnabled);
        dropdownPeriodicidadLogs.setEnabled(logsEnabled);
        layoutUmbralLogs.setEnabled(logsEnabled);

        if (periodicidadReportes != null && !periodicidadReportes.isEmpty()) {
            dropdownPeriodicidadReportes.setText(periodicidadReportes, false);
        }

        if (periodicidadLogs != null && !periodicidadLogs.isEmpty()) {
            dropdownPeriodicidadLogs.setText(periodicidadLogs, false);
        }

        if (umbralLogs > 0 && layoutUmbralLogs.getEditText() != null) {
            layoutUmbralLogs.getEditText().setText(String.valueOf(umbralLogs));
        }

        // Actualizar contador de logs
        updateLogsCounter();
    }

    private void saveReportesPreferences() {
        boolean reportesEnabled = switchReportes.isChecked();
        String periodicidad = dropdownPeriodicidadReportes.getText().toString();

        // Validaciones
        if (reportesEnabled && periodicidad.isEmpty()) {
            Toast.makeText(this, "Por favor seleccione una periodicidad", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar preferencias
        notificationPreferences.setReportesEnabled(reportesEnabled);
        notificationPreferences.setPeriodicidadReportes(periodicidad);

        // Programar o cancelar notificaciones
        if (reportesEnabled) {
            scheduleReportesNotification(periodicidad);
        } else {
            workManager.cancelUniqueWork(WORK_REPORTES);
        }

        Toast.makeText(this, "Preferencias de reportes guardadas correctamente", Toast.LENGTH_SHORT).show();
    }

    private void saveLogsPreferences() {
        boolean logsEnabled = switchLogs.isChecked();
        String periodicidadLogs = dropdownPeriodicidadLogs.getText().toString();
        String umbralLogsStr = "";
        int umbral = 0;

        if (layoutUmbralLogs.getEditText() != null) {
            umbralLogsStr = layoutUmbralLogs.getEditText().getText().toString();
            if (!umbralLogsStr.isEmpty()) {
                try {
                    umbral = Integer.parseInt(umbralLogsStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "El umbral debe ser un número válido", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        // Validaciones
        if (logsEnabled && (periodicidadLogs.isEmpty() || umbral <= 0)) {
            Toast.makeText(this, "Por favor complete la periodicidad y umbral de logs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar preferencias
        notificationPreferences.setLogsEnabled(logsEnabled);
        notificationPreferences.setPeriodicidadLogs(periodicidadLogs);
        notificationPreferences.setUmbralLogs(umbral);

        // Programar o cancelar notificaciones de logs
        if (logsEnabled && umbral > 0) {
            scheduleLogsNotification(umbral, periodicidadLogs);
        } else {
            workManager.cancelUniqueWork(WORK_LOGS);
            workManager.cancelUniqueWork(WORK_LOGS + "_periodic");
        }

        Toast.makeText(this, "Preferencias de logs guardadas correctamente", Toast.LENGTH_SHORT).show();
    }

    private void resetLogsCounter() {
        // Resetear el contador de logs en las preferencias
        notificationPreferences.resetLogsCount();

        // Actualizar la interfaz
        updateLogsCounter();

        Toast.makeText(this, "Contador de logs reseteado", Toast.LENGTH_SHORT).show();
    }

    private void updateLogsCounter() {
        // Usar la misma lógica que InicioActivity: consultar "system_logs" en lugar de "logs"
        if (textViewLogsCounter != null) {
            Log.d(TAG, "Consultando logs sin leer desde system_logs...");

            db.collection("system_logs")
                .whereEqualTo("leido", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        int realLogsCount = queryDocumentSnapshots.size();
                        textViewLogsCounter.setText("Logs sin leer actuales: " + realLogsCount);

                        // Sincronizar el contador local con el real
                        notificationPreferences.setLogsCount(realLogsCount);

                        Log.d(TAG, "Logs sin leer encontrados en system_logs: " + realLogsCount);
                        Log.d(TAG, "Total de documentos en la consulta: " + queryDocumentSnapshots.getDocuments().size());
                    } else {
                        Log.w(TAG, "La consulta de system_logs devolvió null");
                        textViewLogsCounter.setText("Logs sin leer actuales: 0");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al consultar logs sin leer de system_logs: " + e.getMessage(), e);
                    // Fallback: mostrar 0 si hay error en la consulta
                    textViewLogsCounter.setText("Logs sin leer actuales: 0");
                    Toast.makeText(this, "Error al consultar logs. Mostrando valor por defecto.", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void scheduleReportesNotification(String periodicidad) {
        long interval = getIntervalFromPeriodicidad(periodicidad);
        if (interval <= 0) return;

        Log.d(TAG, "Programando notificaciones con periodicidad: " + periodicidad + " (" + (interval / 60000) + " minutos)");

        // Primero, cancelamos TODOS los trabajos existentes para evitar conflictos
        // Esto es crucial para evitar que las notificaciones sigan llegando con el intervalo antiguo
        workManager.cancelUniqueWork(WORK_REPORTES);
        workManager.cancelUniqueWork(WORK_REPORTES + "_periodic");
        // También cancelar cualquier trabajo "suelto" que pueda estar ejecutándose
        workManager.cancelAllWorkByTag("reportes_notification");

        // Si el intervalo es menor que 15 minutos, usar OneTimeRequest con auto-reprogramación
        if (interval < TimeUnit.MINUTES.toMillis(15) && "Cada minuto".equals(periodicidad)) {
            // Para "Cada minuto", usamos una solicitud de trabajo única que se ejecuta inmediatamente
            // y luego programamos la siguiente notificación después de que se complete
            androidx.work.OneTimeWorkRequest reportesWork = new androidx.work.OneTimeWorkRequest.Builder(
                NotificationWorker.class)
                .addTag("reportes_notification") // Añadir etiqueta para facilitar la cancelación
                .setInputData(new androidx.work.Data.Builder()
                    .putString("type", "reportes")
                    .putBoolean("scheduleNext", true)  // Indica que se debe programar la siguiente notificación
                    .putLong("nextInterval", TimeUnit.MINUTES.toMillis(1))
                    .build())
                .build();

            workManager.enqueueUniqueWork(
                WORK_REPORTES,
                androidx.work.ExistingWorkPolicy.REPLACE,
                reportesWork
            );

            Toast.makeText(this, "Las notificaciones por minuto no pueden ser exactamente cada minuto debido a restricciones del sistema", Toast.LENGTH_LONG).show();
        } else {
            // Para intervalos más largos (>=15min), usar PeriodicWorkRequest
            // WorkManager requiere un mínimo de 15 minutos para tareas periódicas
            long finalInterval = Math.max(interval, TimeUnit.MINUTES.toMillis(15));

            Log.d(TAG, "Intervalo configurado para notificaciones periódicas: " + (finalInterval / 60000) + " minutos, intervalo solicitado: " + (interval / 60000) + " minutos");

            PeriodicWorkRequest reportesWork = new PeriodicWorkRequest.Builder(
                NotificationWorker.class,
                finalInterval,
                TimeUnit.MILLISECONDS)
                .addTag("reportes_notification") // Añadir etiqueta para facilitar la cancelación
                .setInputData(new androidx.work.Data.Builder()
                    .putString("type", "reportes")
                    .putBoolean("scheduleNext", false) // Asegurarse de que no se auto-reprograme
                    .putLong("intervalConfigured", interval) // Guardar el intervalo original configurado
                    .build())
                .build();

            workManager.enqueueUniquePeriodicWork(
                WORK_REPORTES + "_periodic",
                ExistingPeriodicWorkPolicy.REPLACE,
                reportesWork
            );

            Log.d(TAG, "Notificación periódica programada exitosamente cada " + periodicidad.toLowerCase());

            // Mostrar un toast informando del intervalo real
            if (interval != finalInterval) {
                Toast.makeText(this,
                    "Las notificaciones se programaron cada " + (finalInterval / 60000) + " minutos debido a restricciones del sistema",
                    Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                    "Notificaciones programadas cada " + periodicidad.toLowerCase(),
                    Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void scheduleLogsNotification(int umbral, String periodicidad) {
        // Permitir al usuario configurar el intervalo para los logs también
        String currentPeriodicidad = dropdownPeriodicidadReportes.getText().toString();
        long interval = getIntervalFromPeriodicidad(currentPeriodicidad);

        Log.d(TAG, "Programando notificaciones de logs con periodicidad: " + currentPeriodicidad + " (" + (interval / 60000) + " minutos)");

        // Cancelar trabajos existentes para evitar conflictos
        workManager.cancelUniqueWork(WORK_LOGS);
        workManager.cancelUniqueWork(WORK_LOGS + "_periodic");
        workManager.cancelAllWorkByTag("logs_notification");

        // Asegurar que el intervalo sea al menos 15 minutos
        long finalInterval = Math.max(interval, TimeUnit.MINUTES.toMillis(15));

        Log.d(TAG, "Intervalo final para notificaciones de logs: " + (finalInterval / 60000) + " minutos");

        PeriodicWorkRequest logsWork = new PeriodicWorkRequest.Builder(
            NotificationWorker.class,
            finalInterval,
            TimeUnit.MILLISECONDS)
            .addTag("logs_notification")
            .setInputData(new androidx.work.Data.Builder()
                .putString("type", "logs")
                .putInt("umbral", umbral)
                .putBoolean("scheduleNext", false)
                .putLong("intervalConfigured", interval)
                .build())
            .build();

        workManager.enqueueUniquePeriodicWork(
            WORK_LOGS + "_periodic",
            ExistingPeriodicWorkPolicy.REPLACE,
            logsWork
        );

        Log.d(TAG, "Notificación de logs programada con umbral: " + umbral);
    }

    private long getIntervalFromPeriodicidad(String periodicidad) {
        switch (periodicidad) {
            case "Cada 15 minutos":
                return TimeUnit.MINUTES.toMillis(15);
            case "Cada 2 horas":
                return TimeUnit.HOURS.toMillis(2);
            case "Cada 4 horas":
                return TimeUnit.HOURS.toMillis(4);
            case "Cada 6 horas":
                return TimeUnit.HOURS.toMillis(6);
            default:
                return TimeUnit.MINUTES.toMillis(15);  // Por defecto, 15 minutos (valor mínimo permitido)
        }
    }

    /**
     * Controla la navegación y elementos interactivos durante la carga de imágenes
     * @param isLoading true si se está cargando una imagen, false si ha terminado
     */
    private void setNavigationAndControlsEnabled(boolean enabled) {
        // Deshabilitar o habilitar la navegación inferior
        if (bottomNavigation != null) {
            bottomNavigation.setEnabled(enabled);
            for (int i = 0; i < bottomNavigation.getMenu().size(); i++) {
                bottomNavigation.getMenu().getItem(i).setEnabled(enabled);
            }
        }

        // Deshabilitar o habilitar el botón de cambiar foto
        if (buttonChangePicture != null) {
            buttonChangePicture.setEnabled(enabled);
        }

        // Deshabilitar o habilitar el botón de guardar y cerrar sesión
        if (buttonGuardarReportes != null) {
            buttonGuardarReportes.setEnabled(enabled);
        }

        if (buttonGuardarLogs != null) {
            buttonGuardarLogs.setEnabled(enabled);
        }

        if (buttonLogout != null) {
            buttonLogout.setEnabled(enabled);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.superadmin_perfil_superadmin_fixed;
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

