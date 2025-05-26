package com.iot.stayflowdev;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginCrearPassActivity extends AppCompatActivity {

    private MaterialButton btnFinalizar;
    private TextInputEditText etPassword, etRepeatPassword;
    private TextInputLayout tilPassword, tilRepeatPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_crear_pass);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        inicializarViews();

        // Configurar validaciones en tiempo real
        configurarValidaciones();

        // Configurar listener del botón finalizar/continuar
        btnFinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarContrasenas()) {
                    // Navegar a la pantalla de cargar foto
                    Intent intent = new Intent(LoginCrearPassActivity.this, LoginCargarFotoActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void inicializarViews() {
        // Botones
        btnFinalizar = findViewById(R.id.btn_finalizar);

        // Campos de contraseña
        etPassword = findViewById(R.id.et_password);
        etRepeatPassword = findViewById(R.id.et_repeat_password);

        // TextInputLayouts
        tilPassword = findViewById(R.id.til_password);
        tilRepeatPassword = findViewById(R.id.til_repeat_password);
    }

    private void configurarValidaciones() {
        // Validar en tiempo real cuando se modifica la contraseña repetida
        etRepeatPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No es necesario implementar
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No es necesario implementar
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Validar si las contraseñas coinciden mientras el usuario escribe
                String password = etPassword.getText().toString();
                String repeatPassword = s.toString();

                if (password.length() > 0 && repeatPassword.length() > 0) {
                    if (!password.equals(repeatPassword)) {
                        tilRepeatPassword.setError("Las contraseñas no coinciden");
                    } else {
                        tilRepeatPassword.setError(null);
                    }
                }
            }
        });

        // También validar si la contraseña original cambia
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No es necesario implementar
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No es necesario implementar
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Si la contraseña original cambia y hay texto en la confirmación,
                // verificar si aún coinciden
                String repeatPassword = etRepeatPassword.getText().toString();
                if (!repeatPassword.isEmpty()) {
                    if (!s.toString().equals(repeatPassword)) {
                        tilRepeatPassword.setError("Las contraseñas no coinciden");
                    } else {
                        tilRepeatPassword.setError(null);
                    }
                }
            }
        });
    }

    private boolean validarContrasenas() {
        String password = etPassword.getText().toString();
        String repeatPassword = etRepeatPassword.getText().toString();

        // Validar que las contraseñas no estén vacías
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Debes ingresar una contraseña");
            return false;
        }

        // Validar longitud mínima de la contraseña
        if (password.length() < 6) {
            tilPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return false;
        }

        // Limpiar error si la contraseña es válida
        tilPassword.setError(null);

        // Validar que se haya ingresado la confirmación
        if (TextUtils.isEmpty(repeatPassword)) {
            tilRepeatPassword.setError("Debes confirmar tu contraseña");
            return false;
        }

        // Validar que las contraseñas coincidan
        if (!password.equals(repeatPassword)) {
            tilRepeatPassword.setError("Las contraseñas no coinciden");
            Toast.makeText(this, "Las contraseñas deben ser idénticas", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Todo correcto
        tilRepeatPassword.setError(null);
        return true;
    }
}
