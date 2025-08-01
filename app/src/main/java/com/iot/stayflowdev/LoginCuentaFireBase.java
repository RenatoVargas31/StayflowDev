package com.iot.stayflowdev;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.Driver.Activity.DriverInicioActivity;
import com.iot.stayflowdev.adminHotel.AdminInicioActivity;
import com.iot.stayflowdev.utils.UserSessionManager;

import java.util.UUID;

public class LoginCuentaFireBase extends AppCompatActivity {

    private static final String TAG = "LoginCuentaFireBase";
    private TextInputEditText emailInput, passwordInput;
    private Button loginButton;
    private TextView forgotPasswordText;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_cuenta_fire_base);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase Auth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = UserSessionManager.getInstance();

        // Inicializar vistas
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        progressBar = findViewById(R.id.progressBar);

        // Configurar listener del botón de inicio de sesión
        loginButton.setOnClickListener(v -> {
            iniciarSesion();
        });

        // Configurar el enlace de olvidé mi contraseña
        forgotPasswordText.setOnClickListener(v -> {
            // Redirigir a la actividad de ingreso de correo
            Intent intent = new Intent(LoginCuentaFireBase.this, LoginIngCorreoActivity.class);
            startActivity(intent);
        });

        verificarConductorRecienRegistrado();

    }

    private void iniciarSesion() {
        // Obtener los valores de los campos
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validar que los campos no estén vacíos
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("El correo electrónico es obligatorio");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("La contraseña es obligatoria");
            passwordInput.requestFocus();
            return;
        }

        // Mostrar el indicador de progreso
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        // Autenticar con Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Ocultar el indicador de progreso
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        // Login exitoso, verificar rol y redirigir
                        Log.d(TAG, "signInWithEmail:success");
                        Toast.makeText(LoginCuentaFireBase.this,
                                "Autenticación exitosa",
                                Toast.LENGTH_SHORT).show();
                        verificarRolYRedirigir();
                    } else {
                        // Si falla el login, mostrar un mensaje al usuario
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginCuentaFireBase.this,
                                "Autenticación fallida: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        loginButton.setEnabled(true);
                    }
                });
    }

    private void verificarRolYRedirigir() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Usuario no autenticado");
            loginButton.setEnabled(true);
            return;
        }

        String userEmail = user.getEmail();

        // Mostrar un mensaje de espera
        Toast.makeText(this, "Verificando información de usuario...", Toast.LENGTH_SHORT).show();

        // Buscar el usuario en Firestore para verificar su rol
        db.collection("usuarios")
                .whereEqualTo("correo", userEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Obtener los datos del usuario
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String rol = document.getString("rol");
                        String userId = document.getId();

                        // Marcar usuario como conectado y generar token de sesión
                        String sessionToken = UUID.randomUUID().toString();
                        sessionManager.setUserConnected(userId, sessionToken);

                        Log.d(TAG, "Usuario " + userId + " marcado como conectado con token: " + sessionToken);


                        // Verificar si el usuario está activo según su rol
                        if (rol != null) {
                            if (rol.equalsIgnoreCase("usuario")) {
                                // Para usuarios regulares, verificar estado
                                Boolean estado = document.getBoolean("estado");
                                if (estado != null && !estado) {
                                    // Usuario desactivado
                                    Toast.makeText(this, "Tu cuenta ha sido desactivada. Contacta a soporte.", Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                    loginButton.setEnabled(true);
                                    return;
                                }
                                // Usuario activo, continuar
                                irAActividadSegunRol(rol);
                            } else if (rol.equalsIgnoreCase("driver")) {
                                // Para conductores, verificar estado y verificación
                                Boolean estado = document.getBoolean("estado");
                                Boolean verificado = document.getBoolean("verificado");

                                if ((estado != null && !estado) || (verificado != null && !verificado)) {
                                    // Personalizar mensaje según el caso
                                    String mensaje;
                                    if (verificado != null && !verificado) {
                                        mensaje = "Tu cuenta aún no ha sido verificada. Por favor, espera la verificación.";
                                    } else {
                                        mensaje = "Tu cuenta ha sido desactivada. Contacta a soporte.";
                                    }
                                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                    loginButton.setEnabled(true);
                                    return;
                                }
                                // Driver activo y verificado, continuar
                                irAActividadSegunRol(rol);
                            } else {
                                // Para otros roles (admin, superadmin), solo verificamos que el rol exista
                                irAActividadSegunRol(rol);
                            }
                        } else {
                            // Rol no definido
                            Log.w(TAG, "Rol no definido para el usuario");
                            irAActividadPorDefecto();
                        }
                    } else {
                        Log.w(TAG, "No se encontró información del usuario en Firestore");
                        // Si no se encuentra el rol en la base de datos, redirigir a una actividad por defecto
                        irAActividadPorDefecto();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener información del usuario", e);
                    Toast.makeText(this, "Error al obtener información del usuario", Toast.LENGTH_SHORT).show();
                    irAActividadPorDefecto();
                    loginButton.setEnabled(true);
                });
    }

    private void irAActividadSegunRol(String rol) {
        Intent intent;

        if (rol == null) {
            irAActividadPorDefecto();
            return;
        }

        // Convertir el rol a minúsculas y eliminar espacios
        String rolNormalizado = rol.toLowerCase().trim();
        Log.d(TAG, "Rol normalizado para comparación: '" + rolNormalizado + "'");

        switch (rolNormalizado) {
            case "adminhotel":
                Log.d(TAG, "Rol detectado: adminhotel - Navegando a AdminInicioActivity");
                intent = new Intent(this, com.iot.stayflowdev.adminHotel.AdminInicioActivity.class);
                break;
            case "driver":
                Log.d(TAG, "Rol detectado: driver - Navegando a DriverInicioActivity");
                intent = new Intent(this, com.iot.stayflowdev.Driver.Activity.DriverInicioActivity.class);
                break;
            case "superadmin":
                Log.d(TAG, "Rol detectado: superadmin - Navegando a InicioActivity");
                intent = new Intent(this, com.iot.stayflowdev.superAdmin.InicioActivity.class);
                break;
            case "usuario":
                Log.d(TAG, "Rol detectado: usuario - Navegando a ClientePerfilActivity");
                intent = new Intent(this, com.iot.stayflowdev.cliente.ClientePerfilActivity.class);
                break;
            default:
                Log.w(TAG, "Rol desconocido: '" + rol + "' - Navegando a actividad por defecto");
                irAActividadPorDefecto();
                return;
        }

        startActivity(intent);
        finish(); // Cierra esta actividad para que el usuario no pueda volver atrás con el botón de retroceso
    }

    private void irAActividadPorDefecto() {
        // Por defecto, navegar a LoginFireBaseActivity
        Log.d(TAG, "Navegando a actividad por defecto (LoginFireBaseActivity)");
        Toast.makeText(this, "No se encontró un rol válido para este usuario", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, LoginFireBaseActivity.class);
        startActivity(intent);
        finish();
    }

    private void verificarConductorRecienRegistrado() {
        Intent intent = getIntent();
        boolean esConductorRecienRegistrado = intent.getBooleanExtra("conductorRecienRegistrado", false);
        String mensajeActivacion = intent.getStringExtra("mensajeActivacion");

        if (esConductorRecienRegistrado && mensajeActivacion != null) {
            mostrarDialogoActivacionCuenta(mensajeActivacion);
        }
    }

    private void mostrarDialogoActivacionCuenta(String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle("Cuenta Pendiente de Activación")
                .setMessage(mensaje)
                .setPositiveButton("Entendido", (dialog, which) -> {
                    dialog.dismiss();
                    // Mostrar un toast adicional
                    Toast.makeText(LoginCuentaFireBase.this,
                            "Podrás iniciar sesión una vez que tu cuenta sea activada",
                            Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }
}
