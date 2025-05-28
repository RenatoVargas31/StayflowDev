package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.AdminInicioActivity;
import com.iot.stayflowdev.adminHotel.HuespedAdminActivity;
import com.iot.stayflowdev.adminHotel.PerfilAdminActivity;
import com.iot.stayflowdev.adminHotel.ReportesAdminActivity;
import com.iot.stayflowdev.adminHotel.adapter.MensajeAdapter;
import com.iot.stayflowdev.adminHotel.model.Mensaje;
import com.iot.stayflowdev.databinding.ActivityMensajeriaAdminBinding;

import java.util.ArrayList;
import java.util.List;

public class MensajeriaAdminActivity extends AppCompatActivity {

    private ActivityMensajeriaAdminBinding binding;
    private List<Mensaje> mensajes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMensajeriaAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Mensajería");

        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.setSelectedItemId(R.id.menu_mensajeria);
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
                return true;
            } else if (id == R.id.menu_perfil) {
                startActivity(new Intent(this, PerfilAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        cargarMensajes();

        MensajeAdapter adapter = new MensajeAdapter(mensajes, this);
        binding.recyclerMensajes.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerMensajes.setAdapter(adapter);
    }

    private void cargarMensajes() {
        mensajes.add(new Mensaje("Ana Bonino", "¿A qué hora puedo hacer check-in?", R.drawable.img_guest_placeholder));
        mensajes.add(new Mensaje("Luis Ramírez", "¿Dónde se recoge la llave?", R.drawable.img_guest_placeholder));
        mensajes.add(new Mensaje("María López", "¿Tienen estacionamiento disponible?", R.drawable.img_guest_placeholder));
    }
}
