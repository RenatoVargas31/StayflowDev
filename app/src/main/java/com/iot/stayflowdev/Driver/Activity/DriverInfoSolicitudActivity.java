package com.iot.stayflowdev.Driver.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.iot.stayflowdev.Driver.Dtos.SolicitudTaxi;
import com.iot.stayflowdev.R;

public class DriverInfoSolicitudActivity extends AppCompatActivity {

    // Declaración de vistas
    private MaterialToolbar toolbar;
    private Chip estadoChip;
    private MaterialTextView horaValor;
    private MaterialTextView nombrePasajero;
    private MaterialTextView telefonoPasajero;
    private MaterialTextView numeroPasajeros;
    private MaterialTextView tipoVehiculo;
    private MaterialTextView direccionOrigen;
    private MaterialTextView direccionDestino;
    private MaterialTextView distanciaValor;
    private MaterialTextView tiempoValor;
    private MaterialTextView notasContenido;
    private MaterialButton btnAceptar;
    private MaterialButton btnRechazar;
    private MaterialTextView origen;
    private MaterialTextView destino;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_info_solicitud);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        inicializarVistas();

        // Configurar toolbar
        configurarToolbar();

        // Recuperar datos del intent
        recuperarDatosIntent();

        // Configurar botones
        configurarBotones();
    }

    private void inicializarVistas() {
        toolbar = findViewById(R.id.toolbar);
        estadoChip = findViewById(R.id.estadoChip);
        horaValor = findViewById(R.id.horaValor);
        nombrePasajero = findViewById(R.id.nombrePasajero);
        telefonoPasajero = findViewById(R.id.telefonoPasajero);
        numeroPasajeros = findViewById(R.id.numeroPasajeros);
        tipoVehiculo = findViewById(R.id.tipoVehiculo);
        direccionOrigen = findViewById(R.id.direccionOrigen);
        origen = findViewById(R.id.direccionOrigenName);
        destino = findViewById(R.id.direccionDestinoName);
        direccionDestino = findViewById(R.id.direccionDestino);
        distanciaValor = findViewById(R.id.distanciaValor);
        tiempoValor = findViewById(R.id.tiempoValor);
        notasContenido = findViewById(R.id.notasContenido);
        btnAceptar = findViewById(R.id.btnAceptar);
        btnRechazar = findViewById(R.id.btnRechazar);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        // Habilitar botón de retroceso
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void recuperarDatosIntent() {
        SolicitudTaxi solicitud = (SolicitudTaxi) getIntent().getSerializableExtra("solicitud");

        if (solicitud == null) {
            Toast.makeText(this, "No se recibió la solicitud", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mostrar los datos
        nombrePasajero.setText(solicitud.getNombrePasajero());
        telefonoPasajero.setText(solicitud.getTelefonoPasajero());
        numeroPasajeros.setText(solicitud.getNumeroPasajeros() + " pasajeros");
        tipoVehiculo.setText(solicitud.getTipoVehiculo());
        direccionOrigen.setText(solicitud.getOrigenDireccion());
        origen.setText(solicitud.getOrigen());
        destino.setText(solicitud.getDestino());
        direccionDestino.setText(solicitud.getDestinoDireccion());
        distanciaValor.setText("~3 km");  // puedes calcularlo más adelante
        tiempoValor.setText("~15 min");  // estimado ficticio
        notasContenido.setText(solicitud.getNotas());

        // Puedes usar estado fijo o dinámico
        configurarEstadoChip("pendiente");
    }


    private void configurarEstadoChip(String status) {
        estadoChip.setText(status);

        // Configurar colores según el estado
        switch (status.toLowerCase()) {
            case "pendiente":
                estadoChip.setChipBackgroundColorResource(android.R.color.holo_orange_light);
                estadoChip.setChipIconTintResource(android.R.color.holo_orange_dark);
                estadoChip.setChipStrokeColorResource(android.R.color.holo_orange_dark);
                // En un caso real, se usaría un ícono específico para cada estado
                // estadoChip.setChipIconResource(R.drawable.ic_pending);
                break;
            case "aceptada":
                estadoChip.setChipBackgroundColorResource(android.R.color.holo_green_light);
                estadoChip.setChipIconTintResource(android.R.color.holo_green_dark);
                estadoChip.setChipStrokeColorResource(android.R.color.holo_green_dark);
                // estadoChip.setChipIconResource(R.drawable.ic_accepted);
                break;
            case "cancelada":
                estadoChip.setChipBackgroundColorResource(android.R.color.holo_red_light);
                estadoChip.setChipIconTintResource(android.R.color.holo_red_dark);
                estadoChip.setChipStrokeColorResource(android.R.color.holo_red_dark);
                // estadoChip.setChipIconResource(R.drawable.ic_cancelled);
                break;
            default:
                // Estado por defecto
                break;
        }
    }

    private void configurarBotones() {
        btnAceptar.setOnClickListener(v -> {
            // Cambiar el estado de la solicitud a "Aceptada"
            configurarEstadoChip("Aceptada");

            // Mostrar mensaje de confirmación
            Snackbar.make(findViewById(R.id.main), "Solicitud aceptada", Snackbar.LENGTH_LONG).show();

            // Aquí iría el código para actualizar el estado en la base de datos
            // También podrías iniciar la navegación al punto de recogida

            // Deshabilitar los botones después de aceptar
            btnAceptar.setEnabled(false);
            btnRechazar.setEnabled(false);
        });

        btnRechazar.setOnClickListener(v -> {
            // Mostrar diálogo de confirmación
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Cancelar solicitud")
                    .setMessage("¿Estás seguro de que deseas cancelar esta solicitud?")
                    .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                        // Cambiar el estado de la solicitud a "Cancelada"
                        configurarEstadoChip("Cancelada");

                        // Mostrar mensaje de confirmación
                        Snackbar.make(findViewById(R.id.main), "Solicitud cancelada", Snackbar.LENGTH_LONG).show();

                        // Aquí iría el código para actualizar el estado en la base de datos

                        // Deshabilitar los botones después de cancelar
                        btnAceptar.setEnabled(false);
                        btnRechazar.setEnabled(false);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // vuelve a la actividad anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}