package com.iot.stayflowdev.superAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
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
                    // Eliminar la transición de animación
                    overridePendingTransition(0, 0);
                    finish(); // Cerrar la activity actual
                    return true;
                }

                return false;
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.superadmin_up_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logs) {
            startActivity(new Intent(this, LogsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Si estamos en la pantalla de inicio, minimizar la app
        if (this instanceof InicioActivity) {
            moveTaskToBack(true);
        } else if (this instanceof UserDetailActivity) {
            // Si estamos en detalles de usuario, volver a gestión
            Intent intent = new Intent(this, GestionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (this instanceof MessagingTestActivity) {
            // Si estamos en el chat, volver a la lista de usuarios
            Intent intent = new Intent(this, SelectUserForChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (this instanceof SelectUserForChatActivity) {
            // Si estamos en la lista de usuarios para chat, volver a inicio
            Intent intent = new Intent(this, InicioActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else {
            // Si no estamos en inicio ni en detalles, volver a la pantalla de inicio
            Intent intent = new Intent(this, InicioActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
        super.onBackPressed();
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