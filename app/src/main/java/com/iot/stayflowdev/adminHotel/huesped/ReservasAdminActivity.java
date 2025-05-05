package com.iot.stayflowdev.adminHotel.huesped;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.AdminInicioActivity;
import com.iot.stayflowdev.adminHotel.HuespedAdminActivity;
import com.iot.stayflowdev.adminHotel.MensajeriaAdminActivity;
import com.iot.stayflowdev.adminHotel.PerfilAdminActivity;
import com.iot.stayflowdev.adminHotel.ReportesAdminActivity;
import com.iot.stayflowdev.adminHotel.adapter.ReservaAdapter;
import com.iot.stayflowdev.adminHotel.model.Reserva;
import com.iot.stayflowdev.databinding.ActivityReservasAdminBinding;

import java.util.ArrayList;
import java.util.List;

public class ReservasAdminActivity extends AppCompatActivity {

    private ActivityReservasAdminBinding binding;
    private List<Reserva> reservas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReservasAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Reservas");

        // Menú inferior
        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.setSelectedItemId(R.id.menu_huesped);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_inicio) {
                startActivity(new Intent(this, AdminInicioActivity.class));
                overridePendingTransition(0, 0);
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

        // Simulación de datos
        cargarReservas();

        // RecyclerView con adapter actualizado
        ReservaAdapter adapter = new ReservaAdapter(reservas, this);
        binding.recyclerReservas.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerReservas.setAdapter(adapter);
    }

    private void cargarReservas() {
        reservas.add(new Reserva("Alba Doden", "Suite", "2 adultos + 1 niño",
                R.drawable.img_guest_placeholder, "ADER234", "25 Julio 2025", "29 Julio 2025"));

        reservas.add(new Reserva("Juan Pérez", "Doble", "2 adultos",
                R.drawable.img_guest_placeholder, "JP123", "20 Julio 2025", "22 Julio 2025"));

        reservas.add(new Reserva("Ana Gómez", "Simple", "1 adulto",
                R.drawable.img_guest_placeholder, "AG456", "15 Julio 2025", "18 Julio 2025"));

    }
}
