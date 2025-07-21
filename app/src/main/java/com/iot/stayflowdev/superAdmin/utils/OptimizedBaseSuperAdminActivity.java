package com.iot.stayflowdev.superAdmin.utils;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.superAdmin.ActiveUsersActivity;
import com.iot.stayflowdev.superAdmin.GestionActivity;
import com.iot.stayflowdev.superAdmin.InicioActivity;
import com.iot.stayflowdev.superAdmin.PerfilActivity;
import com.iot.stayflowdev.superAdmin.ReportesActivity;
import com.iot.stayflowdev.utils.ImageLoadingUtils;
import com.iot.stayflowdev.utils.NavigationThrottleManager;

public abstract class OptimizedBaseSuperAdminActivity extends AppCompatActivity {
    private static final String TAG = "OptimizedBaseSuperAdmin";
    private NavigationThrottleManager navigationManager;
    private Handler mainHandler;
    private boolean isNavigating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar managers
        navigationManager = NavigationThrottleManager.getInstance();
        mainHandler = new Handler(Looper.getMainLooper());

        Log.d(TAG, "Actividad optimizada iniciada: " + getClass().getSimpleName());
    }

    protected void setupOptimizedBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        if (bottomNavigation == null) return;

        String currentActivityKey = getClass().getSimpleName();

        bottomNavigation.setOnItemSelectedListener(item -> {
            // Verificar si ya está navegando
            if (isNavigating) {
                Log.d(TAG, "Navegación ya en progreso, ignorando");
                return false;
            }

            // Verificar throttling
            if (!navigationManager.canNavigate(currentActivityKey)) {
                long remainingTime = navigationManager.getRemainingThrottleTime(currentActivityKey);
                Toast.makeText(this, "Por favor espera " + (remainingTime / 100) / 10.0 + " segundos",
                             Toast.LENGTH_SHORT).show();
                return false;
            }

            Intent intent = getNavigationIntent(item.getItemId());
            if (intent != null && !intent.getComponent().getClassName().equals(getClass().getName())) {
                navigateWithDelay(intent);
                return true;
            }
            return false;
        });

        // Establecer el item seleccionado
        int selectedItem = getBottomNavigationSelectedItem();
        if (selectedItem != 0) {
            bottomNavigation.setSelectedItemId(selectedItem);
        }
    }

    private Intent getNavigationIntent(int itemId) {
        if (itemId == R.id.nav_inicio) {
            return new Intent(this, InicioActivity.class);
        } else if (itemId == R.id.nav_reportes) {
            return new Intent(this, ReportesActivity.class);
        } else if (itemId == R.id.nav_gestion) {
            return new Intent(this, GestionActivity.class);
        } else if (itemId == R.id.nav_perfil) {
            return new Intent(this, PerfilActivity.class);
        }
        return null;
    }

    private void navigateWithDelay(Intent intent) {
        isNavigating = true;

        // Cancelar cargas de imágenes pendientes antes de navegar
        ImageLoadingUtils.cancelPendingLoads(this);

        // Pequeño delay para permitir que se cancelen las operaciones
        mainHandler.postDelayed(() -> {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                // Delay adicional antes de finalizar la actividad actual
                mainHandler.postDelayed(this::finish, 100);

            } catch (Exception e) {
                Log.e(TAG, "Error en navegación", e);
                isNavigating = false;
            }
        }, 150); // 150ms delay
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Cancelar cargas de imágenes cuando la actividad se pausa
        ImageLoadingUtils.cancelPendingLoads(this);
        Log.d(TAG, "Actividad pausada, cargas de imagen canceladas");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isNavigating = false;

        // Limpiar handlers pendientes
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }

        Log.d(TAG, "Actividad destruida: " + getClass().getSimpleName());
    }

    // Métodos abstractos que deben implementar las actividades hijas
    protected abstract int getBottomNavigationSelectedItem();

    // Método para cargar imágenes de manera optimizada
    protected void loadImageSafely(String imageUrl, android.widget.ImageView imageView, int placeholderRes) {
        ImageLoadingUtils.loadImageSafely(this, imageUrl, imageView, placeholderRes, new ImageLoadingUtils.ImageLoadCallback() {
            @Override
            public void onLoadFailed() {
                Log.d(TAG, "Imagen falló al cargar: " + imageUrl);
            }

            @Override
            public void onLoadSuccess() {
                Log.d(TAG, "Imagen cargada exitosamente");
            }
        });
    }

    // Método para cargar imágenes de perfil
    protected void loadProfileImageSafely(String imageUrl, android.widget.ImageView imageView) {
        ImageLoadingUtils.loadProfileImage(this, imageUrl, imageView, new ImageLoadingUtils.ImageLoadCallback() {
            @Override
            public void onLoadFailed() {
                Log.d(TAG, "Imagen de perfil falló al cargar: " + imageUrl);
            }

            @Override
            public void onLoadSuccess() {
                Log.d(TAG, "Imagen de perfil cargada exitosamente");
            }
        });
    }

    // Método para ejecutar tareas en el hilo principal de manera segura
    protected void runOnUiThreadSafely(Runnable runnable) {
        if (mainHandler != null && !isFinishing() && !isDestroyed()) {
            mainHandler.post(runnable);
        }
    }

    // Método para ejecutar tareas con delay de manera segura
    protected void runOnUiThreadWithDelay(Runnable runnable, long delayMs) {
        if (mainHandler != null && !isFinishing() && !isDestroyed()) {
            mainHandler.postDelayed(runnable, delayMs);
        }
    }
}
