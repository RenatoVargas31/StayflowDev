package com.iot.stayflowdev;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginIngCorreoActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private MaterialButton btnMandar;
    private MaterialButton btnVolverLogin;
    private ImageView btnClose;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_ing_correo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inicializar vistas
        inicializarVistas();

        // Configurar listeners
        configurarListeners();
    }

    private void inicializarVistas() {
        etEmail = findViewById(R.id.et_email);
        btnMandar = findViewById(R.id.btn_mandar);
        btnVolverLogin = findViewById(R.id.btn_volver_login);
        btnClose = findViewById(R.id.btn_close);
    }

    private void configurarListeners() {
        // Configurar el botón de cerrar
        btnClose.setOnClickListener(v -> {
            finish(); // Cierra la actividad y vuelve a la anterior
        });

        // Configurar el botón de mandar correo
        btnMandar.setOnClickListener(v -> {
            enviarCorreoRecuperacion();
        });

        // Configurar el botón de volver a iniciar sesión
        btnVolverLogin.setOnClickListener(v -> {
            // Redirigir a LoginCuentaFireBase
            Intent intent = new Intent(LoginIngCorreoActivity.this, LoginCuentaFireBase.class);
            startActivity(intent);
            finish(); // Cerrar esta actividad para evitar acumulación de pantallas
        });
    }

    private void enviarCorreoRecuperacion() {
        String email = etEmail.getText().toString().trim();

        // Validar que el campo de correo no esté vacío
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("El correo electrónico es obligatorio");
            etEmail.requestFocus();
            return;
        }

        // Validar formato de correo básico
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingresa un correo electrónico válido");
            etEmail.requestFocus();
            return;
        }

        // Deshabilitar el botón mientras se procesa la solicitud
        btnMandar.setEnabled(false);
        btnMandar.setText("Enviando...");

        // Enviar correo de recuperación usando Firebase Auth
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    // Rehabilitar el botón
                    btnMandar.setEnabled(true);
                    btnMandar.setText("Mandar correo de verificación");

                    if (task.isSuccessful()) {
                        // Correo enviado exitosamente
                        Toast.makeText(LoginIngCorreoActivity.this,
                                "Se ha enviado un correo de recuperación a: " + email,
                                Toast.LENGTH_LONG).show();

                        Toast.makeText(LoginIngCorreoActivity.this,
                                "Por favor, revisa tu bandeja de entrada y sigue las instrucciones",
                                Toast.LENGTH_SHORT).show();

                        // Limpiar el campo de correo después de enviar
                        etEmail.setText("");

                    } else {
                        // Error al enviar el correo
                        String errorMessage = "Error al enviar el correo de recuperación";

                        // Personalizar mensaje según el tipo de error
                        if (task.getException() != null) {
                            String exceptionMessage = task.getException().getMessage();
                            if (exceptionMessage != null) {
                                if (exceptionMessage.contains("user-not-found") ||
                                    exceptionMessage.contains("There is no user record")) {
                                    errorMessage = "No existe una cuenta registrada con este correo electrónico";
                                } else if (exceptionMessage.contains("invalid-email")) {
                                    errorMessage = "El formato del correo electrónico es inválido";
                                } else if (exceptionMessage.contains("too-many-requests")) {
                                    errorMessage = "Demasiados intentos. Inténtalo más tarde";
                                }
                            }
                        }

                        Toast.makeText(LoginIngCorreoActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
