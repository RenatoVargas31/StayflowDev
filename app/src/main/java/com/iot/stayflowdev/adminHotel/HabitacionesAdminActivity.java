package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.adapter.HabitacionAdapter;
import com.iot.stayflowdev.adminHotel.model.Habitacion;
import com.iot.stayflowdev.databinding.ActivityHabitacionesAdminBinding;

import java.util.ArrayList;
import java.util.List;

public class HabitacionesAdminActivity extends AppCompatActivity {

    private ActivityHabitacionesAdminBinding binding;
    private HabitacionAdapter habitacionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHabitacionesAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // BottomNavigationView
        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.setSelectedItemId(R.id.menu_inicio);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_inicio) {
                return true;
            } else if (id == R.id.menu_reportes) {
                startActivity(new Intent(this, ReportesAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_huesped) {
                startActivity(new Intent(this, HuespedAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_mensajeria) {
                startActivity(new Intent(this, MensajeriaAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_perfil) {
                startActivity(new Intent(this, PerfilAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        // RecyclerView
        binding.recyclerHabitaciones.setLayoutManager(new LinearLayoutManager(this));
        habitacionAdapter = new HabitacionAdapter(new ArrayList<>());
        binding.recyclerHabitaciones.setAdapter(habitacionAdapter);

        actualizarVisibilidad();

        // FAB y botón emptyState → abrir formulario
        binding.fabAddRoom.setOnClickListener(v -> mostrarFormularioAgregarHabitacion());
    }

    private void mostrarFormularioAgregarHabitacion() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_habitacion, null);
        TextInputEditText etTipo = view.findViewById(R.id.etTipo);
        TextInputEditText etCapacidad = view.findViewById(R.id.etCapacidad);
        TextInputEditText etTamano = view.findViewById(R.id.etTamano);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Agregar habitación")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String tipo = etTipo.getText().toString().trim();
                    String capacidadStr = etCapacidad.getText().toString().trim();
                    String tamanoStr = etTamano.getText().toString().trim();

                    if (!tipo.isEmpty() && !capacidadStr.isEmpty() && !tamanoStr.isEmpty()) {
                        int capacidad = Integer.parseInt(capacidadStr);
                        int tamano = Integer.parseInt(tamanoStr);

                        Habitacion habitacion = new Habitacion(tipo, capacidad, tamano);
                        habitacionAdapter.addHabitacion(habitacion);

                        actualizarVisibilidad();
                    } else {
                        Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarVisibilidad() {
        if (habitacionAdapter.getItemCount() > 0) {
            binding.recyclerHabitaciones.setVisibility(View.VISIBLE);
            binding.emptyState.setVisibility(View.GONE);
        } else {
            binding.recyclerHabitaciones.setVisibility(View.GONE);
            binding.emptyState.setVisibility(View.VISIBLE);
        }
    }
}
