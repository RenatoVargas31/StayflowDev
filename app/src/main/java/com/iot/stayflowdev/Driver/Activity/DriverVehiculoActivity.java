package com.iot.stayflowdev.Driver.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
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
import androidx.appcompat.widget.Toolbar;
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
import com.iot.stayflowdev.databinding.ActivityDriverVehiculoBinding;


public class DriverVehiculoActivity extends AppCompatActivity {

    private static final String TAG = "DriverVehiculoActivity";
    private ActivityDriverVehiculoBinding binding;
    private VehiculoRepository vehiculoRepository;
    private Vehiculo currentVehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Inicializar View Binding
        binding = ActivityDriverVehiculoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar repository
        vehiculoRepository = new VehiculoRepository();

        setupToolbar();
        initializeComponents();
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

    private void initializeComponents() {
        // Mostrar loading inicial
        showLoading(true);
    }

    private void setupClickListeners() {
        // Botón para editar vehículo
        binding.btnEditVehicle.setOnClickListener(v -> {
            if (currentVehicle != null) {
                openVehicleEdit();
            }
        });

        // Botón para activar/desactivar vehículo
        binding.btnToggleStatus.setOnClickListener(v -> {
            if (currentVehicle != null) {
                toggleVehicleStatus();
            }
        });

        // Click en la tarjeta del vehículo
        binding.cardCurrentVehicle.setOnClickListener(v -> {
            if (currentVehicle != null) {
                showVehicleDetails();
            }
        });
    }

    private void loadVehicleData() {
        // Obtener todos los vehículos del taxista
        vehiculoRepository.obtenerTodosVehiculosTaxista(
                vehiculos -> {
                    showLoading(false);
                    if (vehiculos != null && !vehiculos.isEmpty()) {
                        // Tomar el primer vehículo activo o el primero disponible
                        currentVehicle = vehiculos.stream()
                                .filter(v -> v.isActivo())
                                .findFirst()
                                .orElse(vehiculos.get(0));

                        showVehicleState(true);
                        displayVehicleData(currentVehicle);
                    } else {
                        showVehicleState(false);
                    }
                },
                exception -> {
                    showLoading(false);
                    Log.e("DriverVehiculoActivity", "Error al cargar vehículos", exception);
                    showVehicleState(false);
                    showSnackbar("Error al cargar información del vehículo");
                }
        );
    }

