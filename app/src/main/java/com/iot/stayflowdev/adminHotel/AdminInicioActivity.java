package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.iot.stayflowdev.R;

public class AdminInicioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_inicio);

        // Configura Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configura BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
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

        // Configura las tarjetas
        setupCard(R.id.includeBtnUbicacion, R.drawable.ic_map, "Ubicación", UbicacionAdminActivity.class);
        setupCard(R.id.includeBtnGaleria, R.drawable.ic_gallery, "Galería", GaleriaAdminActivity.class);
        setupCard(R.id.includeBtnHabitaciones, R.drawable.ic_rooms, "Habitaciones", HabitacionesAdminActivity.class);
        setupCard(R.id.includeBtnServicios, R.drawable.ic_services, "Servicios", ServiciosAdminActivity.class);
    }

    private void setupCard(int includeId, int iconResId, String text, Class<?> targetActivity) {
        View includeView = findViewById(includeId);
        ImageView icon = includeView.findViewById(R.id.icono);
        TextView label = includeView.findViewById(R.id.texto);
        MaterialCardView card = (MaterialCardView) includeView;

        icon.setImageResource(iconResId);
        label.setText(text);

        card.setOnClickListener(v -> startActivity(new Intent(this, targetActivity)));
    }
}
