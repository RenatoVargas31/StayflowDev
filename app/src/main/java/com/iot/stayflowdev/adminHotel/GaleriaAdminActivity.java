package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.repository.AdminHotelViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GaleriaAdminActivity extends AppCompatActivity {

    private LinearLayout galleryContainer, emptyState;
    private MaterialButton btnGuardarGaleria;
    private android.widget.GridLayout gridPreview;

    private final int MAX_IMAGES = 10;
    private String hotelId;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseFirestore db;
    private CollectionReference galeriaRef;

    private final Set<Uri> imagenesSeleccionadas = new LinkedHashSet<>();
    private final List<String[]> urlsExistentes = new ArrayList<>();
    private int imagenesSubidasExistentes = 0;

    private final ActivityResultLauncher<String> pickImages = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(),
            uris -> {
                if (uris != null && !uris.isEmpty()) {
                    imagenesSeleccionadas.addAll(uris);
                    renderizarGaleriaCompleta();
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
            } else if (id == R.id.menu_reportes) {
                startActivity(new Intent(this, ReportesAdminActivity.class));
            } else if (id == R.id.menu_huesped) {
                startActivity(new Intent(this, HuespedAdminActivity.class));
            } else if (id == R.id.menu_mensajeria) {
                startActivity(new Intent(this, MensajeriaAdminActivity.class));
            } else if (id == R.id.menu_perfil) {
                startActivity(new Intent(this, PerfilAdminActivity.class));
            }
            overridePendingTransition(0, 0);
            return true;
        });

        emptyState = findViewById(R.id.emptyState);
        galleryContainer = findViewById(R.id.galleryContainer);
        gridPreview = findViewById(R.id.gridPreview);
        btnGuardarGaleria = findViewById(R.id.btnGuardarGaleria);

        AdminHotelViewModel viewModel = new ViewModelProvider(this).get(AdminHotelViewModel.class);
        viewModel.getHotelId().observe(this, id -> {
            if (id != null) {
                hotelId = id;
                inicializarFirebasePorHotel(id);
                cargarImagenesExistentes();
            }
        });

        findViewById(R.id.btnAddPhotos).setOnClickListener(v -> pickImages.launch("image/*"));

        btnGuardarGaleria.setOnClickListener(v -> {
            if (imagenesSubidasExistentes + imagenesSeleccionadas.size() < 4) {
                Toast.makeText(this, "Debes tener al menos 4 imágenes en total", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!imagenesSeleccionadas.isEmpty()) {
                subirGaleriaCompleta();
            } else {
                Toast.makeText(this, "Debes añadir imágenes nuevas para guardar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hotelId != null) {
            cargarImagenesExistentes();
        }
    }

    private void inicializarFirebasePorHotel(String id) {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("galeria_hoteles").child(id);
        db = FirebaseFirestore.getInstance();
        galeriaRef = db.collection("hoteles").document(id).collection("galeria");
    }

    private void cargarImagenesExistentes() {
        urlsExistentes.clear();
        galeriaRef.get().addOnSuccessListener(query -> {
            imagenesSubidasExistentes = 0;
            if (!query.isEmpty()) {
                for (QueryDocumentSnapshot doc : query) {
                    String url = doc.getString("url");
                    String path = doc.getString("path");
                    if (url != null && path != null) {
                        urlsExistentes.add(new String[]{url, path});
                        imagenesSubidasExistentes++;
                    }
                }
            }
            renderizarGaleriaCompleta();
            updateGalleryState();
        });
    }

    private void renderizarGaleriaCompleta() {
        gridPreview.removeAllViews();
        for (String[] entry : urlsExistentes) {
            agregarImagenView(entry[0], entry[1]);
        }
        mostrarGaleriaTemporal();
    }

    private void mostrarGaleriaTemporal() {
        for (Uri uri : imagenesSeleccionadas) {
            agregarImagenTemporal(uri);
        }
    }

    private void agregarImagenTemporal(Uri uri) {
        ImageView img = new ImageView(this);
        int size = (int) getResources().getDisplayMetrics().density * 140;
        android.widget.GridLayout.LayoutParams params = new android.widget.GridLayout.LayoutParams();
        params.width = size;
        params.height = size;
        params.setMargins(8, 8, 8, 8);
        img.setLayoutParams(params);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(this).load(uri).into(img);

        img.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar imagen")
                .setMessage("¿Deseas eliminar esta imagen antes de subirla?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    imagenesSeleccionadas.remove(uri);
                    renderizarGaleriaCompleta();
                })
                .show());

        gridPreview.addView(img);
        updateGalleryState();
        btnGuardarGaleria.setEnabled(!imagenesSeleccionadas.isEmpty());
    }

    private void subirGaleriaCompleta() {
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Subiendo galería")
                .setMessage("Por favor espera mientras subimos tus imágenes…")
                .setCancelable(false)
                .create();

        dialog.show();

        int total = imagenesSeleccionadas.size();
        int[] subidas = {0};

        for (Uri uri : new ArrayList<>(imagenesSeleccionadas)) {
            String nombreArchivo = "foto_" + System.currentTimeMillis() + ".jpg";
            StorageReference ref = storageRef.child(nombreArchivo);

            ref.putFile(uri).addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(url -> {
                Map<String, Object> data = new HashMap<>();
                data.put("url", url.toString());
                data.put("path", ref.getPath());

                galeriaRef.add(data).addOnSuccessListener(docRef -> {
                    subidas[0]++;
                    if (subidas[0] == total) {
                        dialog.dismiss();
                        Toast.makeText(this, "Galería guardada con éxito", Toast.LENGTH_SHORT).show();
                        imagenesSeleccionadas.clear();
                        gridPreview.removeAllViews();
                        btnGuardarGaleria.setEnabled(false);
                        cargarImagenesExistentes();
                    }
                });
            })).addOnFailureListener(e -> Toast.makeText(this, "Error al subir una imagen", Toast.LENGTH_SHORT).show());
        }
    }

    private void agregarImagenView(String imageUrl, String storagePath) {
        ImageView imageView = new ImageView(this);
        int sizeInDp = (int) getResources().getDisplayMetrics().density * 140;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInDp, sizeInDp);
        params.setMargins(0, 0, 0, 16);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(imageView);

        imageView.setOnClickListener(v -> {
            if (storagePath.isEmpty()) return;
            if (imagenesSubidasExistentes <= 4) {
                Toast.makeText(this, "La galería debe tener al menos 4 imágenes", Toast.LENGTH_SHORT).show();
            } else {
                confirmarEliminarImagen(imageView, storagePath);
            }
        });

        gridPreview.addView(imageView);
    }

    private void confirmarEliminarImagen(ImageView imageView, String storagePath) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar imagen")
                .setMessage("¿Deseas eliminar esta imagen de la galería?")
                .setPositiveButton("Eliminar", (dialog, which) -> storage.getReference().child(storagePath).delete()
                        .addOnSuccessListener(unused -> {
                            eliminarDocumentoDeFirestore(storagePath);
                            gridPreview.removeView(imageView);
                            imagenesSubidasExistentes--;
                            updateGalleryState();
                            Toast.makeText(this, "Imagen eliminada", Toast.LENGTH_SHORT).show();
                        }))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarDocumentoDeFirestore(String path) {
        galeriaRef.whereEqualTo("path", path).get().addOnSuccessListener(query -> {
            for (QueryDocumentSnapshot doc : query) {
                galeriaRef.document(doc.getId()).delete();
            }
        });
    }

    private void updateGalleryState() {
        boolean tieneImagenes = gridPreview.getChildCount() > 0;
        galleryContainer.setVisibility(tieneImagenes ? View.VISIBLE : View.GONE);
        emptyState.setVisibility(tieneImagenes ? View.GONE : View.VISIBLE);
    }
}
