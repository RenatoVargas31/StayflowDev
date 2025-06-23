package com.iot.stayflowdev;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginCrearPassActivity extends AppCompatActivity {

    private static final String TAG = "LoginCrearPass";

    private MaterialButton btnFinalizar;
    private TextInputEditText etPassword, etRepeatPassword;
    private TextInputLayout tilPassword, tilRepeatPassword;
    private ImageView btnClose;

    // Datos del usuario que se han recibido de actividades anteriores
    private String nombres, apellidos, numeroDocumento, tipoDocumento, fechaNacimiento, telefono, domicilio;
    private String placa, modelo, imagenVehiculo;
    private boolean esRegistroTaxista = false;
    private String email; // Email recibido desde Firebase Auth si estamos en un registro con Google

    // Firebase Auth
    private FirebaseAuth mAuth;

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

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Recibir datos de las actividades anteriores
        recibirDatos();

        // Inicializar vistas
        inicializarViews();

        // Configurar validaciones en tiempo real
        configurarValidaciones();

        // Configurar listener del botón finalizar/continuar
        btnFinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarContrasenas()) {
                    // Si el usuario está autenticado (registro con Google), actualizar contraseña
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null && !TextUtils.isEmpty(currentUser.getEmail())) {
                        email = currentUser.getEmail();
                        actualizarContrasenaUsuarioActual(etPassword.getText().toString());
                    } else {
                        // Si estamos en un registro normal, necesitamos el email
                        // En este punto, deberíamos tenerlo del flujo anterior o solicitarlo
                        if (!TextUtils.isEmpty(email)) {
                            registrarUsuarioNuevo(email, etPassword.getText().toString());
                        } else {
                            // Error: falta el correo electrónico
                            Toast.makeText(LoginCrearPassActivity.this,
                                "Error: No se ha proporcionado un correo electrónico",
                                Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }
            }
        });

        // Configurar el botón de cierre
        btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> finish());
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

    private void recibirDatos() {
        Intent intent = getIntent();

        // Datos básicos del usuario
        if (intent.hasExtra("nombres")) {
            nombres = intent.getStringExtra("nombres");
        }
        if (intent.hasExtra("apellidos")) {
            apellidos = intent.getStringExtra("apellidos");
        }
        if (intent.hasExtra("numeroDocumento")) {
            numeroDocumento = intent.getStringExtra("numeroDocumento");
            // Utilizar el número de documento como email por defecto si no se proporciona uno
            if (email == null && numeroDocumento != null) {
                email = numeroDocumento + "@stayflow.com";
            }
        }
        if (intent.hasExtra("tipoDocumento")) {
            tipoDocumento = intent.getStringExtra("tipoDocumento");
        }
        if (intent.hasExtra("fechaNacimiento")) {
            fechaNacimiento = intent.getStringExtra("fechaNacimiento");
        }
        if (intent.hasExtra("telefono")) {
            telefono = intent.getStringExtra("telefono");
        }
        if (intent.hasExtra("domicilio")) {
            domicilio = intent.getStringExtra("domicilio");
        }

        // Recibir correo electrónico explícitamente (con alta prioridad)
        if (intent.hasExtra("email")) {
            email = intent.getStringExtra("email");
        } else if (intent.hasExtra("correo")) {
            email = intent.getStringExtra("correo");
        }

        // Datos del vehículo (solo si es taxista)
        if (intent.hasExtra("placa")) {
            placa = intent.getStringExtra("placa");
        }
        if (intent.hasExtra("modelo")) {
            modelo = intent.getStringExtra("modelo");
        }

        // Ya no necesitamos recibir la imagen del vehículo porque se ha subido a Firebase
        // y sus datos se han guardado en la colección "vehiculo"

        // Flag para indicar si es registro de taxista
        if (intent.hasExtra("esRegistroTaxista")) {
            esRegistroTaxista = intent.getBooleanExtra("esRegistroTaxista", false);
        }

        // Email (desde Firebase Auth si es registro con Google)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();
        } else if (intent.hasExtra("email")) {
            email = intent.getStringExtra("email");
        }
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

    /**
     * Registra un nuevo usuario con email y contraseña en Firebase Auth
     */
    private void registrarUsuarioNuevo(String email, String password) {
        // Mostrar progreso
        mostrarProgreso(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mostrarProgreso(false);

                        if (task.isSuccessful()) {
                            // Registro exitoso, proceder a la pantalla de carga de foto
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Navegar a la siguiente pantalla
                            irAPantallaSiguiente();
                        } else {
                            // Si falla el registro, mostrar un mensaje
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginCrearPassActivity.this,
                                "Error al registrar: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Actualiza la contraseña del usuario actualmente autenticado
     */
    private void actualizarContrasenaUsuarioActual(String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Mostrar progreso
            mostrarProgreso(true);

            user.updatePassword(newPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mostrarProgreso(false);

                            if (task.isSuccessful()) {
                                Log.d(TAG, "User password updated.");
                                // Navegar a la siguiente pantalla
                                irAPantallaSiguiente();
                            } else {
                                Log.w(TAG, "Error updating password", task.getException());
                                Toast.makeText(LoginCrearPassActivity.this,
                                    "Error al actualizar contraseña: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * Muestra u oculta un indicador de progreso
     */
    private void mostrarProgreso(boolean mostrar) {
        // Implementar lógica para mostrar/ocultar progreso (ProgressBar)
        // Por ahora, solo deshabilitamos los botones durante el proceso
        btnFinalizar.setEnabled(!mostrar);
        if (mostrar) {
            Toast.makeText(this, "Procesando...", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navega a la pantalla de carga de foto de perfil
     */
    private void irAPantallaSiguiente() {
        Intent intent = new Intent(this, LoginCargarFotoActivity.class);

        // Pasar todos los datos necesarios
        intent.putExtra("nombres", nombres);
        intent.putExtra("apellidos", apellidos);
        intent.putExtra("numeroDocumento", numeroDocumento);
        intent.putExtra("tipoDocumento", tipoDocumento);
        intent.putExtra("fechaNacimiento", fechaNacimiento);
        intent.putExtra("telefono", telefono);
        intent.putExtra("domicilio", domicilio);

        // Si es taxista, incluir los datos del vehículo
        if (esRegistroTaxista) {
            intent.putExtra("esRegistroTaxista", true);
            intent.putExtra("placa", placa);
            intent.putExtra("modelo", modelo);
            if (imagenVehiculo != null) {
                intent.putExtra("imagenVehiculo", imagenVehiculo);
            }
        }

        startActivity(intent);
    }
}
