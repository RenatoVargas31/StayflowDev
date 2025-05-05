package com.iot.stayflowdev;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract int getLayoutResourceId();
    protected abstract int getCurrentMenuItemId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_base);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Inflar el layout específico de la actividad
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(getLayoutResourceId(), contentFrame, true);

        //Configurar el BottomNavigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(getCurrentMenuItemId());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // No realizar acción si estamos en la misma actividad
            if (id == getCurrentMenuItemId()) {
                return true;
            }

            // Navegación según el ítem seleccionado
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_reservas) {
                startActivity(new Intent(this, DriverReservaActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_mapa) {
                startActivity(new Intent(this, DriverMapaActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, DriverPerfilActivity.class));
                finish();
                return true;
            }

            return false;
        });

    }
}