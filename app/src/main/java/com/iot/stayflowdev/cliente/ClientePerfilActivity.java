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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.LoginFireBaseActivity;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.databinding.ActivityClientePerfilBinding;
import com.iot.stayflowdev.utils.UserSessionManager;

public class ClientePerfilActivity extends AppCompatActivity {

    private ActivityClientePerfilBinding binding;
    private static final String TAG = "ClientePerfilActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Inicializar ViewBinding
        binding = ActivityClientePerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar toolbar
        setSupportActionBar(binding.toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // Sin padding inferior
            return insets;
        });

        // Configurar navegación inferior
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // Establecer Perfil como seleccionado
        binding.bottomNavigation.setSelectedItemId(R.id.nav_perfil);

        // Configurar listener de navegación
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Si ya estamos en esta actividad, no hacer nada
            if (itemId == R.id.nav_perfil) {
                return true;
            }

            // Navegación según el ítem seleccionado
            if (itemId == R.id.nav_buscar) {
                navigateToActivity(ClienteBuscarActivity.class);
                return true;
            } else if (itemId == R.id.nav_favoritos) {
                navigateToActivity(ClienteFavoritosActivity.class);
                return true;
            } else if (itemId == R.id.nav_reservas) {
                navigateToActivity(ClienteReservasActivity.class);
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
        // 1. Obtener el ID del usuario actual antes de cerrar sesión
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserEmail = null;
        if (auth.getCurrentUser() != null) {
            currentUserEmail = auth.getCurrentUser().getEmail();
        }

        // 2. Marcar usuario como desconectado en Firestore
        if (currentUserEmail != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("usuarios")
                    .whereEqualTo("correo", currentUserEmail)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String userId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            UserSessionManager.getInstance().setUserDisconnected(userId);
                            Log.d(TAG, "Usuario " + userId + " marcado como desconectado");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error al marcar usuario como desconectado: " + e.getMessage());
                    });
        }

        // 3. Cerrar sesión en Firebase
        auth.signOut();

        // 4. Limpiar SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        // 5. Mostrar mensaje
        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

        // 6. Redirigir al login
        Intent intent = new Intent(this, LoginFireBaseActivity.class);
        // Flags para limpiar la pila de actividades
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Al presionar atrás, regresar a la pantalla principal
        navigateToActivity(ClienteBuscarActivity.class);
    }

    @Override
    public boolean onSupportNavigateUp() {
        navigateToActivity(ClienteBuscarActivity.class);
        return true;
    }
}