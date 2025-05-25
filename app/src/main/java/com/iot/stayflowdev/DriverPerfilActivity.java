package com.iot.stayflowdev;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DriverPerfilActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_perfil);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // ✅ PRIMERO: INICIALIZAR TODAS LAS VISTAS
        inicializarVistas();

        // ✅ SEGUNDO: CONFIGURAR NAVEGACIÓN
        configurarBottomNavigationFluido();

        // ✅ TERCERO: CONFIGURAR OPCIONES DEL PERFIL
        configurarOpcionesPerfil();
    }

    // ✅ MÉTODO CRÍTICO QUE FALTABA: INICIALIZAR VISTAS
    private void inicializarVistas() {
        bottomNavigation = findViewById(R.id.bottomNavigation); // ✅ ESTO FALTABA!

        // ✅ VERIFICAR QUE SE ENCONTRÓ
        if (bottomNavigation == null) {
            Log.e("DriverPerfilActivity", "❌ ERROR: bottomNavigation es null!");
        } else {
            Log.d("DriverPerfilActivity", "✅ bottomNavigation encontrado correctamente");
        }
    }

    // ✅ CONFIGURACIÓN CON DEBUG
    private void configurarBottomNavigationFluido() {
        if (bottomNavigation == null) {
            Log.e("DriverPerfilActivity", "❌ bottomNavigation es null, no se puede configurar");
            return;
        }

        // ✅ DEBUG: Verificar que el menú se cargó
        if (bottomNavigation.getMenu() == null || bottomNavigation.getMenu().size() == 0) {
            Log.e("DriverPerfilActivity", "❌ ERROR: El menú no se cargó correctamente");
            return;
        }

        // ✅ ESTABLECER PERFIL COMO SELECCIONADO
        bottomNavigation.setSelectedItemId(R.id.nav_perfil);
        Log.d("DriverPerfilActivity", "✅ Intentando seleccionar nav_perfil");

        // ✅ FORZAR SELECCIÓN CON POST PARA ASEGURAR QUE SE APLIQUE
        bottomNavigation.post(() -> {
            bottomNavigation.setSelectedItemId(R.id.nav_perfil);
            Log.d("DriverPerfilActivity", "✅ Selección aplicada con post()");
        });

        // Configurar listener con navegación sin animación
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d("DriverPerfilActivity", "🔄 Ítem seleccionado: " + itemId);

            // Evitar acción si ya estamos en la actividad actual
            if (itemId == R.id.nav_perfil) {
                Log.d("DriverPerfilActivity", "✅ Ya en Perfil");
                return true;
            }

            // Navegación según el ítem seleccionado
            if (itemId == R.id.nav_inicio) {
                Log.d("DriverPerfilActivity", "🏠 Navegando a Inicio");
                navegarSinAnimacion(MainActivity.class);
                return true;
            } else if (itemId == R.id.nav_reservas) {
                Log.d("DriverPerfilActivity", "📋 Navegando a Reservas");
                navegarSinAnimacion(DriverReservaActivity.class);
                return true;
            } else if (itemId == R.id.nav_mapa) {
                Log.d("DriverPerfilActivity", "🗺️ Navegando a Mapa");
                navegarSinAnimacion(DriverMapaActivity.class);
                return true;
            }

            return false;
        });
    }

    // ✅ CONFIGURAR OPCIONES DEL PERFIL
    private void configurarOpcionesPerfil() {
        // Tarjeta de crédito
        ConstraintLayout layoutTarjetaCredit = findViewById(R.id.layout_tarjetaCredit);
        if (layoutTarjetaCredit != null) {
            layoutTarjetaCredit.setOnClickListener(v -> {
                Intent intent = new Intent(DriverPerfilActivity.this, DriverTarjetaCreditoActivity.class);
                startActivity(intent);
                // ✅ APLICAR NAVEGACIÓN SIN ANIMACIÓN
                overridePendingTransition(0, 0);
            });
        } else {
            Log.e("DriverPerfilActivity", "layout_tarjetaCredit no encontrado");
        }

        // Vehículo
        ConstraintLayout layoutVehiculo = findViewById(R.id.layout_vehicle_model);
        if (layoutVehiculo != null) {
            layoutVehiculo.setOnClickListener(v -> {
                Intent intent = new Intent(DriverPerfilActivity.this, DriverVehiculoActivity.class);
                startActivity(intent);
                // ✅ APLICAR NAVEGACIÓN SIN ANIMACIÓN
                overridePendingTransition(0, 0);
            });
        } else {
            Log.e("DriverPerfilActivity", "layout_vehicle_model no encontrado");
        }

        // Correo
        ConstraintLayout layoutCorreo = findViewById(R.id.layout_correo);
        if (layoutCorreo != null) {
            layoutCorreo.setOnClickListener(v -> {
                Intent intent = new Intent(DriverPerfilActivity.this, DriverCorreoActivity.class);
                startActivity(intent);
                // ✅ APLICAR NAVEGACIÓN SIN ANIMACIÓN
                overridePendingTransition(0, 0);
            });
        } else {
            Log.e("DriverPerfilActivity", "layout_correo no encontrado");
        }
    }

    // ✅ MÉTODO PARA NAVEGAR SIN ANIMACIÓN
    private void navegarSinAnimacion(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    // ✅ MANEJAR BOTÓN DE RETROCESO CORRECTAMENTE
    @Override
    public void onBackPressed() {
        // Redirigir a MainActivity cuando se presiona el botón atrás
        super.onBackPressed();
        navegarSinAnimacion(MainActivity.class);
        // ✅ NO LLAMAR super.onBackPressed() después de navegar
    }

    // ✅ VERIFICAR SELECCIÓN EN onResume
    @Override
    protected void onResume() {
        super.onResume();
        // Asegurar que el ítem correcto esté seleccionado cuando se regresa a la actividad
        if (bottomNavigation != null) {
            bottomNavigation.post(() -> {
                bottomNavigation.setSelectedItemId(R.id.nav_perfil);
                Log.d("DriverPerfilActivity", "✅ Selección verificada en onResume()");
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        navegarSinAnimacion(MainActivity.class);
        return true;
    }
}