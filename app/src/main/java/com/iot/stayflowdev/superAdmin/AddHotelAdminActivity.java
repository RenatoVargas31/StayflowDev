package com.iot.stayflowdev.superAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.iot.stayflowdev.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.LinearLayout;
import java.util.HashMap;
import java.util.Map;

public class AddHotelAdminActivity extends AppCompatActivity {

    private static final String TAG = "AddHotelAdminActivity";

    private LinearLayout sectionInfoPersonal;
    private LinearLayout sectionCredenciales;
    private MaterialButton buttonGuardar;
    private CircularProgressIndicator progressIndicator;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Constantes para los extras del Intent
    public static final String EXTRA_ADMIN_NAME = "admin_name";
    public static final String EXTRA_ADMIN_APELLIDOS = "admin_apellidos";
    public static final String EXTRA_ADMIN_EMAIL = "admin_email";
    public static final String EXTRA_ADMIN_ROLE = "admin_role";
    public static final String EXTRA_ADMIN_ROLE_DESC = "admin_role_desc";
    public static final String EXTRA_ADMIN_ENABLED = "admin_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superadmin_add_admin_form_activity);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
        progressIndicator = findViewById(R.id.progressIndicator);

        if (progressIndicator == null) {
            // Si no existe en el layout, lo inicializamos programáticamente
            progressIndicator = new CircularProgressIndicator(this);
            progressIndicator.setIndeterminate(true);
            progressIndicator.setVisibility(View.GONE);
            // Añadir al layout según sea necesario
        }

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
        // Mostrar indicador de progreso
        setLoadingState(true);

        // Obtener datos del formulario
        TextInputEditText editTextNombres = findViewById(R.id.editTextNombres);
        TextInputEditText editTextApellidos = findViewById(R.id.editTextApellidos);
        TextInputEditText editTextCorreo = findViewById(R.id.editTextCorreo);
        TextInputEditText editTextTelefono = findViewById(R.id.editTextTelefono);
        TextInputEditText editTextNumeroDocumento = findViewById(R.id.editTextNumeroDocumento);
        AutoCompleteTextView dropdownTipoDocumento = findViewById(R.id.dropdownTipoDocumento);
        SwitchMaterial switchHabilitado = findViewById(R.id.switchHabilitado);
        TextInputEditText password = findViewById(R.id.editTextPassword);

        // Obtener los valores
        String nombres = editTextNombres.getText().toString().trim();
        String apellidos = editTextApellidos.getText().toString().trim();
        String email = editTextCorreo.getText().toString().trim();
        String telefono = editTextTelefono.getText().toString().trim();
        String tipoDocumento = dropdownTipoDocumento.getText().toString().trim();
        String numeroDocumento = editTextNumeroDocumento.getText().toString().trim();
        String contraseña = password.getText().toString();
        boolean habilitado = switchHabilitado.isChecked();

        // Primero, crear el usuario en Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, contraseña)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // El usuario se creó correctamente en Auth
                    String uid = task.getResult().getUser().getUid();
                    Log.d(TAG, "Usuario creado con UID: " + uid);

                    // Ahora, guardar los datos en Firestore
                    guardarUsuarioEnFirestore(uid, nombres, apellidos, email, telefono,
                                             tipoDocumento, numeroDocumento, habilitado);
                } else {
                    // Si falla la creación del usuario
                    setLoadingState(false);
                    Log.w(TAG, "Error al crear usuario", task.getException());
                    Snackbar.make(findViewById(android.R.id.content),
                                "Error al crear usuario: " + task.getException().getMessage(),
                                Snackbar.LENGTH_LONG).show();
                }
            });
    }

    private void guardarUsuarioEnFirestore(String uid, String nombres, String apellidos,
                                         String email, String telefono, String tipoDocumento,
                                         String numeroDocumento, boolean habilitado) {
        // Crear el mapa de datos para Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put("nombres", nombres);
        userData.put("apellidos", apellidos);
        userData.put("email", email);
        userData.put("telefono", telefono);
        userData.put("rol", "admin_hotel");
        userData.put("estado", habilitado ? "activo" : "inactivo");
        userData.put("tipoDocumento", tipoDocumento);
        userData.put("numeroDocumento", numeroDocumento);
        userData.put("fotoPerfilUrl", "");
        userData.put("fechaNacimiento", "");  // Campo requerido pero vacío por ahora
        userData.put("domicilio", "");  // Campo requerido pero vacío por ahora

        // Datos específicos para admin_hotel
        Map<String, Object> datosEspecificos = new HashMap<>();
        datosEspecificos.put("hotel", "");  // Se actualizará después en otra pantalla
        userData.put("datosEspecificos", datosEspecificos);

        // IMPORTANTE: Usamos .document(uid).set() para asegurarnos de que el documento
        // tenga el mismo ID que el usuario en Firebase Auth, evitando así documentos duplicados
        db.collection("usuarios").document(uid)
            .set(userData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Documento guardado correctamente con UID: " + uid);
                setLoadingState(false);

                // Crear un Intent con los datos completos para actualizar la UI
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_ADMIN_NAME, nombres);
                resultIntent.putExtra(EXTRA_ADMIN_APELLIDOS, apellidos);
                resultIntent.putExtra(EXTRA_ADMIN_EMAIL, email);
                resultIntent.putExtra(EXTRA_ADMIN_ROLE, "admin_hotel");
                resultIntent.putExtra(EXTRA_ADMIN_ROLE_DESC, "Administrador de Hotel");
                resultIntent.putExtra(EXTRA_ADMIN_ENABLED, habilitado);

                // Establecer el resultado y finalizar
                setResult(RESULT_OK, resultIntent);
                finish();
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error al guardar documento", e);
                setLoadingState(false);
                Snackbar.make(findViewById(android.R.id.content),
                            "Error al guardar datos: " + e.getMessage(),
                            Snackbar.LENGTH_LONG).show();
            });
    }

    private void setLoadingState(boolean isLoading) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        buttonGuardar.setEnabled(!isLoading);
    }
}