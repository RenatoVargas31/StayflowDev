package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.AdminInicioActivity;
import com.iot.stayflowdev.adminHotel.HuespedAdminActivity;
import com.iot.stayflowdev.adminHotel.MensajeriaAdminActivity;
import com.iot.stayflowdev.adminHotel.PerfilAdminActivity;
import com.iot.stayflowdev.adminHotel.ReportesAdminActivity;
import com.iot.stayflowdev.databinding.ActivityPerfilAdminBinding;

public class PerfilAdminActivity extends AppCompatActivity {

    private ActivityPerfilAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPerfilAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar Toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Perfil");

        // Configurar menú inferior
        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.setSelectedItemId(R.id.menu_perfil);
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
                // Ya estamos en perfil, no hacemos nada
                return true;
            }
            return false;
        });

        binding.tvNombreCompleto.setText("Juan Perez");
        binding.tvCorreoElectronico.setText("juan.admin@gmail.com");

    }
}
