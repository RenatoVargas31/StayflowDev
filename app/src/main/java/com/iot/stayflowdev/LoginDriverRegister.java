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

public class LoginDriverRegister extends AppCompatActivity {

    private MaterialButton btnContinuarRegistroDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_driver_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar botones
        btnContinuarRegistroDriver = findViewById(R.id.btn_continuar_registro_driver);

        // Configurar listener del botón continuar
        btnContinuarRegistroDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la pantalla de ingreso de correo
                Intent intent = new Intent(LoginDriverRegister.this, LoginIngCorreoActivity.class);
                startActivity(intent);
            }
        });
    }
}

