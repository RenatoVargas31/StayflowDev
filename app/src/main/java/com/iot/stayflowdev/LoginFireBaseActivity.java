package com.iot.stayflowdev;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class LoginFireBaseActivity extends AppCompatActivity {

    private static final String TAG = "LoginFireBase";
    private Button btnEmailLogin, btnGoogleLogin;
    private FirebaseAuth mAuth;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_fire_base);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Error fatal: ", throwable);
            // Puedes guardar el error en un archivo si es necesario
        });
        // Enlazar vistas
        btnEmailLogin = findViewById(R.id.btnEmailLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        // Configurar listeners
        btnEmailLogin.setOnClickListener(view -> iniciarSesionConEmail());
        btnGoogleLogin.setOnClickListener(view -> iniciarSesionConGoogle());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verificar si el usuario ya está autenticado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Si ya está autenticado, redirigir a la actividad principal
            irAPantallaPrincipal();
        }
    }

    private void iniciarSesionConEmail() {
        // Lista de proveedores para iniciar sesión con email
        List<AuthUI.IdpConfig> proveedores = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
        );
        iniciarInterfazDeAutenticacion(proveedores);
    }

    private void iniciarSesionConGoogle() {
        // Lista de proveedores para iniciar sesión con Google
        List<AuthUI.IdpConfig> proveedores = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );
        iniciarInterfazDeAutenticacion(proveedores);
    }

    private void iniciarInterfazDeAutenticacion(List<AuthUI.IdpConfig> proveedores) {
        try {
            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(proveedores)
                    .build();
            signInLauncher.launch(signInIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error al iniciar autenticación: ", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Inicio de sesión exitoso
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Log.d(TAG, "signInWithCredential:success");
            Toast.makeText(this, "Autenticación exitosa", Toast.LENGTH_SHORT).show();
            irAPantallaPrincipal();
        } else {
            // Si el inicio de sesión falla, muestra un mensaje al usuario
            if (response == null) {
                Log.w(TAG, "signIn:canceled");
                Toast.makeText(this, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show();
            } else {
                Log.w(TAG, "signIn:failure", response.getError());
                Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void irAPantallaPrincipal() {
        Log.d(TAG, "Navegando a la pantalla principal");
        // Implementa la navegación a tu Activity principal o muestra un mensaje temporal
        Toast.makeText(this, "¡Usuario autenticado! Redireccionar a la actividad principal", Toast.LENGTH_SHORT).show();
    }
}