package com.iot.stayflowdev.Driver.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.iot.stayflowdev.Driver.Dtos.Vehiculo;
import com.iot.stayflowdev.Driver.Repository.VehiculoRepository;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.databinding.ActivityDriverVehicleEditBinding;

import java.util.HashMap;
import java.util.Map;

public class DriverVehicleEditActivity extends AppCompatActivity {

    private static final String TAG = "DriverVehicleEdit";
    private ActivityDriverVehicleEditBinding binding;
    private VehiculoRepository vehiculoRepository;
    private Vehiculo currentVehicle;
    private String vehiclePlaca;
    private Uri selectedImageUri;
    private boolean imageChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Inicializar View Binding
        binding = ActivityDriverVehicleEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar repository
        vehiculoRepository = new VehiculoRepository();

        setupToolbar();
        getIntentData();
        setupClickListeners();
        loadVehicleData();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void getIntentData() {
        Intent intent = getIntent();
        vehiclePlaca = intent.getStringExtra("vehicle_placa");

        if (vehiclePlaca == null || vehiclePlaca.isEmpty()) {
            showSnackbar("Error: No se encontró información del vehículo");
            finish();
        }
    }

    private void setupClickListeners() {
        // Click en el contenedor de la foto para cambiarla
        binding.photoContainer.setOnClickListener(v -> openImagePicker());

        // Switch de estado activo/inactivo
        binding.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateStatusDescription(isChecked);
        });

        // Botón cancelar
        binding.btnCancel.setOnClickListener(v -> {
            if (hasChanges()) {
                showCancelDialog();
            } else {
                finish();
            }
        });

        // Botón guardar
        binding.btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void loadVehicleData() {
        showLoading(true);

        vehiculoRepository.obtenerVehiculoPorPlaca(vehiclePlaca,
                vehiculo -> {
                    runOnUiThread(() -> {
                        showLoading(false);
                        currentVehicle = vehiculo;
                        populateFields(vehiculo);
                    });
                },
                exception -> {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Log.e(TAG, "Error al cargar vehículo", exception);
                        showSnackbar("Error al cargar información del vehículo");
                        finish();
                    });
                }
        );
    }

    private void populateFields(Vehiculo vehiculo) {
        if (vehiculo == null) return;

        // Llenar campos
        binding.etPlaca.setText(vehiculo.getPlacaFormateada());
        binding.etModelo.setText(vehiculo.getModelo());

        // Configurar switch de estado
        binding.switchActive.setChecked(vehiculo.isActivo());
        updateStatusDescription(vehiculo.isActivo());

        // Cargar imagen
        loadVehicleImage(vehiculo.getFotoVehiculo());
    }

    private void loadVehicleImage(String photoUrl) {
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Log.d(TAG, "Cargando imagen desde: " + photoUrl);

            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.pexels_alexgtacar_745150_1592384)
                    .error(R.drawable.pexels_alexgtacar_745150_1592384)
                    .centerCrop()
                    .into(binding.ivVehiclePhoto);
        } else {
            Log.d(TAG, "Sin imagen, usando por defecto");
            binding.ivVehiclePhoto.setImageResource(R.drawable.pexels_alexgtacar_745150_1592384);
        }
    }

    private void updateStatusDescription(boolean isActive) {
        if (isActive) {
            binding.tvStatusDescription.setText("Activo - Disponible para viajes");
            binding.tvStatusDescription.setTextColor(ContextCompat.getColor(this, R.color.green_500));
            binding.ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
            binding.ivStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.green_500));
        } else {
            binding.tvStatusDescription.setText("Inactivo - No disponible para viajes");
            binding.tvStatusDescription.setTextColor(ContextCompat.getColor(this, R.color.md_theme_error));
            binding.ivStatusIcon.setImageResource(R.drawable.ic_cancel);
            binding.ivStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.md_theme_error));
        }
    }

    private void openImagePicker() {
        // Verificar permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_STORAGE);
            return;
        }

        // Intent para seleccionar imagen
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private void validateAndSave() {
        // Validar modelo
        String modelo = binding.etModelo.getText().toString().trim();

        if (modelo.isEmpty()) {
            binding.tilModelo.setError("El modelo es requerido");
            binding.etModelo.requestFocus();
            return;
        }

        // Limpiar error
        binding.tilModelo.setError(null);

        // Verificar si hay cambios
        if (!hasChanges()) {
            showSnackbar("No se detectaron cambios");
            return;
        }

        // Guardar cambios
        saveChanges();
    }

    private boolean hasChanges() {
        if (currentVehicle == null) return false;

        String newModelo = binding.etModelo.getText().toString().trim();
        boolean newStatus = binding.switchActive.isChecked();

        // Verificar cambios en campos
        boolean modeloChanged = !newModelo.equals(currentVehicle.getModelo());
        boolean statusChanged = newStatus != currentVehicle.isActivo();

        return modeloChanged || statusChanged || imageChanged;
    }

    private void saveChanges() {
        showLoading(true);
        binding.btnSave.setEnabled(false);

        // Preparar datos para actualizar
        Map<String, Object> updateData = new HashMap<>();

        String newModelo = binding.etModelo.getText().toString().trim();
        boolean newStatus = binding.switchActive.isChecked();

        updateData.put("modelo", newModelo);
        updateData.put("activo", newStatus);

        // Si hay imagen nueva, subirla primero
        if (selectedImageUri != null && imageChanged) {
            uploadImageAndSave(updateData);
        } else {
            updateVehicleData(updateData);
        }
    }

    private void uploadImageAndSave(Map<String, Object> updateData) {
        showSnackbar("Subiendo imagen...");

        vehiculoRepository.subirFotoVehiculo(
                vehiclePlaca,
                selectedImageUri,
                downloadUrl -> {
                    // Agregar URL de imagen a los datos
                    updateData.put("fotoVehiculo", downloadUrl);
                    updateVehicleData(updateData);
                },
                exception -> {
                    runOnUiThread(() -> {
                        showLoading(false);
                        binding.btnSave.setEnabled(true);
                        Log.e(TAG, "Error al subir imagen", exception);
                        showSnackbar("Error al subir la imagen");
                    });
                },
                taskSnapshot -> {
                    // Progreso de subida
                    int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                    Log.d(TAG, "Progreso: " + progress + "%");
                }
        );
    }

    private void updateVehicleData(Map<String, Object> updateData) {
        vehiculoRepository.actualizarVehiculo(vehiclePlaca, updateData,
                aVoid -> {
                    runOnUiThread(() -> {
                        showLoading(false);
                        binding.btnSave.setEnabled(true);
                        showSnackbar("Vehículo actualizado correctamente");

                        // Retornar resultado exitoso
                        setResult(RESULT_OK);
                        finish();
                    });
                },
                exception -> {
                    runOnUiThread(() -> {
                        showLoading(false);
                        binding.btnSave.setEnabled(true);
                        Log.e(TAG, "Error al actualizar vehículo", exception);
                        showSnackbar("Error al guardar los cambios");
                    });
                }
        );
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Descartar cambios")
                .setMessage("¿Estás seguro de que quieres descartar los cambios realizados?")
                .setPositiveButton("Descartar", (dialog, which) -> finish())
                .setNegativeButton("Continuar editando", null)
                .show();
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);

        // Deshabilitar controles durante loading
        binding.btnSave.setEnabled(!show);
        binding.btnCancel.setEnabled(!show);
        binding.photoContainer.setEnabled(!show);
        binding.etModelo.setEnabled(!show);
        binding.switchActive.setEnabled(!show);
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (data != null && data.getData() != null) {
                selectedImageUri = data.getData();
                imageChanged = true;

                // Mostrar imagen seleccionada inmediatamente
                Glide.with(this)
                        .load(selectedImageUri)
                        .centerCrop()
                        .into(binding.ivVehiclePhoto);

                showSnackbar("Imagen seleccionada. Guarda los cambios para aplicarla.");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_READ_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                showSnackbar("Permiso necesario para seleccionar imagen");
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (hasChanges()) {
            showCancelDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    // Constantes
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final int PERMISSION_REQUEST_READ_STORAGE = 2001;
}