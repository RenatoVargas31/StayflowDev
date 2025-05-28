package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.iot.stayflowdev.R;

public class GaleriaAdminActivity extends AppCompatActivity {

    private boolean hasImages = false;

    private LinearLayout emptyState;
    private LinearLayout scrollView;
    private ImageView image1, image2, image3, image4;

    private final ActivityResultLauncher<String> pickImages = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    loadImageIntoNextSlot(uri);
                    hasImages = true;
                    updateGalleryState();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galeria_admin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.menu_inicio);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_inicio) {
                startActivity(new Intent(this, AdminInicioActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_reportes) {
                startActivity(new Intent(this, ReportesAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_huesped) {
                startActivity(new Intent(this, HuespedAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_mensajeria) {
                startActivity(new Intent(this, MensajeriaAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_perfil) {
                startActivity(new Intent(this, PerfilAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        emptyState = findViewById(R.id.emptyState);
        scrollView = findViewById(R.id.scrollView);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image4 = findViewById(R.id.image4);

        findViewById(R.id.btnAddPhotos).setOnClickListener(v -> pickImages.launch("image/*"));

    }

    private void loadImageIntoNextSlot(Uri uri) {
        if (image1.getDrawable() == null) {
            image1.setImageURI(uri);
        } else if (image2.getDrawable() == null) {
            image2.setImageURI(uri);
        } else if (image3.getDrawable() == null) {
            image3.setImageURI(uri);
        } else if (image4.getDrawable() == null) {
            image4.setImageURI(uri);
        } else {
            showMessage("Ya alcanzaste el máximo de 4 imágenes");
        }
    }

    private void removeImage(ImageView imageView) {
        imageView.setImageDrawable(null);
        checkIfAnyImageLeft();
    }

    private void checkIfAnyImageLeft() {
        hasImages = image1.getDrawable() != null ||
                image2.getDrawable() != null ||
                image3.getDrawable() != null ||
                image4.getDrawable() != null;
        updateGalleryState();
    }

    private void updateGalleryState() {
        scrollView.setVisibility(hasImages ? LinearLayout.VISIBLE : LinearLayout.GONE);
        emptyState.setVisibility(hasImages ? LinearLayout.GONE : LinearLayout.VISIBLE);
    }

    private void confirmSave() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cambios guardados")
                .setMessage("La galería ha sido actualizada correctamente")
                .setPositiveButton("OK", null)
                .show();
    }

    private void confirmCancel() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("¿Descartar cambios?")
                .setMessage("Se perderán los cambios no guardados.")
                .setNegativeButton("Seguir editando", null)
                .setPositiveButton("Descartar", (dialog, which) -> finish())
                .show();
    }

    private void showMessage(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}
