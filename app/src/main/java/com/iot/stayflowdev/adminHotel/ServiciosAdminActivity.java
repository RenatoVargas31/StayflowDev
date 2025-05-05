package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.adapter.ServicioAdapter;
import com.iot.stayflowdev.adminHotel.model.Servicio;
import com.iot.stayflowdev.databinding.ActivityServiciosAdminBinding;

import java.util.ArrayList;
import java.util.List;

public class ServiciosAdminActivity extends AppCompatActivity {

    private ActivityServiciosAdminBinding binding;
    private ServicioAdapter servicioAdapter;
    private List<Servicio> listaServicios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServiciosAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // BottomNavigationView
        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.setSelectedItemId(R.id.menu_inicio);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_inicio) return true;
            if (id == R.id.menu_reportes) startActivity(new Intent(this, ReportesAdminActivity.class));
            else if (id == R.id.menu_huesped) startActivity(new Intent(this, HuespedAdminActivity.class));
            else if (id == R.id.menu_mensajeria) startActivity(new Intent(this, MensajeriaAdminActivity.class));
            else if (id == R.id.menu_perfil) startActivity(new Intent(this, PerfilAdminActivity.class));
            overridePendingTransition(0, 0);
            return true;
        });

        // RecyclerView
        servicioAdapter = new ServicioAdapter(listaServicios);
        binding.recyclerServicios.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerServicios.setAdapter(servicioAdapter);

        // FAB → abrir formulario
        binding.fabAddService.setOnClickListener(v -> mostrarFormulario());

        // Botón guardar en formulario
        binding.btnSaveService.setOnClickListener(v -> guardarServicio());

        // Botón cancelar formulario
        binding.btnCancel.setOnClickListener(v -> binding.bottomSheetFormulario.setVisibility(View.GONE));

        // RadioButton listeners
        RadioButton radioFree = binding.radioFree;
        RadioButton radioWithPrice = binding.radioWithPrice;

        radioFree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.inputPrice.setEnabled(false);
                binding.inputPrice.setText("");
            }
        });

        radioWithPrice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.inputPrice.setEnabled(true);
            }
        });

        // Inicialmente ocultar formulario
        binding.bottomSheetFormulario.setVisibility(View.GONE);

        binding.fabAddService.setOnClickListener(v -> {
            AgregarServicioBottomSheet.newInstance((nombre, descripcion, precio) -> {
                Servicio nuevoServicio = new Servicio(nombre, 100, precio);
                listaServicios.add(nuevoServicio);
                servicioAdapter.updateData(listaServicios);
            }).show(getSupportFragmentManager(), "AgregarServicio");
        });
    }

    private void mostrarFormulario() {
        binding.bottomSheetFormulario.setVisibility(View.VISIBLE);
    }

    private void guardarServicio() {
        String nombre = binding.inputServiceName.getText().toString().trim();
        String descripcion = binding.inputDescription.getText().toString().trim();
        String precio = binding.inputPrice.getText().toString().trim();
        boolean esGratis = binding.radioFree.isChecked();

        if (nombre.isEmpty() || descripcion.isEmpty() || (!esGratis && precio.isEmpty())) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String precioFinal = esGratis ? "Gratis" : precio;

        Servicio nuevoServicio = new Servicio(nombre, 100, precioFinal); // Aquí puedes ajustar el porcentaje si quieres
        listaServicios.add(nuevoServicio);
        servicioAdapter.updateData(listaServicios);

        // Limpiar campos y cerrar formulario
        binding.inputServiceName.setText("");
        binding.inputDescription.setText("");
        binding.inputPrice.setText("");
        binding.bottomSheetFormulario.setVisibility(View.GONE);
    }
}
