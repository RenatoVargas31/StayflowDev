package com.iot.stayflowdev.adminHotel.huesped;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.AdminInicioActivity;
import com.iot.stayflowdev.adminHotel.HuespedAdminActivity;
import com.iot.stayflowdev.adminHotel.PerfilAdminActivity;
import com.iot.stayflowdev.adminHotel.MensajeriaAdminActivity;

import com.iot.stayflowdev.adminHotel.ReportesAdminActivity;
import com.iot.stayflowdev.adminHotel.adapter.CheckoutAdapter;
import com.iot.stayflowdev.adminHotel.model.Checkout;
import com.iot.stayflowdev.databinding.ActivityCheckoutAdminBinding;

import java.util.ArrayList;
import java.util.List;

public class CheckoutAdminActivity extends AppCompatActivity {

    private ActivityCheckoutAdminBinding binding;
    private List<Checkout> checkouts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Checkout");

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

        cargarCheckouts();

        CheckoutAdapter adapter = new CheckoutAdapter(checkouts, this);
        binding.recyclerCheckout.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerCheckout.setAdapter(adapter);
    }

    private void cargarCheckouts() {
        checkouts.add(new Checkout("Ana Bonino", "AFR456", "Hospedaje: S/.1000", "Tarjeta: ••••1234", "Daños: S/.100", "Total: S/.1100", R.drawable.img_guest_placeholder));
        checkouts.add(new Checkout("Luis Ramírez", "LR789", "Hospedaje: S/.950", "Tarjeta: ••••5678", "Daños: S/.50", "Total: S/.1000", R.drawable.img_guest_placeholder));
        checkouts.add(new Checkout("María López", "ML012", "Hospedaje: S/.800", "Tarjeta: ••••9876", "Daños: S/.0", "Total: S/.800", R.drawable.img_guest_placeholder));

    }
}
