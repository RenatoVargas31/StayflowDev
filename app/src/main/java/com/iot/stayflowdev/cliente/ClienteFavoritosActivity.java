package com.iot.stayflowdev.cliente;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.databinding.ActivityClienteFavoritosBinding;

public class ClienteFavoritosActivity extends AppCompatActivity {

    private ActivityClienteFavoritosBinding binding;
    private static final String TAG = "ClienteFavoritosActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Inicializar ViewBinding
        binding = ActivityClienteFavoritosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // Sin padding inferior
            return insets;
        });

        // Configurar navegación inferior
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // Establecer Favoritos como seleccionado
        //binding.bottomNavigation.setSelectedItemId(R.id.nav_favoritos);

        // Configurar listener de navegación
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Si ya estamos en esta actividad, no hacer nada
            /*
            if (itemId == R.id.nav_favoritos) {
                return true;
            }
            */
            // Navegación según el ítem seleccionado
            if (itemId == R.id.nav_buscar) {
                navigateToActivity(ClienteBuscarActivity.class);
                return true;
            } else if (itemId == R.id.nav_reservas) {
                navigateToActivity(ClienteReservasActivity.class);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                navigateToActivity(ClientePerfilActivity.class);
                return true;
            }

            return false;
        });
    }

    // Método para navegar sin animación
    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        overridePendingTransition(0, 0); // Sin animación
        finish();
    }

    @Override
    public void onBackPressed() {
        // Al presionar atrás, regresar a la pantalla principal
        navigateToActivity(ClienteBuscarActivity.class);
    }
}