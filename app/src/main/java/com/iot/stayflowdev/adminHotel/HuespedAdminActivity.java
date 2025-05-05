package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.huesped.CheckoutAdminActivity;
import com.iot.stayflowdev.adminHotel.huesped.ReservasAdminActivity;
import com.iot.stayflowdev.adminHotel.huesped.TaxiAdminActivity;
import com.iot.stayflowdev.databinding.ActivityHuespedAdminBinding;

public class HuespedAdminActivity extends AppCompatActivity {

    private ActivityHuespedAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHuespedAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configura Toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // Configura BottomNavigationView
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

        // Configura click listeners para las tarjetas
        binding.cardReservas.setOnClickListener(v ->
                startActivity(new Intent(this, ReservasAdminActivity.class))
        );

        binding.cardTaxi.setOnClickListener(v ->
                startActivity(new Intent(this, TaxiAdminActivity.class))
        );

        binding.cardCheckout.setOnClickListener(v ->
                startActivity(new Intent(this, CheckoutAdminActivity.class))
        );
    }
}
