package com.iot.stayflowdev.adminHotel;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.adapter.ServicioAdapter;
import com.iot.stayflowdev.model.Servicio;
import com.iot.stayflowdev.adminHotel.repository.AdminHotelViewModel;
import com.iot.stayflowdev.databinding.ActivityServiciosAdminBinding;
import com.iot.stayflowdev.viewmodels.ServicioViewModel;

import java.util.ArrayList;

public class ServiciosAdminActivity extends AppCompatActivity {

    private ActivityServiciosAdminBinding binding;
    private ServicioAdapter servicioAdapter;
    private ServicioViewModel servicioViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServiciosAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.menu_inicio);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_inicio) {
                startActivity(new Intent(this, AdminInicioActivity.class));
                overridePendingTransition(0, 0);

                finish();
            } else if (id == R.id.menu_reportes) {
                startActivity(new Intent(this, ReportesAdminActivity.class));
                overridePendingTransition(0, 0);

                finish();

            } else if (id == R.id.menu_huesped) {
                startActivity(new Intent(this, HuespedAdminActivity.class));
                overridePendingTransition(0, 0);

                finish();

            } else if (id == R.id.menu_mensajeria) {
                startActivity(new Intent(this, MensajeriaAdminActivity.class));                overridePendingTransition(0, 0);
                overridePendingTransition(0, 0);

                finish();

            } else if (id == R.id.menu_perfil) {
                startActivity(new Intent(this, PerfilAdminActivity.class));
                overridePendingTransition(0, 0);

                finish();

            }
            overridePendingTransition(0, 0);
            return true;
        });

        servicioViewModel = new ViewModelProvider(this).get(ServicioViewModel.class);
        AdminHotelViewModel adminHotelViewModel = new ViewModelProvider(this).get(AdminHotelViewModel.class);

        adminHotelViewModel.getHotelId().observe(this, hotelId -> {
            if (hotelId == null || hotelId.isEmpty()) {
                mostrarError("Error", "No se encontró hotel asignado.");
                return;
            }

            servicioAdapter = new ServicioAdapter(new ArrayList<>(), new ServicioAdapter.OnServicioActionListener() {
                @Override
                public void onEditar(Servicio servicio) {
                    mostrarDialogoEditar(servicio, hotelId);
                }

                @Override
                public void onEliminar(Servicio servicio) {
                    confirmarEliminar(servicio, hotelId);
                }
            });

            binding.recyclerServicios.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerServicios.setAdapter(servicioAdapter);

            binding.fabAddService.setOnClickListener(v -> mostrarDialogoAgregar(hotelId));
            binding.btnMontoTaxi.setOnClickListener(v -> mostrarDialogoMontoMinimoTaxi(hotelId));


            servicioViewModel.getServicios(hotelId).observe(this, servicios -> servicioAdapter.updateData(servicios));
        });
    }

    private void mostrarDialogoAgregar(String hotelId) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_servicio, null);
        MaterialAutoCompleteTextView dropdown = view.findViewById(R.id.dropdownTipoServicio);
        TextInputEditText etDescripcion = view.findViewById(R.id.inputDescription);


        String[] servicios = {"Restaurante", "Piscina", "WiFi", "Estacionamiento", "Mascotas", "Gimnasio", "Spa"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, servicios);
        dropdown.setAdapter(adapter);
        dropdown.setKeyListener(null); // evita que escriban
        dropdown.setOnClickListener(v -> dropdown.showDropDown());


        new MaterialAlertDialogBuilder(this)
                .setTitle("Agregar Servicio")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = dropdown.getText().toString().trim();
                    String descripcion = etDescripcion.getText().toString().trim();


                    if (nombre.isEmpty() || descripcion.isEmpty()) {
                        mostrarError("Error", "Completa todos los campos antes de guardar.");
                        return;
                    }

                    Servicio s = new Servicio();
                    s.setNombre(nombre);
                    s.setDescripcion(descripcion);

                    servicioViewModel.agregar(hotelId, s, () -> mostrarMensaje("Servicio agregado correctamente."));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEditar(Servicio servicio, String hotelId) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_servicio, null);
        MaterialAutoCompleteTextView dropdown = view.findViewById(R.id.dropdownTipoServicio);
        TextInputEditText etDescripcion = view.findViewById(R.id.inputDescription);

        String[] servicios = {"Restaurante", "Piscina", "WiFi", "Estacionamiento", "Mascotas", "Gimnasio", "Spa"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, servicios);
        dropdown.setAdapter(adapter);
        dropdown.setKeyListener(null);
        dropdown.setOnClickListener(v -> dropdown.showDropDown());

        dropdown.setText(servicio.getNombre(), false);
        etDescripcion.setText(servicio.getDescripcion());

        boolean esGratis = servicio.getEsGratis() != null && servicio.getEsGratis();

        new MaterialAlertDialogBuilder(this)
                .setTitle("Editar Servicio")
                .setView(view)
                .setPositiveButton("Actualizar", (dialog, which) -> {
                    String nombre = dropdown.getText().toString().trim();
                    String descripcion = etDescripcion.getText().toString().trim();

                    if (nombre.isEmpty() || descripcion.isEmpty()) {
                        mostrarError("Error", "Completa todos los campos antes de guardar.");
                        return;
                    }

                    servicio.setNombre(nombre);
                    servicio.setDescripcion(descripcion);

                    servicioViewModel.actualizar(hotelId, servicio, () -> mostrarMensaje("Servicio actualizado."));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoMontoMinimoTaxi(String hotelId) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_input_monto_taxi_admin, null);
        TextInputEditText input = view.findViewById(R.id.inputMontoTaxi);

        FirebaseFirestore.getInstance()
                .collection("hoteles")
                .document(hotelId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && document.contains("montoMinimoTaxiGratis")) {
                        double montoActual = document.getDouble("montoMinimoTaxiGratis");
                        input.setText(String.valueOf(montoActual));
                    }

                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Monto mínimo para taxi gratis")
                            .setView(view)
                            .setPositiveButton("Guardar", (dialog, which) -> {
                                String valor = input.getText().toString().trim();
                                if (!valor.isEmpty()) {
                                    double montoNuevo = Double.parseDouble(valor);
                                    FirebaseFirestore.getInstance()
                                            .collection("hoteles")
                                            .document(hotelId)
                                            .update("montoMinimoTaxiGratis", montoNuevo)
                                            .addOnSuccessListener(unused ->
                                                    mostrarMensaje("Monto actualizado correctamente"))
                                            .addOnFailureListener(e ->
                                                    mostrarError("Error", "No se pudo actualizar el monto"));
                                } else {
                                    mostrarError("Error", "Ingresa un valor válido.");
                                }
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                })
                .addOnFailureListener(e -> mostrarError("Error", "No se pudo obtener el monto actual."));
    }



    private void confirmarEliminar(Servicio servicio, String hotelId) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Deseas eliminar el servicio '" + servicio.getNombre() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    servicioViewModel.eliminar(hotelId, servicio.getId(), () -> mostrarMensaje("Servicio eliminado."));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarError(String titulo, String mensaje) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}
