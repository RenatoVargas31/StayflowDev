package com.iot.stayflowdev;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LoginDriverRegister extends AppCompatActivity {

    private static final String TAG = "LoginDriverRegister";

    private MaterialButton btnContinuarRegistroDriver;
    private MaterialButton btnCargarImagen;
    private ImageView imgPreviewVehicle;
    private TextInputEditText etPlaca;
    private TextInputEditText etModelo;
    private TextInputLayout tilPlaca;
    private TextInputLayout tilModelo;

    // Patrón para validar placas en Perú: 3 caracteres alfanuméricos, guion, 3 caracteres alfanuméricos (formato: XXX-XXX)
    private static final Pattern PLACA_PATTERN = Pattern.compile("^[A-Z0-9]{3}-[A-Z0-9]{3}$");

    private Uri imageUri;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String fotoVehiculoURL;

    // Launcher para abrir la galería y obtener un resultado
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Obtener la URI de la imagen seleccionada
                    imageUri = result.getData().getData();

                    // Mostrar la imagen seleccionada en la vista previa
                    if (imageUri != null) {
                        imgPreviewVehicle.setImageURI(imageUri);
                        imgPreviewVehicle.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        Toast.makeText(this, "Imagen del vehículo seleccionada", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_driver_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Inicializar vistas
        initViews();

        // Configurar listeners
        configurarListeners();

        // Recibir datos del formulario anterior
        recibirDatosUsuario();
    }

    private void initViews() {
        btnContinuarRegistroDriver = findViewById(R.id.btn_continuar_registro_driver);
        btnCargarImagen = findViewById(R.id.btn_cargar_imagen);
        imgPreviewVehicle = findViewById(R.id.img_preview_vehicle);
        etPlaca = findViewById(R.id.et_placa);
        etModelo = findViewById(R.id.et_modelo);
        tilPlaca = findViewById(R.id.til_placa);
        tilModelo = findViewById(R.id.til_modelo);
    }

    private void configurarListeners() {
        // Listener para el botón de cargar imagen
        btnCargarImagen.setOnClickListener(v -> {
            abrirGaleria();
        });

        // Validación en tiempo real para la placa
        etPlaca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Si la longitud es 3, automaticamente insertar el guión
                if (s.length() == 3 && before == 0 && !s.toString().contains("-")) {
                    etPlaca.setText(s + "-");
                    etPlaca.setSelection(4);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                validarPlaca();
            }
        });

        // Validación en tiempo real para el modelo
        etModelo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validarModelo();
            }
        });

        // Configurar listener del botón continuar
        btnContinuarRegistroDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarFormulario()) {
                    // Subir la imagen a Firebase Storage
                    subirImagenAFirebase();
                }
            }
        });
    }

    private void recibirDatosUsuario() {
        // Recibir los datos del usuario del formulario anterior
        if (getIntent().hasExtra("nombres") && getIntent().hasExtra("apellidos")) {
            // Si hay datos en el intent, son datos del usuario que ya se validaron
            // Aquí podemos guardarlos para enviarlos junto con los datos del vehículo
            // al siguiente paso del registro
        }
    }

    private boolean validarFormulario() {
        boolean isPlacaValida = validarPlaca();
        boolean isModeloValido = validarModelo();
        boolean isImagenCargada = validarImagen();

        return isPlacaValida && isModeloValido && isImagenCargada;
    }

    private boolean validarPlaca() {
        String placa = etPlaca.getText().toString().trim();

        if (TextUtils.isEmpty(placa)) {
            tilPlaca.setError("La placa es obligatoria");
            return false;
        }

        if (!PLACA_PATTERN.matcher(placa).matches()) {
            tilPlaca.setError("Formato incorrecto. Ejemplo: ABC-123");
            return false;
        }

        tilPlaca.setError(null);
        return true;
    }

    private boolean validarModelo() {
        String modelo = etModelo.getText().toString().trim();

        if (TextUtils.isEmpty(modelo)) {
            tilModelo.setError("El modelo es obligatorio");
            return false;
        }

        if (modelo.length() > 25) {
            tilModelo.setError("El modelo no puede exceder los 25 caracteres");
            return false;
        }

        tilModelo.setError(null);
        return true;
    }

    private boolean validarImagen() {
        if (imageUri == null) {
            Toast.makeText(this, "Debes cargar una foto de tu vehículo", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void subirImagenAFirebase() {
        if (imageUri != null) {
            // Deshabilitar botón para evitar múltiples clics
            btnContinuarRegistroDriver.setEnabled(false);

            // Mostrar mensaje de carga
            Toast.makeText(this, "Subiendo imagen y guardando datos...", Toast.LENGTH_SHORT).show();

            // Usar la placa como nombre para la imagen en Storage
            String placa = etPlaca.getText().toString().trim();
            StorageReference imagenRef = storageRef.child("fotos_vehiculo/" + placa + ".jpg");

            imagenRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imagenRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        fotoVehiculoURL = uri.toString();
                        guardarDatosEnFirestore();
                    }))
                    .addOnFailureListener(e -> {
                        // Habilitar el botón nuevamente
                        btnContinuarRegistroDriver.setEnabled(true);
                        Log.e(TAG, "Error al subir la imagen", e);
                        Toast.makeText(LoginDriverRegister.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void guardarDatosEnFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Crear el objeto con los datos del vehículo
            Map<String, Object> vehiculo = new HashMap<>();
            vehiculo.put("driverId", userId);
            vehiculo.put("placa", etPlaca.getText().toString().trim());
            vehiculo.put("modelo", etModelo.getText().toString().trim());
            vehiculo.put("fotoVehiculo", fotoVehiculoURL);
            vehiculo.put("activo", false); // Por defecto en false

            // Guardar en Firestore en la colección vehiculo
            db.collection("vehiculo").document(etPlaca.getText().toString().trim())
                    .set(vehiculo)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Datos del vehículo guardados correctamente");
                        // Habilitar el botón nuevamente
                        btnContinuarRegistroDriver.setEnabled(true);
                        navegarACrearPass();
                    })
                    .addOnFailureListener(e -> {
                        // Habilitar el botón nuevamente
                        btnContinuarRegistroDriver.setEnabled(true);
                        Log.e(TAG, "Error al guardar los datos del vehículo", e);
                        Toast.makeText(LoginDriverRegister.this, "Error al guardar los datos del vehículo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void navegarACrearPass() {
        // Navegar a la pantalla de creación de contraseña
        Intent intent = new Intent(LoginDriverRegister.this, LoginCrearPassActivity.class);

        // Pasar los datos del vehículo (ahora solo para referencia, ya están guardados en Firestore)
        intent.putExtra("placa", etPlaca.getText().toString().trim());
        intent.putExtra("modelo", etModelo.getText().toString().trim());

        // Mantener los datos del usuario que vinieron del formulario anterior
        if (getIntent().hasExtra("nombres")) {
            intent.putExtra("nombres", getIntent().getStringExtra("nombres"));
        }
        if (getIntent().hasExtra("apellidos")) {
            intent.putExtra("apellidos", getIntent().getStringExtra("apellidos"));
        }
        if (getIntent().hasExtra("numeroDocumento")) {
            intent.putExtra("numeroDocumento", getIntent().getStringExtra("numeroDocumento"));
        }
        if (getIntent().hasExtra("tipoDocumento")) {
            intent.putExtra("tipoDocumento", getIntent().getStringExtra("tipoDocumento"));
        }
        if (getIntent().hasExtra("fechaNacimiento")) {
            intent.putExtra("fechaNacimiento", getIntent().getStringExtra("fechaNacimiento"));
        }
        if (getIntent().hasExtra("telefono")) {
            intent.putExtra("telefono", getIntent().getStringExtra("telefono"));
        }
        if (getIntent().hasExtra("domicilio")) {
            intent.putExtra("domicilio", getIntent().getStringExtra("domicilio"));
        }
        if (getIntent().hasExtra("email")) {
            intent.putExtra("email", getIntent().getStringExtra("email"));
        }

        // Marcar que este es un registro de taxista
        intent.putExtra("esRegistroTaxista", true);

        Toast.makeText(this, "¡Datos del vehículo guardados correctamente!", Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }
}

