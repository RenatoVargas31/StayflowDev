package com.iot.stayflowdev.superAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.User;

import java.util.HashMap;
import java.util.Map;

public class DriverVerificationActivity extends BaseSuperAdminActivity {

    private TextView tvNombreCompleto, tvEmail, tvTelefono, tvDocumento, tvDomicilio;
    private TextView tvPlaca, tvModelo, tvMarca, tvColor, tvAnio;
    private ImageView ivFotoPerfil, ivFotoVehiculo, ivCedulaFrente, ivCedulaReverso;
    private ImageView ivLicenciaFrente, ivLicenciaReverso, ivSoat, ivTecnomecanica;
    private Button btnAprobar, btnRechazar;

    private User taxista;
    private FirebaseFirestore db;
    private String taxistaId;

    // Implementar métodos abstractos requeridos por BaseSuperAdminActivity
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_driver_verification;
    }

    @Override
    protected int getBottomNavigationSelectedItem() {
        return R.id.nav_gestion; // Mantener seleccionado el ítem de gestión
    }

    @Override
    protected String getToolbarTitle() {
        return "Verificación de Taxista";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // El layout ya se configura en BaseSuperAdminActivity

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Obtener el ID del taxista desde el Intent
        taxistaId = getIntent().getStringExtra("taxista_id");
        if (taxistaId == null) {
            Toast.makeText(this, "Error: No se recibió el ID del taxista", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupListeners();

        // Cargar datos del taxista
        loadDriverData();
    }

    private void initViews() {
        // Información personal
        tvNombreCompleto = findViewById(R.id.tvNombreCompleto);
        tvEmail = findViewById(R.id.tvEmail);
        tvTelefono = findViewById(R.id.tvTelefono);
        tvDocumento = findViewById(R.id.tvDocumento);
        tvDomicilio = findViewById(R.id.tvDomicilio);

        // Información del vehículo
        tvPlaca = findViewById(R.id.tvPlaca);
        tvModelo = findViewById(R.id.tvModelo);
        tvMarca = findViewById(R.id.tvMarca);
        tvColor = findViewById(R.id.tvColor);
        tvAnio = findViewById(R.id.tvAnio);

        // Imágenes
        ivFotoPerfil = findViewById(R.id.ivFotoPerfil);
        ivFotoVehiculo = findViewById(R.id.ivFotoVehiculo);
        ivCedulaFrente = findViewById(R.id.ivCedulaFrente);
        ivCedulaReverso = findViewById(R.id.ivCedulaReverso);
        ivLicenciaFrente = findViewById(R.id.ivLicenciaFrente);
        ivLicenciaReverso = findViewById(R.id.ivLicenciaReverso);
        ivSoat = findViewById(R.id.ivSoat);
        ivTecnomecanica = findViewById(R.id.ivTecnomecanica);

        // Botones
        btnAprobar = findViewById(R.id.btnAprobar);
        btnRechazar = findViewById(R.id.btnRechazar);
    }

    private void setupListeners() {
        btnAprobar.setOnClickListener(v -> showApprovalDialog());
        btnRechazar.setOnClickListener(v -> showRejectionDialog());

        // Configurar clicks en imágenes para verlas en tamaño completo
        setupImageClickListeners();
    }

    private void setupImageClickListeners() {
        View.OnClickListener imageClickListener = v -> {
            ImageView imageView = (ImageView) v;
            String imageUrl = (String) imageView.getTag();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                showFullScreenImage(imageUrl);
            }
        };

        ivFotoPerfil.setOnClickListener(imageClickListener);
        ivFotoVehiculo.setOnClickListener(imageClickListener);
        ivCedulaFrente.setOnClickListener(imageClickListener);
        ivCedulaReverso.setOnClickListener(imageClickListener);
        ivLicenciaFrente.setOnClickListener(imageClickListener);
        ivLicenciaReverso.setOnClickListener(imageClickListener);
        ivSoat.setOnClickListener(imageClickListener);
        ivTecnomecanica.setOnClickListener(imageClickListener);
    }

    private void loadDriverData() {
        showLoading(true);

        db.collection("usuarios").document(taxistaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);
                    if (documentSnapshot.exists()) {
                        taxista = documentSnapshot.toObject(User.class);
                        if (taxista != null) {
                            taxista.setUid(documentSnapshot.getId());
                            displayDriverInfo();
                        }
                    } else {
                        Toast.makeText(this, "Taxista no encontrado", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayDriverInfo() {
        // Mostrar información personal
        tvNombreCompleto.setText(taxista.getNombres() + " " + taxista.getApellidos());
        tvEmail.setText(taxista.getEmail());
        tvTelefono.setText(taxista.getTelefono());
        tvDocumento.setText(taxista.getTipoDocumento() + ": " + taxista.getNumeroDocumento());
        tvDomicilio.setText(taxista.getDomicilio());

        // Mostrar información del vehículo
        tvPlaca.setText(taxista.getPlaca());
        tvModelo.setText(taxista.getModelo());

        // Obtener datos específicos del vehículo desde datosEspecificos
        Map<String, Object> datosEspecificos = taxista.getDatosEspecificos();
        if (datosEspecificos != null) {
            tvMarca.setText(getStringFromMap(datosEspecificos, "marca"));
            tvColor.setText(getStringFromMap(datosEspecificos, "color"));
            tvAnio.setText(getStringFromMap(datosEspecificos, "anio"));

            // Cargar imágenes
            loadImageFromMap(datosEspecificos, "fotoVehiculo", ivFotoVehiculo);
            loadImageFromMap(datosEspecificos, "cedulaFrente", ivCedulaFrente);
            loadImageFromMap(datosEspecificos, "cedulaReverso", ivCedulaReverso);
            loadImageFromMap(datosEspecificos, "licenciaFrente", ivLicenciaFrente);
            loadImageFromMap(datosEspecificos, "licenciaReverso", ivLicenciaReverso);
            loadImageFromMap(datosEspecificos, "soat", ivSoat);
            loadImageFromMap(datosEspecificos, "tecnomecanica", ivTecnomecanica);
        }

        // Cargar foto de perfil
        if (taxista.getFotoPerfilUrl() != null && !taxista.getFotoPerfilUrl().isEmpty()) {
            Glide.with(this)
                    .load(taxista.getFotoPerfilUrl())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivFotoPerfil);
            ivFotoPerfil.setTag(taxista.getFotoPerfilUrl());
        }
    }

    private String getStringFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "No especificado";
    }

    private void loadImageFromMap(Map<String, Object> map, String key, ImageView imageView) {
        Object urlObj = map.get(key);
        if (urlObj != null) {
            String url = urlObj.toString();
            if (!url.isEmpty()) {
                Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_error)
                        .into(imageView);
                imageView.setTag(url);
            }
        }
    }

    private void showApprovalDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Aprobar Taxista")
                .setMessage("¿Está seguro que desea aprobar a este taxista? Esta acción permitirá que el taxista use la aplicación.")
                .setPositiveButton("Aprobar", (dialog, which) -> approveDriver())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showRejectionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Rechazar Taxista")
                .setMessage("¿Está seguro que desea rechazar a este taxista? Esta acción no permitirá que el taxista use la aplicación.")
                .setPositiveButton("Rechazar", (dialog, which) -> rejectDriver())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void approveDriver() {
        updateDriverStatus(true);
    }

    private void rejectDriver() {
        updateDriverStatus(false);
    }

    private void updateDriverStatus(boolean approved) {
        showLoading(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("verificado", approved);
        updates.put("estado", approved); // También actualizar el estado general

        db.collection("usuarios").document(taxistaId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    String message = approved ? "Taxista aprobado exitosamente" : "Taxista rechazado";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                    // Retornar resultado y cerrar actividad
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("driver_updated", true);
                    resultIntent.putExtra("driver_approved", approved);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error al actualizar estado: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showFullScreenImage(String imageUrl) {
        Intent intent = new Intent(this, FullScreenImageActivity.class);
        intent.putExtra("image_url", imageUrl);
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        // Implementar indicador de carga
        findViewById(R.id.progressBar).setVisibility(show ? View.VISIBLE : View.GONE);
        btnAprobar.setEnabled(!show);
        btnRechazar.setEnabled(!show);
    }
}
