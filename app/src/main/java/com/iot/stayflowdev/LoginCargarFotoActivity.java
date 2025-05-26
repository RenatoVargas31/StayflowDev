package com.iot.stayflowdev;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
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

public class LoginCargarFotoActivity extends AppCompatActivity {

    private ShapeableImageView profileImageView;
    private MaterialButton btnSelectPhoto;
    private MaterialButton btnOmitirFinalizar;
    private Uri selectedImageUri; // Para almacenar la URI de la imagen seleccionada

    // Launcher para abrir la galería y obtener un resultado
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Obtener la URI de la imagen seleccionada
                    selectedImageUri = result.getData().getData();

                    // Mostrar la imagen seleccionada en la vista previa
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

        // Inicializar vistas
        initializeViews();

        // Configurar listeners
        setupListeners();
    }

    private void initializeViews() {
        profileImageView = findViewById(R.id.profile_image);
        btnSelectPhoto = findViewById(R.id.btn_select_photo);
        btnOmitirFinalizar = findViewById(R.id.btn_omitir_finalizar);
    }

    private void setupListeners() {
        // Configurar onClick para seleccionar foto
        btnSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Configurar onClick para omitir y finalizar
        btnOmitirFinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes ir a la siguiente actividad o finalizar el proceso de registro
                Toast.makeText(LoginCargarFotoActivity.this, "Registro completado", Toast.LENGTH_SHORT).show();
                // Navegar a la pantalla principal o finalizar el registro según corresponda
                finishRegistration();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void finishRegistration() {
        // Aquí puedes guardar la URI de la imagen seleccionada si es necesario

        // Por ahora, simplemente finalizar la actividad o navegar a la pantalla principal
        // Ejemplo: Intent intent = new Intent(this, MainActivity.class);
        // startActivity(intent);
        // finishAffinity(); // Cierra todas las actividades anteriores

        // Para este ejemplo, simplemente mostramos un mensaje
        Toast.makeText(this, "¡Registro completado con éxito!", Toast.LENGTH_LONG).show();
    }
}

