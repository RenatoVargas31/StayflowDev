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
import com.iot.stayflowdev.services.LogService;

public class AddHotelAdminActivity extends AppCompatActivity {

    private static final String TAG = "AddHotelAdminActivity";

    private LinearLayout sectionInfoPersonal;
    private LinearLayout sectionCredenciales;
    private MaterialButton buttonGuardar;
    private CircularProgressIndicator progressIndicator;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private LogService logService; // Servicio para registrar logs

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

        // Validar longitud mínima de 8 caracteres
        if (pass.length() < 8) {
            password.setError("La contraseña debe tener al menos 8 caracteres");
            isValid = false;
        }

        // Validar que contenga al menos una letra mayúscula
        if (!pass.matches(".*[A-Z].*")) {
            password.setError("La contraseña debe contener al menos una letra mayúscula");
            isValid = false;
        }

        // Validar que contenga al menos dos caracteres especiales
        String specialChars = "!@#$%^&*()_-+=<>?/[]{}|~";
        int specialCharCount = 0;

        for (char c : pass.toCharArray()) {
            if (specialChars.contains(String.valueOf(c))) {
                specialCharCount++;
            }
        }

        if (specialCharCount < 2) {
            password.setError("La contraseña debe contener al menos 2 caracteres especiales (!@#$%^&*()_-+=<>?/[]{}|~)");
            isValid = false;
        }

        // Validar que las contraseñas coincidan
        if (!pass.equals(confirm)) {
            confirmPassword.setError("Las contraseñas no coinciden");
            isValid = false;
        }

