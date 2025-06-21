package com.iot.stayflowdev;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private TextInputEditText etNombres, etApellidos, etNumeroDocumento, etFechaNacimiento, etTelefono, etDomicilio;
    private TextInputLayout tilNombres, tilApellidos, tilNumeroDocumento, tilFechaNacimiento, tilTelefono, tilDomicilio;
    private RadioGroup rgTipoDocumento;
    private RadioButton rbDni, rbCarnet;
    private boolean isDniSelected = true;

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
                    // Navegar a la pantalla de creación de contraseña
                    Intent intent = new Intent(LoginRegisterActivity.this, LoginCrearPassActivity.class);
                    // Pasar los datos del usuario al siguiente formulario
                    intent.putExtra("nombres", etNombres.getText().toString().trim());
                    intent.putExtra("apellidos", etApellidos.getText().toString().trim());
                    intent.putExtra("numeroDocumento", etNumeroDocumento.getText().toString().trim());
                    intent.putExtra("tipoDocumento", isDniSelected ? "DNI" : "Carné de extranjería");
                    intent.putExtra("fechaNacimiento", etFechaNacimiento.getText().toString().trim());
                    intent.putExtra("telefono", etTelefono.getText().toString().trim());
                    intent.putExtra("domicilio", etDomicilio.getText().toString().trim());
                    startActivity(intent);
                }
            }
        });

        // Configurar listener del botón registrarse como taxista
        btnRegistroTaxista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validar el formulario antes de permitir el registro como taxista
                if (validarFormulario()) {
                    // Navegar a la pantalla de registro de taxista
                    Intent intent = new Intent(LoginRegisterActivity.this, LoginDriverRegister.class);
                    // Pasar los datos del usuario al siguiente formulario
                    intent.putExtra("nombres", etNombres.getText().toString().trim());
                    intent.putExtra("apellidos", etApellidos.getText().toString().trim());
                    intent.putExtra("numeroDocumento", etNumeroDocumento.getText().toString().trim());
                    intent.putExtra("tipoDocumento", isDniSelected ? "DNI" : "Carné de extranjería");
                    intent.putExtra("fechaNacimiento", etFechaNacimiento.getText().toString().trim());
                    intent.putExtra("telefono", etTelefono.getText().toString().trim());
                    intent.putExtra("domicilio", etDomicilio.getText().toString().trim());
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginRegisterActivity.this,
                            "Debes completar correctamente todos los campos del formulario",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void inicializarViews() {
        // Botones
        btnContinuar = findViewById(R.id.btn_continuar);
        btnRegistroTaxista = findViewById(R.id.btn_registro_taxista);

        // RadioGroup y RadioButtons para tipo de documento
        rgTipoDocumento = findViewById(R.id.rg_tipo_documento);
        rbDni = findViewById(R.id.rb_dni);
        rbCarnet = findViewById(R.id.rb_carnet);

        // TextInputEditText
        etNombres = findViewById(R.id.et_nombres);
        etApellidos = findViewById(R.id.et_apellidos);
        etNumeroDocumento = findViewById(R.id.et_dni); // Mantenemos el ID pero cambiamos la referencia
        etFechaNacimiento = findViewById(R.id.et_fecha_nacimiento);
        etTelefono = findViewById(R.id.et_telefono);
        etDomicilio = findViewById(R.id.et_domicilio);

        // TextInputLayout
        tilNombres = findViewById(R.id.til_nombres);
        tilApellidos = findViewById(R.id.til_apellidos);
        tilNumeroDocumento = findViewById(R.id.til_dni); // Mantenemos el ID pero cambiamos la referencia
        tilFechaNacimiento = findViewById(R.id.til_fecha_nacimiento);
        tilTelefono = findViewById(R.id.til_telefono);
        tilDomicilio = findViewById(R.id.til_domicilio);
    }

    private void configurarListeners() {
        // Listener para el RadioGroup de tipo de documento
        rgTipoDocumento.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_dni) {
                    isDniSelected = true;
                    etNumeroDocumento.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
                    etNumeroDocumento.setHint("Número de DNI");
                    tilNumeroDocumento.setHint("Número de DNI");
                } else if (checkedId == R.id.rb_carnet) {
                    isDniSelected = false;
                    etNumeroDocumento.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
                    etNumeroDocumento.setHint("Número de carné de extranjería");
                    tilNumeroDocumento.setHint("Número de carné de extranjería");
                }
            }
        });

        // Listener para el campo de fecha de nacimiento
        etFechaNacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(LoginRegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        etFechaNacimiento.setText(dateFormat.format(calendar.getTime()));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // Listeners para validaciones en tiempo real
        etNombres.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validarCampoNombre(s.toString(), tilNombres);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etApellidos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validarCampoNombre(s.toString(), tilApellidos);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private boolean validarFormulario() {
        boolean esValido = true;

        // Validar nombres
        if (!validarCampoNombre(etNombres.getText().toString().trim(), tilNombres)) {
            esValido = false;
        }

        // Validar apellidos
        if (!validarCampoNombre(etApellidos.getText().toString().trim(), tilApellidos)) {
            esValido = false;
        }

        // Validar número de documento
        if (TextUtils.isEmpty(etNumeroDocumento.getText().toString().trim())) {
            tilNumeroDocumento.setError("El número de documento es obligatorio");
            esValido = false;
        } else {
            tilNumeroDocumento.setError(null);
        }

        // Validar fecha de nacimiento
        if (TextUtils.isEmpty(etFechaNacimiento.getText().toString().trim())) {
            tilFechaNacimiento.setError("La fecha de nacimiento es obligatoria");
            esValido = false;
        } else {
            tilFechaNacimiento.setError(null);
        }

        // Validar teléfono
        if (TextUtils.isEmpty(etTelefono.getText().toString().trim())) {
            tilTelefono.setError("El teléfono es obligatorio");
            esValido = false;
        } else {
            tilTelefono.setError(null);
        }

        // Validar domicilio
        if (TextUtils.isEmpty(etDomicilio.getText().toString().trim())) {
            tilDomicilio.setError("El domicilio es obligatorio");
            esValido = false;
        } else {
            tilDomicilio.setError(null);
        }

        return esValido;
    }

    private boolean validarCampoNombre(String nombre, TextInputLayout tilCampo) {
        if (TextUtils.isEmpty(nombre)) {
            tilCampo.setError("Este campo es obligatorio");
            return false;
        } else if (!NOMBRE_PATTERN.matcher(nombre).matches()) {
            tilCampo.setError("El nombre no es válido");
            return false;
        } else {
            tilCampo.setError(null);
            return true;
        }
    }
}
