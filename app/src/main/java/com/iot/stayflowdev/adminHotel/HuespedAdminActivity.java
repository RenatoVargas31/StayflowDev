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

        // Guarda una referencia a la actividad actual
        final Class<?> currentActivity = this.getClass();

        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = null;
            int id = item.getItemId();

            if (id == R.id.menu_inicio && currentActivity != AdminInicioActivity.class) {
                intent = new Intent(this, AdminInicioActivity.class);
            } else if (id == R.id.menu_reportes && currentActivity != ReportesAdminActivity.class) {
                intent = new Intent(this, ReportesAdminActivity.class);
            } else if (id == R.id.menu_huesped && currentActivity != HuespedAdminActivity.class) {
                intent = new Intent(this, HuespedAdminActivity.class);
            } else if (id == R.id.menu_mensajeria && currentActivity != MensajeriaAdminActivity.class) {
                intent = new Intent(this, MensajeriaAdminActivity.class);
            } else if (id == R.id.menu_perfil && currentActivity != PerfilAdminActivity.class) {
                intent = new Intent(this, PerfilAdminActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }

            return true;
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
