package com.iot.stayflowdev;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class LoginFireBaseActivity extends AppCompatActivity {

    private static final String TAG = "LoginFireBase";
    private Button btnEmailLogin, btnGoogleLogin, btnAlreadyHaveAccount;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Primero configura la vista
        setContentView(R.layout.activity_login_fire_base);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase Auth, Firestore y vistas
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        btnEmailLogin = findViewById(R.id.btnEmailLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnAlreadyHaveAccount = findViewById(R.id.btnAlreadyHaveAccount);

        // Temporalmente desactivar botones hasta verificar autenticación
        btnEmailLogin.setEnabled(false);
        btnGoogleLogin.setEnabled(false);

        // Configurar listeners
        btnEmailLogin.setOnClickListener(view -> iniciarSesionConEmail());
        btnGoogleLogin.setOnClickListener(view -> iniciarSesionConGoogle());
        btnAlreadyHaveAccount.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginCuentaFireBase.class);
            startActivity(intent);
        });

        // Verificar autenticación con el servidor
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                            // El token sigue siendo válido, redirigir
                            verificarRolYRedirigir();
                        } else {
                            // Token inválido o cuenta eliminada, habilitar botones
                            btnEmailLogin.setEnabled(true);
                            btnGoogleLogin.setEnabled(true);
                            Toast.makeText(LoginFireBaseActivity.this,
                                    "Sesión expirada, inicie sesión nuevamente",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // No hay usuario, habilitar botones
            btnEmailLogin.setEnabled(true);
            btnGoogleLogin.setEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verificar si el usuario ya está autenticado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Si ya está autenticado, redirigir según el rol
            verificarRolYRedirigir();
        }
    }

    private void iniciarSesionConEmail() {
        // Lista de proveedores para iniciar sesión con email
        List<AuthUI.IdpConfig> proveedores = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
        );
        iniciarInterfazDeAutenticacion(proveedores);
    }

    private void iniciarSesionConGoogle() {
        // Primero cerramos la sesión existente para forzar la elección de cuenta
        FirebaseAuth.getInstance().signOut();

        // Lista de proveedores para iniciar sesión con Google
        List<AuthUI.IdpConfig> proveedores = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );
        iniciarInterfazDeAutenticacion(proveedores);
    }

    private void iniciarInterfazDeAutenticacion(List<AuthUI.IdpConfig> proveedores) {
        try {
            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(proveedores)
                    .setIsSmartLockEnabled(false, false) // Deshabilitar SmartLock completamente (credenciales y cuentas sugeridas)
                    .build();
            signInLauncher.launch(signInIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error al iniciar autenticación: ", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Inicio de sesión exitoso
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Log.d(TAG, "signInWithCredential:success");

            // Registrar usuario en Firestore con rol "usuario" usando el mismo UID
            if (user != null) {
                String email = user.getEmail();
                String uid = user.getUid();

                // Log para depuración
                Log.d(TAG, "Creando usuario en Firestore con UID: " + uid);

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Crea el objeto de usuario
                java.util.Map<String, Object> usuario = new java.util.HashMap<>();
                usuario.put("correo", email);
                // Eliminamos la asignación de rol aquí, se asignará en LoginCargarFotoActivity

                // Guarda en la colección "usuarios" usando el UID como ID del documento
                db.collection("usuarios")
                  .document(uid)  // Usar el UID como ID del documento
                  .set(usuario)
                  .addOnSuccessListener(aVoid -> {
                      Log.d(TAG, "Usuario registrado en Firestore con ID: " + uid);
                  })
                  .addOnFailureListener(e -> {
                      Log.e(TAG, "Error al registrar usuario en Firestore", e);
                  });
            }

            verificarRolYRedirigir();
        } else {
            // Si el inicio de sesión falla, muestra un mensaje al usuario
            if (response == null) {
                Log.w(TAG, "signIn:canceled");
                Toast.makeText(this, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show();
            } else {
                Log.w(TAG, "signIn:failure", response.getError());
                Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void verificarRolYRedirigir() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Usuario no autenticado");
            return;
        }

        String userId = user.getUid();
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

                        // Verificar si el usuario está activo según su rol
                        if (rol != null) {
                            if (rol.equalsIgnoreCase("usuario")) {
                                // Para usuarios regulares, verificar estado
                                Boolean estado = document.getBoolean("estado");
                                if (estado != null && !estado) {
                                    // Usuario desactivado
                                    Toast.makeText(this, "Tu cuenta ha sido desactivada. Contacta a soporte.", Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                    // Habilitar botones nuevamente
                                    btnEmailLogin.setEnabled(true);
                                    btnGoogleLogin.setEnabled(true);
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
                                    // Habilitar botones nuevamente
                                    btnEmailLogin.setEnabled(true);
                                    btnGoogleLogin.setEnabled(true);
                                    return;
                                }
                                // Driver activo y verificado, continuar
                                irAActividadSegunRol(rol);
                            } else {
                                // Para otros roles (admin, superadmin), solo verificamos que el rol exista
                                irAActividadSegunRol(rol);
                            }
                        } else {
                            // Si el rol es nulo, redirigimos al usuario a la pantalla de registro
                            Log.d(TAG, "Rol es nulo - Navegando a LoginRegisterActivity");
                            Intent intent = new Intent(this, LoginRegisterActivity.class);
                            startActivity(intent);
                            finish();
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
                });
    }

    private void irAActividadSegunRol(String rol) {
        Intent intent;

        if (rol == null) {
            // Si el rol es nulo, redirigimos al usuario a la pantalla de registro
            Log.d(TAG, "Rol es nulo - Navegando a LoginRegisterActivity");
            intent = new Intent(this, LoginRegisterActivity.class);
            startActivity(intent);
            finish();
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
                intent = new Intent(this, com.iot.stayflowdev.cliente.ClienteBuscarActivity.class);
                break;
            default:
                // Si el rol no coincide con ninguno de los esperados, también redirigimos a la pantalla de registro
                Log.w(TAG, "Rol desconocido: '" + rol + "' - Navegando a LoginRegisterActivity");
                intent = new Intent(this, LoginRegisterActivity.class);
                startActivity(intent);
                finish();
                return;
        }

        startActivity(intent);
        finish(); // Cierra esta actividad para que el usuario no pueda volver atrás con el botón de retroceso
    }

    private void irAActividadPorDefecto() {
        // Por defecto, navegar a PerfilActivity (o cualquier otra actividad de tu elección)
        Log.d(TAG, "Navegando a actividad por defecto (PerfilActivity)");
        Intent intent = new Intent(this, com.iot.stayflowdev.cliente.ClienteBuscarActivity.class);
        startActivity(intent);
        finish();
    }
}

