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
