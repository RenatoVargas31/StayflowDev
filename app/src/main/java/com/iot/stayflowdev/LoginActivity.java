package com.iot.stayflowdev;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private MaterialButton btnIniciarSesion;
    private MaterialButton btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar botones
        inicializarComponentes();

        // Configurar listeners
        configurarListeners();
    }

    private void inicializarComponentes() {
        btnIniciarSesion = findViewById(R.id.btn_iniciar_sesion);
        btnRegistrar = findViewById(R.id.btn_registrar);
    }

    private void configurarListeners() {
        // Botón Iniciar Sesión - Navegar a LoginSesionActivity
        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, LoginSesionActivity.class);
                startActivity(intent);
            }
        });

        // Botón Registrarse - Navegar a LoginRegisterActivity
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, LoginRegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}

