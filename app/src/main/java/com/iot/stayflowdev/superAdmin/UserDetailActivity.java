package com.iot.stayflowdev.superAdmin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.User;
import com.iot.stayflowdev.utils.ImageLoadingUtils;

public class UserDetailActivity extends BaseSuperAdminActivity {

    private static final String TAG = "UserDetailActivity";

    public static final String EXTRA_USER_ID = "USER_ID";
    public static final String EXTRA_USER_NAME = "USER_NAME";
    public static final String EXTRA_USER_ROLE = "USER_ROLE";
    public static final String EXTRA_USER_ROLE_DESC = "USER_ROLE_DESC";
    public static final String EXTRA_USER_ENABLED = "USER_ENABLED";

    private String userId;
    private String userName;
    private String userRole;
    private String userRoleDescription;
    private boolean userEnabled;
    private FirebaseFirestore db;
    private User currentUser;

    private CircularProgressIndicator progressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas comunes
        progressIndicator = findViewById(R.id.progressIndicator);
        if (progressIndicator == null) {
            // Si el progressIndicator no existe en el layout
            Log.w(TAG, "ProgressIndicator no encontrado en el layout");
        }

        // Obtener datos del Intent
        userId = getIntent().getStringExtra(EXTRA_USER_ID);
        userName = getIntent().getStringExtra(EXTRA_USER_NAME);
        userRole = getIntent().getStringExtra(EXTRA_USER_ROLE);
        userRoleDescription = getIntent().getStringExtra(EXTRA_USER_ROLE_DESC);
        userEnabled = getIntent().getBooleanExtra(EXTRA_USER_ENABLED, false);

        // Validar que se recibieron los datos necesarios
        if (userName == null || userRole == null || userRoleDescription == null) {
            Toast.makeText(this, "Error: Datos de usuario incompletos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar datos básicos inicialmente
        initBasicViews();

        // Cargar datos completos si tenemos el userId
        if (userId != null && !userId.isEmpty()) {
            fetchUserData();
        } else {
            Toast.makeText(this, "Error: ID de usuario no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.superadmin_user_detail;
    }

    @Override
    protected int getBottomNavigationSelectedItem() {
        return R.id.nav_gestion; // O el ítem que corresponda
    }

    @Override
    protected String getToolbarTitle() {
        return "Detalles de Usuario";
    }

    private void initBasicViews() {
        // Inicializar vistas con datos básicos del Intent
        TextView textViewUserName = findViewById(R.id.textViewUserName);
        TextView textViewUserRole = findViewById(R.id.textViewUserRole);
        TextView textViewAccountStatus = findViewById(R.id.textViewAccountStatus);
        SwitchCompat switchAccountStatus = findViewById(R.id.switchAccountStatus);

        // Configurar datos del usuario
        textViewUserName.setText(userName);
        textViewUserRole.setText(userRoleDescription);
        textViewAccountStatus.setText(userEnabled ? "Habilitado" : "Deshabilitado");
        switchAccountStatus.setChecked(userEnabled);

        // Configurar listener para cambios en el switch
        switchAccountStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentUser != null) {
                showChangeStatusConfirmation(isChecked);
            }
        });
    }

    private void showChangeStatusConfirmation(boolean newStatus) {
        // Implementar diálogo de confirmación similar al de GestionActivity
        // Para mantener el código simple, solo mostramos un Toast por ahora
        Toast.makeText(this, "Función para cambiar estado a: " + (newStatus ? "habilitado" : "deshabilitado"), Toast.LENGTH_SHORT).show();

        if (userId != null) {
            updateUserStatus(userId, newStatus);
        }
    }

    private void updateUserStatus(String userId, boolean newStatus) {
        setLoading(true);
        db.collection("usuarios").document(userId)
            .update("estado", newStatus)
            .addOnSuccessListener(aVoid -> {
                setLoading(false);
                Toast.makeText(this, "Estado actualizado correctamente", Toast.LENGTH_SHORT).show();

                // Actualizar la UI
                TextView textViewAccountStatus = findViewById(R.id.textViewAccountStatus);
                textViewAccountStatus.setText(newStatus ? "Habilitado" : "Deshabilitado");

                // Actualizar el objeto usuario
                if (currentUser != null) {
                    currentUser.setEstado(newStatus);
                }
            })
            .addOnFailureListener(e -> {
                setLoading(false);
                Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                // Revertir el switch si falla
                SwitchCompat switchAccountStatus = findViewById(R.id.switchAccountStatus);
                switchAccountStatus.setChecked(!switchAccountStatus.isChecked());
            });
    }

