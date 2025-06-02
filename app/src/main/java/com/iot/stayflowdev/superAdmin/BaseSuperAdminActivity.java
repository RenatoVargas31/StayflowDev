package com.iot.stayflowdev.superAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.iot.stayflowdev.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseSuperAdminActivity extends AppCompatActivity {

    protected MaterialToolbar topAppBar;
    protected BottomNavigationView bottomNavigation;
    protected FrameLayout contentFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cargar el layout base
        setContentView(R.layout.superadmin_base_superadmin_activity);

        // Inicializar los menús
        initMenus();
        setupMenuListeners();

        // Inflar el contenido específico de la activity
        contentFrame = findViewById(R.id.content_frame);
        View contentView = LayoutInflater.from(this).inflate(getLayoutResourceId(), contentFrame, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar el ítem seleccionado cada vez que la activity se reanuda
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(getBottomNavigationSelectedItem());
        }
    }

    // Método abstracto que cada Activity hija debe implementar
    protected abstract int getLayoutResourceId();

    // Método opcional para que las Activities hijas sepan qué item del bottom nav marcar
    protected abstract int getBottomNavigationSelectedItem();

    private void initMenus() {
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        if (topAppBar != null) {
            setSupportActionBar(topAppBar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getToolbarTitle());
            }
        }

        if (bottomNavigation != null) {
            // Marcar el item correspondiente como seleccionado
            bottomNavigation.setSelectedItemId(getBottomNavigationSelectedItem());
        }
    }

    private void setupMenuListeners() {
        // Listener para el menú superior
        if (topAppBar != null) {
            topAppBar.setOnMenuItemClickListener(this::onOptionsItemSelected);
        }

        // Listener para el menú inferior
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                
                // No navegar si ya estás en la misma pantalla
                if (itemId == getBottomNavigationSelectedItem()) {
                    return true;
                }

                // Navegar a la nueva activity
                Intent intent = null;
                if (itemId == R.id.nav_inicio) {
                    intent = new Intent(this, InicioActivity.class);
                } else if (itemId == R.id.nav_gestion) {
                    intent = new Intent(this, GestionActivity.class);
                } else if (itemId == R.id.nav_reportes) {
                    intent = new Intent(this, ReportesActivity.class);
                } else if (itemId == R.id.nav_perfil) {
                    intent = new Intent(this, PerfilActivity.class);
                }

                if (intent != null) {
                    // Evitar acumulación de activities en el stack
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish(); // Cerrar la activity actual
                    return true;
                }

                return false;
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_logs) {
            // Ir a configuración
            Intent intent = new Intent(this, LogsActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.nav_perfil) {
            // Ir a perfil
            Intent intent = new Intent(this, PerfilActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_logout) {
            // Implementar logout
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Método para obtener el título de la toolbar (puede ser sobrescrito)
    protected String getToolbarTitle() {
        return "StayFlow";
    }

    // Método utilitario para cambiar el título desde las Activities hijas
    protected void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}