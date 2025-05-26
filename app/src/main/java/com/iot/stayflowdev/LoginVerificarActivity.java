package com.iot.stayflowdev;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class LoginVerificarActivity extends AppCompatActivity {

    private MaterialButton btnVerificar;
    private EditText digit1, digit2, digit3, digit4, digit5, digit6;
    private EditText[] digitFields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_verificar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar campos de entrada de código
        initializeViews();

        // Configurar comportamiento dinámico de los campos
        setupDigitFields();

        // Configurar listener del botón verificar
        btnVerificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateCode()) {
                    // Navegar a la pantalla de crear contraseña
                    Intent intent = new Intent(LoginVerificarActivity.this, LoginCrearPassActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void initializeViews() {
        // Botones
        btnVerificar = findViewById(R.id.btn_verificar);

        // Campos de dígitos
        digit1 = findViewById(R.id.digit1);
        digit2 = findViewById(R.id.digit2);
        digit3 = findViewById(R.id.digit3);
        digit4 = findViewById(R.id.digit4);
        digit5 = findViewById(R.id.digit5);
        digit6 = findViewById(R.id.digit6);

        // Array para manejar los campos fácilmente
        digitFields = new EditText[]{digit1, digit2, digit3, digit4, digit5, digit6};
    }

    private void setupDigitFields() {
        // Asignar el foco al primer campo cuando se inicia la actividad
        digit1.requestFocus();

        // Configurar TextWatcher para cada campo de dígito
        for (int i = 0; i < digitFields.length; i++) {
            final int currentIndex = i;

            digitFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // No necesario para esta implementación
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // No necesario para esta implementación
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Si se ingresó un dígito, mover al siguiente campo
                    if (s.length() == 1) {
                        // Si no es el último campo, mover al siguiente
                        if (currentIndex < digitFields.length - 1) {
                            digitFields[currentIndex + 1].requestFocus();
                        } else {
                            // Si es el último campo, ocultar el teclado
                            digitFields[currentIndex].clearFocus();
                        }
                    }
                }
            });

            // Manejar el botón de borrar/retroceso para navegación inversa
            digitFields[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    // Si el campo actual está vacío y no es el primer campo, ir al campo anterior
                    if (digitFields[currentIndex].getText().toString().isEmpty() && currentIndex > 0) {
                        digitFields[currentIndex - 1].requestFocus();
                        // Opcional: borrar el contenido del campo anterior
                        digitFields[currentIndex - 1].setText("");
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private boolean validateCode() {
        // Verificar que todos los campos tengan un dígito
        for (EditText digitField : digitFields) {
            if (digitField.getText().toString().isEmpty()) {
                // Mostrar mensaje de error o indicación visual
                digitField.setError("");
                return false;
            }
        }

        // Aquí podría agregarse la validación del código con el backend
        return true;
    }
}