    private void showVehicleState(boolean hasVehicle) {
        if (hasVehicle) {
            binding.layoutSinVehiculo.setVisibility(View.GONE);
            binding.layoutConVehiculo.setVisibility(View.VISIBLE);
        } else {
            binding.layoutSinVehiculo.setVisibility(View.VISIBLE);
            binding.layoutConVehiculo.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean show) {
        // Puedes agregar un ProgressBar si lo necesitas
        // Por ahora solo ocultar/mostrar contenido
        if (show) {
            binding.layoutSinVehiculo.setVisibility(View.GONE);
            binding.layoutConVehiculo.setVisibility(View.GONE);
        }
    }

    private void displayVehicleData(Vehiculo vehiculo) {
        if (vehiculo == null) return;

        // Establecer datos básicos
        binding.tvVehiclePlate.setText(vehiculo.getPlacaFormateada());
        binding.tvVehicleModel.setText(vehiculo.getMarcaYModelo());
        binding.tvVehicleOwner.setText("Tú");

        // Configurar estado del vehículo
        updateVehicleStatus(vehiculo.isActivo());

        // Cargar imagen del vehículo
        loadVehicleImage(vehiculo.getFotoVehiculo());

        // Configurar botón de estado
        updateToggleButton(vehiculo.isActivo());
    }

    private void updateVehicleStatus(boolean isActive) {
        if (isActive) {
            binding.tvEstadoVehiculo.setText("Activo");
            binding.tvEstadoVehiculo.setTextColor(ContextCompat.getColor(this, R.color.green_500));
            binding.ivEstadoIcon.setImageResource(R.drawable.ic_check_circle);
            binding.ivEstadoIcon.setColorFilter(ContextCompat.getColor(this, R.color.green_500));
            binding.chipEstadoVehiculo.setBackgroundColor(ContextCompat.getColor(this, R.color.icon_bg_success));
            binding.tvVehicleStatusDetail.setText("Activo - Disponible para viajes");
            binding.tvVehicleStatusDetail.setTextColor(ContextCompat.getColor(this, R.color.green_500));
        } else {
            binding.tvEstadoVehiculo.setText("Inactivo");
            binding.tvEstadoVehiculo.setTextColor(ContextCompat.getColor(this, R.color.md_theme_error));
            binding.ivEstadoIcon.setImageResource(R.drawable.ic_cancel);
            binding.ivEstadoIcon.setColorFilter(ContextCompat.getColor(this, R.color.md_theme_error));
            binding.chipEstadoVehiculo.setBackgroundColor(ContextCompat.getColor(this, R.color.icon_bg_error));
            binding.tvVehicleStatusDetail.setText("Inactivo - No disponible para viajes");
            binding.tvVehicleStatusDetail.setTextColor(ContextCompat.getColor(this, R.color.md_theme_error));
        }
    }

    private void updateToggleButton(boolean isActive) {
        if (isActive) {
            binding.btnToggleStatus.setText("Desactivar");
            binding.btnToggleStatus.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_toggle_off));
            binding.btnToggleStatus.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_theme_error)));
            binding.btnToggleStatus.setTextColor(ContextCompat.getColor(this, R.color.md_theme_error));
            binding.btnToggleStatus.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_theme_error)));
        } else {
            binding.btnToggleStatus.setText("Activar");
            binding.btnToggleStatus.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_toggle_on));
            binding.btnToggleStatus.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green_500)));
            binding.btnToggleStatus.setTextColor(ContextCompat.getColor(this, R.color.green_500));
            binding.btnToggleStatus.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green_500)));
        }
    }

    private void loadVehicleImage(String photoUrl) {
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Log.d(TAG, "Cargando imagen de vehículo desde: " + photoUrl);

            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.pexels_alexgtacar_745150_1592384)
                    .error(R.drawable.pexels_alexgtacar_745150_1592384)
                    .into(binding.ivVehiclePhoto);

            binding.tvVehiclePhotoStatus.setText("Cargada desde Firebase");
            binding.layoutFotoInfo.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "Sin imagen de vehículo, usando imagen por defecto");
            binding.ivVehiclePhoto.setImageResource(R.drawable.pexels_alexgtacar_745150_1592384);
            binding.tvVehiclePhotoStatus.setText("Por defecto");
            binding.layoutFotoInfo.setVisibility(View.GONE);
        }
    }

    private void toggleVehicleStatus() {
        if (currentVehicle == null) return;

        boolean newStatus = !currentVehicle.isActivo();
        String placa = currentVehicle.getPlaca();

        // Mostrar loading en el botón
        binding.btnToggleStatus.setEnabled(false);

        if (newStatus) {
            vehiculoRepository.activarVehiculo(placa,
                    aVoid -> {
                        runOnUiThread(() -> {
                            currentVehicle.setActivo(true);
                            updateVehicleStatus(true);
                            updateToggleButton(true);
                            binding.btnToggleStatus.setEnabled(true);
                            showSnackbar("Vehículo activado correctamente");
                        });
                    },
                    exception -> {
                        runOnUiThread(() -> {
                            binding.btnToggleStatus.setEnabled(true);
                            Log.e("DriverVehiculoActivity", "Error al activar vehículo", exception);
                            showSnackbar("Error al activar el vehículo");
                        });
                    }
            );
        } else {
            vehiculoRepository.desactivarVehiculo(placa,
                    aVoid -> {
                        runOnUiThread(() -> {
                            currentVehicle.setActivo(false);
                            updateVehicleStatus(false);
                            updateToggleButton(false);
                            binding.btnToggleStatus.setEnabled(true);
                            showSnackbar("Vehículo desactivado correctamente");
                        });
                    },
                    exception -> {
                        runOnUiThread(() -> {
                            binding.btnToggleStatus.setEnabled(true);
                            Log.e("DriverVehiculoActivity", "Error al desactivar vehículo", exception);
                            showSnackbar("Error al desactivar el vehículo");
                        });
                    }
            );
        }
    }
    private void openVehicleEdit() {
        if (currentVehicle == null) return;

        // Intent para abrir la actividad de edición de vehículo
        Intent intent = new Intent(this, DriverVehicleEditActivity.class);
        intent.putExtra("vehicle_placa", currentVehicle.getPlaca());
        intent.putExtra("vehicle_modelo", currentVehicle.getModelo());
        startActivityForResult(intent, REQUEST_CODE_EDIT_VEHICLE);
    }

    private void openImagePicker() {
        // Verificar permisos primero
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_STORAGE);
            return;
        }

        // Crear intent para seleccionar imagen
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private void showVehicleDetails() {
        if (currentVehicle == null) return;

        // Crear diálogo con detalles adicionales
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detalles del vehículo")
                .setMessage("Placa: " + currentVehicle.getPlacaFormateada() + "\n" +
                        "Modelo: " + currentVehicle.getMarcaYModelo() + "\n" +
                        "Estado: " + (currentVehicle.isActivo() ? "Activo" : "Inactivo") + "\n" +
                        "Foto: " + (currentVehicle.tieneFoto() ? "Disponible" : "No disponible"))
                .setPositiveButton("Cerrar", null)
                .show();
    }
/*
    private void uploadVehicleImage(Uri imageUri) {
        if (currentVehicle == null || imageUri == null) return;

        // Mostrar progreso
        binding.fabCambiarFoto.setEnabled(false);
        showSnackbar("Subiendo imagen...");

        vehiculoRepository.subirFotoVehiculo(
                currentVehicle.getPlaca(),
                imageUri,
                downloadUrl -> {
                    runOnUiThread(() -> {
                        // Actualizar el objeto actual
                        currentVehicle.setFotoVehiculo(downloadUrl);

                        // Actualizar UI
                        loadVehicleImage(downloadUrl);
                        binding.fabCambiarFoto.setEnabled(true);
                        showSnackbar("Imagen actualizada correctamente");
                    });
                },
                exception -> {
                    runOnUiThread(() -> {
                        binding.fabCambiarFoto.setEnabled(true);
                        Log.e("DriverVehiculoActivity", "Error al subir imagen", exception);
                        showSnackbar("Error al subir la imagen");
                    });
                },
                taskSnapshot -> {
                    // Progreso de subida (opcional)
                    int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                    Log.d("DriverVehiculoActivity", "Progreso de subida: " + progress + "%");
                }
        );
    } */

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_REGISTER_VEHICLE:
                case REQUEST_CODE_EDIT_VEHICLE:
                    // Recargar datos del vehículo
                    loadVehicleData();
                    break;
                case REQUEST_CODE_PICK_IMAGE:
                    if (data != null && data.getData() != null) {
                        Uri imageUri = data.getData();
                        //uploadVehicleImage(imageUri);
                    }
                    break;
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
        binding = null; // Evitar memory leaks
    }

    // Constantes
    private static final int REQUEST_CODE_REGISTER_VEHICLE = 1001;
    private static final int REQUEST_CODE_EDIT_VEHICLE = 1002;
    private static final int REQUEST_CODE_PICK_IMAGE = 1003;
    private static final int PERMISSION_REQUEST_READ_STORAGE = 2001;
}