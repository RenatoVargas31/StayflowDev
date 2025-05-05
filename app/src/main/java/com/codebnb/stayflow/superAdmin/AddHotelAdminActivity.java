package com.codebnb.stayflow.superAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.codebnb.stayflow.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import android.widget.LinearLayout;

public class AddHotelAdminActivity extends AppCompatActivity {

    private LinearLayout sectionInfoPersonal;
    private LinearLayout sectionCredenciales;
    private MaterialButton buttonGuardar;

    // Constantes para los extras del Intent
    public static final String EXTRA_ADMIN_NAME = "admin_name";
    public static final String EXTRA_ADMIN_ROLE = "admin_role";
    public static final String EXTRA_ADMIN_ROLE_DESC = "admin_role_desc";
    public static final String EXTRA_ADMIN_ENABLED = "admin_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_admin_form_activity);

        // Configurar Toolbar con navegación hacia atrás
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Dropdown tipo documento
        AutoCompleteTextView dropdownTipoDocumento = findViewById(R.id.dropdownTipoDocumento);
        String[] tiposDocumento = {"DNI", "Pasaporte", "Carnet de Extranjería", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tiposDocumento);
        dropdownTipoDocumento.setAdapter(adapter);

        // Referencias a secciones
        sectionInfoPersonal = findViewById(R.id.sectionInfoPersonal);
        sectionCredenciales = findViewById(R.id.sectionCredenciales);
        buttonGuardar = findViewById(R.id.buttonGuardar);

        // Botón continuar (pasa a la parte 2 del formulario)
        MaterialButton buttonContinuar = findViewById(R.id.buttonContinuar);
        buttonContinuar.setOnClickListener(view -> {
            if (validarFormulario()) {
                // Ocultar primera sección y mostrar la sección de credenciales
                sectionInfoPersonal.setVisibility(View.GONE);
                sectionCredenciales.setVisibility(View.VISIBLE);
            }
        });

        // Botón guardar (finaliza el formulario)
        buttonGuardar.setOnClickListener(v -> {
            if (validarCredenciales()) {
                guardarDatosAdministrador();
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Si estamos en la sección de credenciales, volver a la primera sección
            if (sectionCredenciales.getVisibility() == View.VISIBLE) {
                sectionCredenciales.setVisibility(View.GONE);
                sectionInfoPersonal.setVisibility(View.VISIBLE);
                return true;
            }
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Si estamos en la sección de credenciales, volver a la primera sección
        if (sectionCredenciales.getVisibility() == View.VISIBLE) {
            sectionCredenciales.setVisibility(View.GONE);
            sectionInfoPersonal.setVisibility(View.VISIBLE);
            return;
        }
        super.onBackPressed();
    }

    private boolean validarFormulario() {
        TextInputEditText editTextNombres = findViewById(R.id.editTextNombres);
        TextInputEditText editTextApellidos = findViewById(R.id.editTextApellidos);
        TextInputEditText editTextCorreo = findViewById(R.id.editTextCorreo);
        TextInputEditText editTextTelefono = findViewById(R.id.editTextTelefono);
        TextInputEditText editTextNumeroDocumento = findViewById(R.id.editTextNumeroDocumento);
        AutoCompleteTextView dropdownTipoDocumento = findViewById(R.id.dropdownTipoDocumento);

        boolean isValid = true;

        if (editTextNombres.getText().toString().trim().isEmpty()) {
            editTextNombres.setError("Campo obligatorio");
            isValid = false;
        }

        if (editTextApellidos.getText().toString().trim().isEmpty()) {
            editTextApellidos.setError("Campo obligatorio");
            isValid = false;
        }

        String email = editTextCorreo.getText().toString().trim();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextCorreo.setError("Correo electrónico inválido");
            isValid = false;
        }

        if (editTextTelefono.getText().toString().trim().isEmpty()) {
            editTextTelefono.setError("Campo obligatorio");
            isValid = false;
        }

        if (dropdownTipoDocumento.getText().toString().trim().isEmpty()) {
            dropdownTipoDocumento.setError("Seleccione un tipo de documento");
            isValid = false;
        }

        if (editTextNumeroDocumento.getText().toString().trim().isEmpty()) {
            editTextNumeroDocumento.setError("Campo obligatorio");
            isValid = false;
        }

        if (!isValid) {
            Snackbar.make(findViewById(R.id.buttonContinuar), "Corrige los errores del formulario", Snackbar.LENGTH_SHORT).show();
        }

        return isValid;
    }

    private boolean validarCredenciales() {
        TextInputEditText password = findViewById(R.id.editTextPassword);
        TextInputEditText confirmPassword = findViewById(R.id.editTextConfirmPassword);

        boolean isValid = true;

        String pass = password.getText().toString();
        String confirm = confirmPassword.getText().toString();

        if (pass.length() < 6) {
            password.setError("Mínimo 6 caracteres");
            isValid = false;
        }

        if (!pass.equals(confirm)) {
            confirmPassword.setError("Las contraseñas no coinciden");
            isValid = false;
        }

        if (!isValid) {
            Snackbar.make(password, "Corrige los errores en las credenciales", Snackbar.LENGTH_SHORT).show();
        }

        return isValid;
    }

    private void guardarDatosAdministrador() {
        // Obtener datos del formulario
        TextInputEditText editTextNombres = findViewById(R.id.editTextNombres);
        TextInputEditText editTextApellidos = findViewById(R.id.editTextApellidos);
        SwitchMaterial switchHabilitado = findViewById(R.id.switchHabilitado);

        // Construir el nombre completo
        String nombreCompleto = editTextNombres.getText().toString().trim() + " " +
                editTextApellidos.getText().toString().trim();

        // Obtener el estado del switch
        boolean habilitado = switchHabilitado.isChecked();

        // Crear un Intent con los datos
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_ADMIN_NAME, nombreCompleto);
        resultIntent.putExtra(EXTRA_ADMIN_ROLE, "Admin"); // Rol fijo para administradores
        resultIntent.putExtra(EXTRA_ADMIN_ROLE_DESC, "Admin Hotel");
        resultIntent.putExtra(EXTRA_ADMIN_ENABLED, habilitado);

        // Establecer el resultado
        setResult(RESULT_OK, resultIntent);

        // Registro para depuración
        Log.d("AddHotelAdmin", "Administrador guardado: " + nombreCompleto + ", Habilitado: " + habilitado);
    }
}