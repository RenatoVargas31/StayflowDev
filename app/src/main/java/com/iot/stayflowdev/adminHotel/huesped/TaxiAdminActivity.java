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
import com.iot.stayflowdev.adminHotel.adapter.TaxiAdapter;
import com.iot.stayflowdev.adminHotel.model.Taxi;
import com.iot.stayflowdev.databinding.ActivityTaxiAdminBinding;

import java.util.ArrayList;
import java.util.List;

public class TaxiAdminActivity extends AppCompatActivity {

    private ActivityTaxiAdminBinding binding;
    private List<Taxi> taxis = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaxiAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Estado del Taxista");

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
        cargarTaxis();

        // RecyclerView
        TaxiAdapter adapter = new TaxiAdapter(taxis, this);
        binding.recyclerTaxi.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTaxi.setAdapter(adapter);
    }

    private void cargarTaxis() {
        taxis.add(new Taxi("Ana Bonino", "AFR456", "Asignado",
                "23-06-25 02:30 pm", "Aeropuerto → Hotel", R.drawable.img_guest_placeholder));

        taxis.add(new Taxi("Luis Ramírez", "LR789", "En camino",
                "23-06-25 03:00 pm", "Aeropuerto → Hotel", R.drawable.img_guest_placeholder));

        taxis.add(new Taxi("María López", "ML012", "Llegado",
                "23-06-25 04:15 pm", "Aeropuerto → Hotel", R.drawable.img_guest_placeholder));
    }
}