    private void fetchUserData() {
        setLoading(true);

        db.collection("usuarios").document(userId)
            .get()
            .addOnCompleteListener(task -> {
                setLoading(false);

                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        currentUser = document.toObject(User.class);
                        // Asegurarse de que el usuario tiene el ID correcto
                        if (currentUser != null) {
                            currentUser.setUid(document.getId());
                            updateUIWithUserData(currentUser);
                        }
                    } else {
                        Log.d(TAG, "No se encontró el documento para el usuario con ID: " + userId);
                        Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "Error al obtener documento", task.getException());
                    Toast.makeText(this, "Error al cargar datos: " +
                                  (task.getException() != null ? task.getException().getMessage() : "desconocido"),
                                  Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void updateUIWithUserData(User user) {
        TextView textViewDNI = findViewById(R.id.textViewDNI);
        TextView textViewEmail = findViewById(R.id.textViewEmail);
        TextView textViewPhone = findViewById(R.id.textViewPhone);
        ImageView imageViewProfile = findViewById(R.id.imageViewProfile);

        // Actualizar datos del usuario con la información real de Firestore
        TextView textViewUserName = findViewById(R.id.textViewUserName);
        textViewUserName.setText(user.getName()); // Usa el método getName que combina nombres y apellidos

        // DNI (número de documento)
        String documentInfo = user.getTipoDocumento() != null && !user.getTipoDocumento().isEmpty() ?
                              user.getTipoDocumento() + ": " : "";
        documentInfo += user.getNumeroDocumento() != null ? user.getNumeroDocumento() : "No disponible";
        textViewDNI.setText(documentInfo);

        // Email
        textViewEmail.setText(user.getEmail() != null ? user.getEmail() : "No disponible");

        // Teléfono
        textViewPhone.setText(user.getTelefono() != null ? user.getTelefono() : "No disponible");

        // Foto de perfil
        if (user.getFotoPerfilUrl() != null && !user.getFotoPerfilUrl().isEmpty() && !user.getFotoPerfilUrl().equals("null")) {
            // Debug: Log para verificar la URL
            Log.d(TAG, "Cargando imagen de perfil: " + user.getFotoPerfilUrl());

            ImageLoadingUtils.loadImageWithGlide(this, user.getFotoPerfilUrl(), imageViewProfile, R.drawable.ic_person, R.drawable.ic_person);
        } else {
            // No hay foto, mostrar ícono por defecto con fondo
            Log.d(TAG, "Usuario sin foto de perfil, mostrando icono por defecto");

            ImageLoadingUtils.clearImage(imageViewProfile);
            imageViewProfile.setImageResource(R.drawable.ic_person);
            imageViewProfile.setBackgroundColor(getColor(R.color.md_theme_surfaceVariant));
            imageViewProfile.setPadding(16, 16, 16, 16);
            imageViewProfile.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        // Mostrar datos específicos según el rol
        setupSpecificRoleData(user);
    }

    private void setupSpecificRoleData(User user) {
        // Implementar lógica para mostrar datos específicos según el rol
        // Por ejemplo, para admin_hotel mostrar el hotel asignado, etc.

        if (user.getDatosEspecificos() != null) {
            switch (user.getRol()) {
                case "adminhotel":
                    // Mostrar hotel asignado
                    String hotel = user.getDatosEspecificos().containsKey("hotel") ?
                                  user.getDatosEspecificos().get("hotel").toString() :
                                  "No asignado";
                    // Aquí podrías mostrar estos datos en algún TextView adicional
                    break;
                case "driver":
                    // Mostrar datos específicos del taxista
                    // Como placa, modelo de vehículo, etc.
                    break;
                case "usuario":
                    // Mostrar datos específicos del cliente
                    // Como preferencias, historial, etc.
                    break;
            }
        }
    }

    private void setLoading(boolean isLoading) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
}