        if (!isValid) {
            Snackbar.make(password, "La contraseña debe tener al menos 8 caracteres, una letra mayúscula y dos caracteres especiales", Snackbar.LENGTH_LONG).show();
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

        // GUARDAR LA INFORMACIÓN DEL SUPER ADMIN ACTUAL
        if (mAuth.getCurrentUser() == null) {
            setLoadingState(false);
            Snackbar.make(findViewById(android.R.id.content),
                         "Error: No hay usuario autenticado", Snackbar.LENGTH_LONG).show();
            return;
        }

        String superAdminEmail = mAuth.getCurrentUser().getEmail();
        String superAdminUid = mAuth.getCurrentUser().getUid();

        // Primero, crear el usuario en Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, contraseña)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // El usuario se creó correctamente en Auth
                    String newAdminUid = task.getResult().getUser().getUid();
                    Log.d(TAG, "Nuevo administrador creado con UID: " + newAdminUid);

                    // Ahora, guardar los datos en Firestore
                    guardarUsuarioEnFirestore(newAdminUid, nombres, apellidos, email, telefono,
                                             tipoDocumento, numeroDocumento, habilitado,
                                             superAdminEmail, superAdminUid);
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
                                         String numeroDocumento, boolean habilitado,
                                         String superAdminEmail, String superAdminUid) {
        // Crear el mapa de datos para Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put("nombres", nombres);
        userData.put("apellidos", apellidos);
        userData.put("correo", email);
        userData.put("telefono", telefono);
        userData.put("rol", "adminhotel");
        userData.put("estado", habilitado);
        userData.put("tipoDocumento", tipoDocumento);
        userData.put("numeroDocumento", numeroDocumento);
        userData.put("fotoPerfilUrl", "");
        userData.put("fechaNacimiento", "");
        userData.put("domicilio", "");

        // Datos específicos para admin_hotel
        Map<String, Object> datosEspecificos = new HashMap<>();
        datosEspecificos.put("hotel", "");
        userData.put("datosEspecificos", datosEspecificos);

        // Guardar en Firestore
        db.collection("usuarios").document(uid)
            .set(userData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Documento guardado correctamente con UID: " + uid);

                // CERRAR SESIÓN DEL NUEVO ADMIN Y RECUPERAR DATOS DEL SUPERADMIN
                mAuth.signOut();

                // Recuperar datos del superadmin desde Firestore para re-autenticarlo
                recuperarSesionSuperAdmin(superAdminUid, uid, nombres, apellidos,
                                        email, telefono, tipoDocumento, numeroDocumento, habilitado);
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error al guardar documento", e);
                setLoadingState(false);
                Snackbar.make(findViewById(android.R.id.content),
                            "Error al guardar datos: " + e.getMessage(),
                            Snackbar.LENGTH_LONG).show();
            });
    }

    private void recuperarSesionSuperAdmin(String superAdminUid, String newAdminUid,
                                         String nombres, String apellidos, String email,
                                         String telefono, String tipoDocumento,
                                         String numeroDocumento, boolean habilitado) {

        // Obtener los datos del superadmin desde Firestore
        db.collection("usuarios").document(superAdminUid)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String superAdminEmail = documentSnapshot.getString("correo");

                    if (superAdminEmail != null) {
                        // Mostrar diálogo para que el superadmin ingrese su contraseña
                        mostrarDialogoReautenticacion(superAdminEmail, superAdminUid, newAdminUid,
                                                    nombres, apellidos, email, telefono,
                                                    tipoDocumento, numeroDocumento, habilitado);
                    } else {
                        // No se pudo obtener el email, redirigir al login
                        redirigirAlLogin("No se pudo recuperar la sesión automáticamente");
                    }
                } else {
                    redirigirAlLogin("No se encontraron datos del superadmin");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al recuperar datos del superadmin", e);
                redirigirAlLogin("Error al recuperar la sesión");
            });
    }

    private void mostrarDialogoReautenticacion(String superAdminEmail, String superAdminUid,
                                             String newAdminUid, String nombres, String apellidos,
                                             String email, String telefono, String tipoDocumento,
                                             String numeroDocumento, boolean habilitado) {

        // Crear un diálogo personalizado para la contraseña
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Confirmar Identidad");
        builder.setMessage("Para mantener su sesión activa, por favor ingrese su contraseña:");

        // Crear el campo de entrada para la contraseña
        final android.widget.EditText passwordInput = new android.widget.EditText(this);
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                                  android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint("Contraseña del superadmin");
        builder.setView(passwordInput);

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String password = passwordInput.getText().toString();
            if (!password.isEmpty()) {
                // Intentar re-autenticar al superadmin
                reautenticarSuperAdmin(superAdminEmail, password, superAdminUid, newAdminUid,
                                     nombres, apellidos, email, telefono, tipoDocumento,
                                     numeroDocumento, habilitado);
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                             "Debe ingresar la contraseña", Snackbar.LENGTH_SHORT).show();
                redirigirAlLogin("Contraseña requerida para continuar");
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            redirigirAlLogin("Sesión no restaurada");
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void reautenticarSuperAdmin(String superAdminEmail, String password, String superAdminUid,
                                       String newAdminUid, String nombres, String apellidos,
                                       String email, String telefono, String tipoDocumento,
                                       String numeroDocumento, boolean habilitado) {

        // Re-autenticar al superadmin
        mAuth.signInWithEmailAndPassword(superAdminEmail, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Superadmin re-autenticado exitosamente");

                    // Registrar en el servicio de logs
                    registrarCreacionAdministrador(newAdminUid, nombres, apellidos, email,
                                                 telefono, tipoDocumento, numeroDocumento, habilitado);

                    setLoadingState(false);

                    // Mostrar mensaje de éxito
                    Snackbar.make(findViewById(android.R.id.content),
                                "✅ Administrador creado exitosamente. Sesión mantenida.",
                                Snackbar.LENGTH_LONG).show();

                    // Crear Intent con los datos para actualizar la UI
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(EXTRA_ADMIN_NAME, nombres);
                    resultIntent.putExtra(EXTRA_ADMIN_APELLIDOS, apellidos);
                    resultIntent.putExtra(EXTRA_ADMIN_EMAIL, email);
                    resultIntent.putExtra(EXTRA_ADMIN_ROLE, "adminhotel");
                    resultIntent.putExtra(EXTRA_ADMIN_ROLE_DESC, "Administrador de Hotel");
                    resultIntent.putExtra(EXTRA_ADMIN_ENABLED, habilitado);
                    resultIntent.putExtra("REQUIRE_REAUTH", false); // NO se requiere re-autenticación

                    setResult(RESULT_OK, resultIntent);

                    // Cerrar la actividad después de un breve delay
                    new android.os.Handler().postDelayed(() -> {
                        finish();
                    }, 1500);

                } else {
                    Log.w(TAG, "Error al re-autenticar superadmin", task.getException());
                    setLoadingState(false);
                    redirigirAlLogin("Contraseña incorrecta. Debe iniciar sesión nuevamente.");
                }
            });
    }

    private void redirigirAlLogin(String mensaje) {
        setLoadingState(false);

        Snackbar.make(findViewById(android.R.id.content), mensaje, Snackbar.LENGTH_LONG).show();

        // Redirigir al login después de un breve delay
        new android.os.Handler().postDelayed(() -> {
            Intent loginIntent = new Intent(this, com.iot.stayflowdev.LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        }, 2000);
    }

    // Métodos auxiliares que faltaban
    private void setLoadingState(boolean isLoading) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        // Deshabilitar/habilitar botones durante la carga
        if (buttonGuardar != null) {
            buttonGuardar.setEnabled(!isLoading);
        }

        MaterialButton buttonContinuar = findViewById(R.id.buttonContinuar);
        if (buttonContinuar != null) {
            buttonContinuar.setEnabled(!isLoading);
        }

        // Cambiar texto del botón para indicar estado de carga
        if (buttonGuardar != null) {
            if (isLoading) {
                buttonGuardar.setText("Creando...");
            } else {
                buttonGuardar.setText("Guardar Administrador");
            }
        }
    }

    private void registrarCreacionAdministrador(String uid, String nombres, String apellidos,
                                              String email, String telefono, String tipoDocumento,
                                              String numeroDocumento, boolean habilitado) {
        try {
            // Inicializar el servicio de logs si no existe
            if (logService == null) {
                logService = new LogService();
            }

            // Crear los datos del log
            Map<String, Object> logData = new HashMap<>();
            logData.put("timestamp", com.google.firebase.Timestamp.now());
            logData.put("category", "user_management");
            logData.put("title", "Nuevo administrador de hotel creado");

            String description = String.format("Se creó un nuevo administrador de hotel:\n" +
                                              "Nombre: %s %s\n" +
                                              "Email: %s\n" +
                                              "Teléfono: %s\n" +
                                              "Documento: %s - %s\n" +
                                              "Estado: %s",
                                              nombres, apellidos, email, telefono,
                                              tipoDocumento, numeroDocumento,
                                              habilitado ? "Habilitado" : "Deshabilitado");

            logData.put("description", description);
            logData.put("leido", false);

            // Datos adicionales
            logData.put("adminId", uid);
            logData.put("adminEmail", email);
            logData.put("actionType", "admin_creation");

            // Guardar en la colección system_logs
            db.collection("system_logs")
                .add(logData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Log de creación de administrador guardado: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar log de creación de administrador", e);
                });

        } catch (Exception e) {
            Log.e(TAG, "Error al crear log de creación de administrador", e);
        }
    }
}
