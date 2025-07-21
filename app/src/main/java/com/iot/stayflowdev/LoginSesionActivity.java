package com.iot.stayflowdev;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class LoginSesionActivity extends AppCompatActivity {

    private TextView tvOlvidePassword;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_sesion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        inicializarVistas();

        // Configurar listeners
        configurarListeners();

        // Verificar si es un conductor recién registrado
        verificarConductorRecienRegistrado();
    }

    private void inicializarVistas() {
        tvOlvidePassword = findViewById(R.id.tv_olvide_password);
        btnLogin = findViewById(R.id.btn_login);
    }

    private void configurarListeners() {
        // Configurar click en "Olvidé mi contraseña"
        tvOlvidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirigir a la actividad para recuperar contraseña
                Intent intent = new Intent(LoginSesionActivity.this, LoginIngCorreoActivity.class);

                // Enviar un flag para indicar que es un flujo de recuperación de contraseña
                intent.putExtra("esRecuperacionPassword", true);

                startActivity(intent);
            }
        });

        // Configurar click en el botón de login (puedes añadir aquí la lógica adicional)
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implementar la lógica de inicio de sesión aquí
            }
        });
    }

    private void verificarConductorRecienRegistrado() {
        Intent intent = getIntent();
        boolean esConductorRecienRegistrado = intent.getBooleanExtra("conductorRecienRegistrado", false);
        String mensajeActivacion = intent.getStringExtra("mensajeActivacion");

        if (esConductorRecienRegistrado && mensajeActivacion != null) {
            mostrarDialogoActivacionCuenta(mensajeActivacion);
        }
    }

    private void mostrarDialogoActivacionCuenta(String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle("Cuenta Pendiente de Activación")
                .setMessage(mensaje)
                .setPositiveButton("Entendido", (dialog, which) -> {
                    dialog.dismiss();
                    // Mostrar un toast adicional
                    Toast.makeText(LoginSesionActivity.this,
                            "Podrás iniciar sesión una vez que tu cuenta sea activada",
                            Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }
}
