package com.iot.stayflowdev.cliente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.iot.stayflowdev.LoginFireBaseActivity;
import com.iot.stayflowdev.R;

public class ClientePerfilActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private static final String TAG = "ClientePerfilActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cliente_perfil);
        setSupportActionBar(findViewById(R.id.toolbar)); // Configurar toolbar

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // No padding en el bottom
            return insets;
        });

        // Inicializar todas las vistas
        inicializarVistas();

        // Configurar navegación inferior
        configurarBottomNavigationFluido();
    }

    // Inicializar vistas
    private void inicializarVistas() {
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Verificar que se encontró
        if (bottomNavigation == null) {
            Log.e(TAG, "❌ ERROR: bottomNavigation es null!");
        } else {
            Log.d(TAG, "✅ bottomNavigation encontrado correctamente");
        }
    }

    // Configuración de la navegación inferior
    private void configurarBottomNavigationFluido() {
        if (bottomNavigation == null) {
            Log.e(TAG, "❌ bottomNavigation es null, no se puede configurar");
            return;
        }

        // Verificar que el menú se cargó
        if (bottomNavigation.getMenu() == null || bottomNavigation.getMenu().size() == 0) {
            Log.e(TAG, "❌ ERROR: El menú no se cargó correctamente");
            return;
        }

        // Establecer perfil como seleccionado
        bottomNavigation.setSelectedItemId(R.id.nav_perfil);
        Log.d(TAG, "✅ Intentando seleccionar nav_perfil");

        // Forzar selección con post para asegurar que se aplique
        bottomNavigation.post(() -> {
            bottomNavigation.setSelectedItemId(R.id.nav_perfil);
            Log.d(TAG, "✅ Selección aplicada con post()");
        });

        // Configurar listener con navegación sin animación
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "🔄 Ítem seleccionado: " + itemId);

            // Evitar acción si ya estamos en la actividad actual
            if (itemId == R.id.nav_perfil) {
                Log.d(TAG, "✅ Ya en Perfil");
                return true;
            }

            // Navegación según el ítem seleccionado
            if (itemId == R.id.nav_buscar) {
                Log.d(TAG, "🏠 Navegando a Inicio");
                navegarSinAnimacion(ClienteBuscarActivity.class);
                return true;
            }

            return false;
        });
    }

    // Método para navegar sin animación
    private void navegarSinAnimacion(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    // Manejar botón de retroceso correctamente
    @Override
    public void onBackPressed() {
        // Redirigir a MainActivity cuando se presiona el botón atrás
        super.onBackPressed();
        navegarSinAnimacion(ClienteBuscarActivity.class);
    }

    // Verificar selección en onResume
    @Override
    protected void onResume() {
        super.onResume();
        // Asegurar que el ítem correcto esté seleccionado cuando se regresa a la actividad
        if (bottomNavigation != null) {
            bottomNavigation.post(() -> {
                bottomNavigation.setSelectedItemId(R.id.nav_perfil);
                Log.d(TAG, "✅ Selección verificada en onResume()");
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        navegarSinAnimacion(ClienteBuscarActivity.class);
        return true;
    }

    // Inflar el menú
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    // Manejar eventos del menú
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            Log.d(TAG, "Botón de logout presionado");
            cerrarSesion();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Método para cerrar sesión
    private void cerrarSesion() {
        // 1. Cerrar sesión en Firebase
        FirebaseAuth.getInstance().signOut();

        // 2. Limpiar SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        // 3. Mostrar mensaje
        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

        // 4. Redirigir al login
        Intent intent = new Intent(this, LoginFireBaseActivity.class);
        // Flags para limpiar la pila de actividades
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}