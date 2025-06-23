package com.iot.stayflowdev;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class LoginCargarFotoActivity extends AppCompatActivity {

    private static final String TAG = "LoginCargarFoto";

    private ShapeableImageView profileImageView;
    private MaterialButton btnSelectPhoto;
    private MaterialButton btnFinalizar; // Cambiado de btnOmitirFinalizar a btnFinalizar
    private ImageView btnClose;
    private Uri selectedImageUri;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private String nombres, apellidos, numeroDocumento, tipoDocumento, fechaNacimiento, telefono, domicilio;
    private String placa, modelo, imagenVehiculo;
    private boolean esRegistroTaxista = false;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();

                    if (selectedImageUri != null) {
                        profileImageView.setImageURI(selectedImageUri);
                        profileImageView.setScaleType(ShapeableImageView.ScaleType.CENTER_CROP);
                        Toast.makeText(this, "Foto de perfil seleccionada", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_cargar_foto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        recibirDatos();
        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        profileImageView = findViewById(R.id.profile_image);
        btnSelectPhoto = findViewById(R.id.btn_select_photo);
        btnFinalizar = findViewById(R.id.btn_finalizar); // Cambiado de btnOmitirFinalizar a btnFinalizar
        btnClose = findViewById(R.id.btn_close);

        btnClose.setOnClickListener(v -> {
            Toast.makeText(this, "Si cierras la aplicación, tendrás que comenzar el registro nuevamente",
                    Toast.LENGTH_LONG).show();
        });
    }

    private void recibirDatos() {
        Intent intent = getIntent();

        if (intent.hasExtra("nombres")) {
            nombres = intent.getStringExtra("nombres");
        }
        if (intent.hasExtra("apellidos")) {
            apellidos = intent.getStringExtra("apellidos");
        }
        if (intent.hasExtra("numeroDocumento")) {
            numeroDocumento = intent.getStringExtra("numeroDocumento");
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

        if (intent.hasExtra("placa")) {
            placa = intent.getStringExtra("placa");
        }
        if (intent.hasExtra("modelo")) {
            modelo = intent.getStringExtra("modelo");
        }
        if (intent.hasExtra("imagenVehiculo")) {
            imagenVehiculo = intent.getStringExtra("imagenVehiculo");
        }

        if (intent.hasExtra("esRegistroTaxista")) {
            esRegistroTaxista = intent.getBooleanExtra("esRegistroTaxista", false);
        }
    }

    private void setupListeners() {
        btnSelectPhoto.setOnClickListener(v -> openGallery());

        btnFinalizar.setOnClickListener(v -> finishRegistration());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void finishRegistration() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar que se haya seleccionado una imagen
        if (selectedImageUri == null) {
            Toast.makeText(this, "Por favor, selecciona una foto de perfil para continuar", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Finalizando registro...", Toast.LENGTH_SHORT).show();

        // Guardar datos en Firestore
        guardarDatosUsuarioFirestore(currentUser.getUid());
    }

    private void guardarDatosUsuarioFirestore(String userId) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e(TAG, "No hay usuario autenticado");
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("nombres", nombres);
        userData.put("apellidos", apellidos);
        userData.put("numeroDocumento", numeroDocumento);
        userData.put("tipoDocumento", tipoDocumento);
        userData.put("fechaNacimiento", fechaNacimiento);
        userData.put("telefono", telefono);
        userData.put("domicilio", domicilio);
        userData.put("correo", user.getEmail());

        // Asignar el rol según el tipo de registro
        String rol = esRegistroTaxista ? "driver" : "usuario";
        userData.put("rol", rol);

        // Campo estado con diferente valor según tipo de usuario
        if (esRegistroTaxista) {
            userData.put("estado", true); // Campo de estado como booleano (true por defecto)
            userData.put("verificado", false); // Campo de verificación para drivers
            userData.put("activo", false); // Campo de activación para drivers

            // Si es taxista, añadir referencia a la placa del vehículo
            if (placa != null && !placa.isEmpty()) {
                userData.put("placa", placa);
            }
        } else {
            userData.put("estado", true); // Para usuarios regulares (también booleano)
        }

        // Si seleccionó una imagen de perfil, subirla a Firebase Storage y guardar la URL
        if (selectedImageUri != null) {
            StorageReference profileImageRef = storageRef.child("fotos_perfil/" + userId + ".jpg");
            profileImageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                userData.put("imagenPerfilURL", uri.toString());
                                // Ya no necesitamos subir la foto del vehículo aquí porque se hizo en LoginDriverRegister
                                guardarDatosEnFirestore(userId, userData, rol);
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Error al obtener URL de la imagen", e);
                                Toast.makeText(LoginCargarFotoActivity.this,
                                        "Error al finalizar el registro: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }))
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error al subir imagen a Firebase Storage", e);
                        Toast.makeText(LoginCargarFotoActivity.this,
                                "Error al finalizar el registro: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            guardarDatosEnFirestore(userId, userData, rol);
        }
    }

    private void guardarDatosEnFirestore(String userId, Map<String, Object> userData, String rol) {
        // Si es taxista, añadir datos del vehículo
        if (esRegistroTaxista) {
            userData.put("placa", placa);
            userData.put("modelo", modelo);

            if (imagenVehiculo != null) {
                userData.put("imagenVehiculoURI", imagenVehiculo);
            }
        }

        // Guardar en Firestore
        DocumentReference userRef = db.collection("usuarios").document(userId);
        userRef.set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Datos de usuario guardados en Firestore");

                    // Registro completado, redirigir según el rol
                    redirigirSegunRol(rol);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al guardar datos en Firestore", e);
                    Toast.makeText(LoginCargarFotoActivity.this,
                            "Error al finalizar el registro: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void redirigirSegunRol(String rol) {
        Intent intent;

        switch (rol.toLowerCase()) {
            case "driver":
                Toast.makeText(this, "¡Registro de taxista completado con éxito!", Toast.LENGTH_SHORT).show();
                intent = new Intent(this, com.iot.stayflowdev.Driver.Activity.DriverInicioActivity.class);
                break;
            case "usuario":
            default:
                Toast.makeText(this, "¡Registro completado con éxito!", Toast.LENGTH_SHORT).show();
                intent = new Intent(this, com.iot.stayflowdev.cliente.ClientePerfilActivity.class);
                break;
        }

        // Limpiar actividades anteriores para que no pueda volver atrás
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
