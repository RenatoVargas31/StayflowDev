package com.iot.stayflowdev.adminHotel;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iot.stayflowdev.LoginFireBaseActivity;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.repository.AdminHotelViewModel;
import com.iot.stayflowdev.adminHotel.service.NotificacionService;
import com.iot.stayflowdev.databinding.ActivityPerfilAdminBinding;
import com.iot.stayflowdev.model.User;
import com.iot.stayflowdev.utils.UserSessionManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PerfilAdminActivity extends AppCompatActivity {

    private ActivityPerfilAdminBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private static final String TAG = "PerfilAdmin";

    // UI Components para foto de perfil
    private ShapeableImageView imageViewProfile;
    private MaterialButton buttonChangePicture;
    private CircularProgressIndicator progressIndicator;
    private User currentUser;

    // Lanzador para seleccionar imagen
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // Control de navegación durante subida
    private boolean isUploadingImage = false;

    private ImageView notificationIcon;
    private TextView badgeText;
    private AdminHotelViewModel viewModel;
    private NotificacionService notificacionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPerfilAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        inicializarVistas();
        setupImagePickerLauncher();
        configurarViewModel();
        configurarToolbar();
        loadUserData();

        // Configurar menú inferior
        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.setSelectedItemId(R.id.menu_perfil);
        // Guarda una referencia a la actividad actual
        final Class<?> currentActivity = this.getClass();

        bottomNav.setOnItemSelectedListener(item -> {
            // Verificar si se está subiendo una imagen
            if (isUploadingImage) {
                Toast.makeText(this, "Por favor espera a que termine de subir la imagen", Toast.LENGTH_SHORT).show();
                return false; // Bloquear navegación
            }

            Intent intent = null;
            int id = item.getItemId();

            if (id == R.id.menu_inicio && currentActivity != AdminInicioActivity.class) {
                intent = new Intent(this, AdminInicioActivity.class);
            } else if (id == R.id.menu_reportes && currentActivity != ReportesAdminActivity.class) {
                intent = new Intent(this, ReportesAdminActivity.class);
            } else if (id == R.id.menu_huesped && currentActivity != HuespedAdminActivity.class) {
                intent = new Intent(this, HuespedAdminActivity.class);
            } else if (id == R.id.menu_mensajeria && currentActivity != MensajeriaAdminActivity.class) {
                intent = new Intent(this, MensajeriaAdminActivity.class);
            } else if (id == R.id.menu_perfil && currentActivity != PerfilAdminActivity.class) {
                intent = new Intent(this, PerfilAdminActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }

            return true;
        });

        // Cerrar sesión
        binding.btnCerrarSesion.setOnClickListener(v -> {
            // Verificar si se está subiendo una imagen
            if (isUploadingImage) {
                Toast.makeText(this, "Por favor espera a que termine de subir la imagen antes de cerrar sesión", Toast.LENGTH_SHORT).show();
                return;
            }
            cerrarSesion();
        });

        getSharedPreferences("notificaciones", MODE_PRIVATE)
                .edit()
                .remove("mostrada")
                .apply();

    }

    private void inicializarVistas() {
        // Vistas para foto de perfil
        imageViewProfile = findViewById(R.id.iv_profile_picture);
        buttonChangePicture = findViewById(R.id.buttonChangePicture);
        progressIndicator = findViewById(R.id.progressIndicator);

        // Configurar botón para cambiar foto
        buttonChangePicture.setOnClickListener(v -> openImagePicker());

        // Vistas existentes para notificaciones
        notificationIcon = findViewById(R.id.notification_icon);
        badgeText = findViewById(R.id.badge_text);

        // Inicializar servicio de notificaciones
        notificacionService = new NotificacionService(this);
    }

    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadProfileImage(imageUri);
                    }
                }
            }
        );
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        if (currentUser == null || mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "No se pudo identificar al usuario actual", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        isUploadingImage = true; // Iniciar control de navegación

        try {
            // Comprimir la imagen para reducir tamaño
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageData = baos.toByteArray();

            // Definir la ruta de almacenamiento
            String userId = mAuth.getCurrentUser().getUid();
            String imageName = userId + ".jpg";
            StorageReference profileImageRef = storageRef.child("fotos_perfil/" + imageName);

            // Subir la imagen
            UploadTask uploadTask = profileImageRef.putBytes(imageData);
            uploadTask
                .addOnSuccessListener(taskSnapshot -> {
                    // Obtener URL de descarga
                    profileImageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        // Actualizar la URL en Firestore
                        updateProfileImageUrl(downloadUri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    isUploadingImage = false; // Finalizar control de navegación
                    Toast.makeText(PerfilAdminActivity.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al subir imagen", e);
                });
        } catch (IOException e) {
            showLoading(false);
            isUploadingImage = false; // Finalizar control de navegación
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error al procesar imagen", e);
        }
    }

    private void updateProfileImageUrl(String imageUrl) {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("usuarios").document(userId)
            .update("fotoPerfilUrl", imageUrl)
            .addOnSuccessListener(aVoid -> {
                showLoading(false);
                isUploadingImage = false; // Finalizar control de navegación
                Toast.makeText(PerfilAdminActivity.this, "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show();

                // Actualizar la imagen en la interfaz
                Glide.with(PerfilAdminActivity.this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_perfil)
                    .error(R.drawable.ic_perfil)
                    .circleCrop()
                    .into(imageViewProfile);

                // Actualizar el objeto usuario
                if (currentUser != null) {
                    currentUser.setFotoPerfilUrl(imageUrl);
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                isUploadingImage = false; // Finalizar control de navegación
                Toast.makeText(PerfilAdminActivity.this, "Error al actualizar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error al actualizar URL de imagen en Firestore", e);
            });
    }

    private void loadUserData() {
        FirebaseUser currentFirebaseUser = mAuth.getCurrentUser();
        if (currentFirebaseUser == null) {
            Log.e(TAG, "No hay usuario autenticado");
            Toast.makeText(this, "Error: No se pudo obtener la información del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentFirebaseUser.getUid();
        showLoading(true);

        db.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                showLoading(false);
                if (documentSnapshot.exists()) {
                    currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null) {
                        updateUIWithUserData();
                    }
                } else {
                    Log.e(TAG, "El documento del usuario no existe en Firestore");
                    Toast.makeText(this, "No se encontró la información del usuario", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e(TAG, "Error al obtener datos del usuario", e);
                Toast.makeText(this, "Error al cargar los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void updateUIWithUserData() {
        if (currentUser == null) return;

        // Mostrar nombres y apellidos
        String nombreCompleto = "";
        if (currentUser.getNombres() != null && !currentUser.getNombres().isEmpty()) {
            nombreCompleto += currentUser.getNombres();
        }
        if (currentUser.getApellidos() != null && !currentUser.getApellidos().isEmpty()) {
            if (!nombreCompleto.isEmpty()) nombreCompleto += " ";
            nombreCompleto += currentUser.getApellidos();
        }

        if (nombreCompleto.trim().isEmpty()) {
            nombreCompleto = "Usuario sin nombre";
        }

        binding.tvNombreCompleto.setText(nombreCompleto);
        binding.tvNombreAdmin.setText(nombreCompleto);

        // Mostrar email
        if (currentUser.getEmail() != null) {
            binding.tvCorreoElectronico.setText(currentUser.getEmail());
        } else {
            binding.tvCorreoElectronico.setText("Correo no disponible");
        }

        // Cargar imagen de perfil si existe
        if (currentUser.getFotoPerfilUrl() != null && !currentUser.getFotoPerfilUrl().isEmpty()) {
            Glide.with(this)
                .load(currentUser.getFotoPerfilUrl())
                .placeholder(R.drawable.ic_perfil)
                .error(R.drawable.ic_perfil)
                .circleCrop()
                .into(imageViewProfile);
        }
    }

    private void showLoading(boolean show) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        // Deshabilitar el botón mientras se sube la imagen
        if (buttonChangePicture != null) {
            buttonChangePicture.setEnabled(!show);
        }
    }

    private void configurarViewModel() {
        // ViewModel
        viewModel = new ViewModelProvider(this).get(AdminHotelViewModel.class);

        // Observar notificaciones de checkout
        viewModel.getContadorNotificaciones().observe(this, contador -> {
            actualizarBadgeNotificaciones(contador);
        });

        // Cargar notificaciones al iniciar
        viewModel.cargarNotificacionesCheckoutTiempoReal();

        // Iniciar actualizaciones automáticas cada 5 minutos
        viewModel.iniciarActualizacionAutomatica();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener actualizaciones automáticas
        if (viewModel != null) {
            viewModel.detenerActualizacionAutomatica();
        }
    }
    private void configurarToolbar() {
        // Configurar click del icono de notificaciones
        notificationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificacionesAdminActivity.class);
            startActivity(intent);
        });
    }
    private void actualizarBadgeNotificaciones(Integer contador) {
        if (contador != null && contador > 0) {
            badgeText.setVisibility(View.VISIBLE);
            badgeText.setText(contador > 99 ? "99+" : String.valueOf(contador));
        } else {
            badgeText.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar notificaciones cuando regresamos a esta activity
        if (viewModel != null) {
            viewModel.cargarNotificacionesCheckoutTiempoReal();
        }
    }

    @Override
    public void onBackPressed() {
        // Verificar si se está subiendo una imagen
        if (isUploadingImage) {
            Toast.makeText(this, "Por favor espera a que termine de subir la imagen", Toast.LENGTH_SHORT).show();
            return; // Bloquear el botón de retroceso
        }

        // Si no se está subiendo imagen, permitir comportamiento normal
        super.onBackPressed();
    }

    // Método para cerrar sesión con integración completa
    private void cerrarSesion() {
        // 1. Obtener el ID del usuario actual antes de cerrar sesión
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserEmail = null;
        if (auth.getCurrentUser() != null) {
            currentUserEmail = auth.getCurrentUser().getEmail();
        }

        // 2. Marcar usuario como desconectado en Firestore
        if (currentUserEmail != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("usuarios")
                    .whereEqualTo("correo", currentUserEmail)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String userId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            UserSessionManager.getInstance().setUserDisconnected(userId);
                            Log.d(TAG, "Usuario Admin " + userId + " marcado como desconectado");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error al marcar usuario admin como desconectado: " + e.getMessage());
                    });
        }

        // 3. Mostrar mensaje
        Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();

        // 4. Cerrar sesión en Firebase
        auth.signOut();

        // 5. Limpiar SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        // 6. Redirigir al login
        Intent intent = new Intent(this, LoginFireBaseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

