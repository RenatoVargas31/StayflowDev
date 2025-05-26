package com.iot.stayflowdev;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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

import java.util.regex.Pattern;

public class LoginIngCorreoActivity extends AppCompatActivity {

    private MaterialButton btnMandarCodigo;
    private TextInputEditText etEmail;
    private TextInputLayout tilEmail;

    // Patrón para validar correos con dominios comunes
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+(\\.[a-z]+)*"
    );

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

        // Inicializar vistas
        btnMandarCodigo = findViewById(R.id.btn_mandar);
        etEmail = findViewById(R.id.et_email);

        // Obtener la referencia al TextInputLayout (para mostrar errores)
        tilEmail = (TextInputLayout) etEmail.getParent().getParent();

        // Configurar listener del botón mandar código
        btnMandarCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarEmail()) {
                    // Navegar a la pantalla de verificación
                    Intent intent = new Intent(LoginIngCorreoActivity.this, LoginVerificarActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Valida que el correo electrónico tenga un formato válido
     * @return true si el formato es válido, false en caso contrario
     */
    private boolean validarEmail() {
        String email = etEmail.getText().toString().trim();

        // Validar que el campo no esté vacío
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("El correo electrónico es obligatorio");
            return false;
        }

        // Validar formato de correo usando el patrón
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            tilEmail.setError("Ingrese un correo electrónico válido (ej: ejemplo@gmail.com)");
            return false;
        }

        // Validar dominios comunes
        if (!email.contains("@gmail.com") && !email.contains("@hotmail.com") &&
            !email.contains("@outlook.com") && !email.contains("@yahoo.com")) {
            tilEmail.setError("Por favor ingrese un correo con dominio válido (gmail.com, hotmail.com, etc.)");
            return false;
        }

        // Si pasa todas las validaciones, limpiamos el error si hubiera
        tilEmail.setError(null);
        return true;
    }
}
