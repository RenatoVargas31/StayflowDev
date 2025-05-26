package com.iot.stayflowdev;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class LoginRegisterActivity extends AppCompatActivity {

    private MaterialButton btnContinuar;
    private MaterialButton btnRegistroTaxista;

    // Campos del formulario
    private TextInputEditText etNombre, etDni, etFechaNacimiento, etCelular;
    private TextInputLayout tilNombre, tilDni, tilFechaNacimiento, tilCelular;

    // Calendario para el selector de fecha
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    // Patrones para validaciones
    private static final Pattern NOMBRE_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]{3,50}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar campos de formulario
        inicializarViews();

        // Configurar formato de fecha
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Configurar listeners
        configurarListeners();

        // Configurar listener del botón continuar
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarFormulario()) {
                    // Navegar a la pantalla de ingreso de correo
                    Intent intent = new Intent(LoginRegisterActivity.this, LoginIngCorreoActivity.class);
                    startActivity(intent);
                }
            }
        });

        // Configurar listener del botón registrarse como taxista
        btnRegistroTaxista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la pantalla de registro de taxista
                Intent intent = new Intent(LoginRegisterActivity.this, LoginDriverRegister.class);
                startActivity(intent);
            }
        });
    }

    private void inicializarViews() {
        // Botones
        btnContinuar = findViewById(R.id.btn_continuar);
        btnRegistroTaxista = findViewById(R.id.btn_registro_taxista);

        // TextInputEditText
        etNombre = findViewById(R.id.et_nombre);
        etDni = findViewById(R.id.et_dni);
        etFechaNacimiento = findViewById(R.id.et_fecha_nacimiento);
        etCelular = findViewById(R.id.et_celular);

        // TextInputLayout
        tilNombre = findViewById(R.id.til_nombre);
        tilDni = findViewById(R.id.til_dni);
        tilFechaNacimiento = findViewById(R.id.til_fecha_nacimiento);
        tilCelular = findViewById(R.id.til_celular);
    }

    private void configurarListeners() {
        // Validación en tiempo real para el nombre
        etNombre.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validarNombre();
            }
        });

        // Validación en tiempo real para el DNI
        etDni.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validarDni();
            }
        });

        // Validación en tiempo real para el celular
        etCelular.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validarCelular();
            }
        });

        // Configurar el selector de fecha
        etFechaNacimiento.setOnClickListener(v -> mostrarSelectorFecha());
    }

    private boolean validarFormulario() {
        boolean isNombreValido = validarNombre();
        boolean isDniValido = validarDni();
        boolean isFechaValida = validarFecha();
        boolean isCelularValido = validarCelular();

        return isNombreValido && isDniValido && isFechaValida && isCelularValido;
    }

    private boolean validarNombre() {
        String nombre = etNombre.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            tilNombre.setError("El nombre es obligatorio");
            return false;
        }

        if (!NOMBRE_PATTERN.matcher(nombre).matches()) {
            tilNombre.setError("Ingrese un nombre válido (solo letras)");
            return false;
        }

        tilNombre.setError(null);
        return true;
    }

    private boolean validarDni() {
        String dni = etDni.getText().toString().trim();

        if (TextUtils.isEmpty(dni)) {
            tilDni.setError("El DNI es obligatorio");
            return false;
        }

        if (dni.length() != 8) {
            tilDni.setError("El DNI debe tener 8 dígitos");
            return false;
        }

        try {
            long dniNum = Long.parseLong(dni);
        } catch (NumberFormatException e) {
            tilDni.setError("El DNI debe contener solo números");
            return false;
        }

        tilDni.setError(null);
        return true;
    }

    private boolean validarFecha() {
        String fecha = etFechaNacimiento.getText().toString().trim();

        if (TextUtils.isEmpty(fecha)) {
            tilFechaNacimiento.setError("La fecha de nacimiento es obligatoria");
            return false;
        }

        try {
            Date fechaNac = dateFormat.parse(fecha);
            Calendar calendarActual = Calendar.getInstance();
            Calendar calendarNac = Calendar.getInstance();
            calendarNac.setTime(fechaNac);

            int edad = calendarActual.get(Calendar.YEAR) - calendarNac.get(Calendar.YEAR);

            if (calendarActual.get(Calendar.DAY_OF_YEAR) < calendarNac.get(Calendar.DAY_OF_YEAR)) {
                edad--;
            }

            if (edad < 18) {
                tilFechaNacimiento.setError("Debes ser mayor de 18 años");
                return false;
            }

            if (edad > 100) {
                tilFechaNacimiento.setError("Ingresa una fecha válida");
                return false;
            }
        } catch (Exception e) {
            tilFechaNacimiento.setError("Formato de fecha inválido");
            return false;
        }

        tilFechaNacimiento.setError(null);
        return true;
    }

    private boolean validarCelular() {
        String celular = etCelular.getText().toString().trim();

        if (TextUtils.isEmpty(celular)) {
            tilCelular.setError("El número de celular es obligatorio");
            return false;
        }

        if (celular.length() != 9) {
            tilCelular.setError("El número debe tener 9 dígitos");
            return false;
        }

        if (!celular.startsWith("9")) {
            tilCelular.setError("El número debe empezar con 9");
            return false;
        }

        tilCelular.setError(null);
        return true;
    }

    private void mostrarSelectorFecha() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        etFechaNacimiento.setText(dateFormat.format(calendar.getTime()));
                        validarFecha();
                    }
                },
                year - 18,  // Por defecto, ajustamos a que muestre 18 años atrás
                month,
                day
        );

        // Establecer fecha máxima (hoy)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Establecer fecha mínima (100 años atrás)
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        datePickerDialog.show();
    }
}
