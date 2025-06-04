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

import java.util.regex.Pattern;

public class LoginDriverRegister extends AppCompatActivity {

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
                    // Navegar a la pantalla de ingreso de correo
                    Intent intent = new Intent(LoginDriverRegister.this, LoginIngCorreoActivity.class);
                    intent.putExtra("esRegistroTaxista", true);
                    // Pasar los datos del vehículo
                    intent.putExtra("placa", etPlaca.getText().toString().trim());
                    intent.putExtra("modelo", etModelo.getText().toString().trim());
                    if (imageUri != null) {
                        intent.putExtra("imagenVehiculo", imageUri.toString());
                    }
                    startActivity(intent);
                }
            }
        });
    }

    private void recibirDatosUsuario() {
        // Recibir los datos del usuario del formulario anterior
        if (getIntent().hasExtra("nombre")) {
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
}
