package com.iot.stayflowdev.superAdmin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.R;

public class HotelDetailActivity extends AppCompatActivity {

    private static final String TAG = "HotelDetailActivity";
    public static final String EXTRA_HOTEL_ID = "HOTEL_ID";
    public static final String EXTRA_HOTEL_NAME = "HOTEL_NAME";

    private String hotelId;
    private String hotelName;

    // Views
    private MaterialToolbar toolbar;
    private ImageView imageViewHotel;
    private TextView textViewHotelName;
    private TextView textViewLocation;
    private TextView textViewAdmin;
    private TextView textViewDescription;
    private MaterialButton buttonViewReports;
    private CircularProgressIndicator progressIndicator;

    // Firebase
    private FirebaseFirestore db;

    public static void start(Context context, String hotelId, String hotelName) {
        Intent intent = new Intent(context, HotelDetailActivity.class);
        intent.putExtra(EXTRA_HOTEL_ID, hotelId);
        intent.putExtra(EXTRA_HOTEL_NAME, hotelName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_detail);

        // Obtener datos del intent
        hotelId = getIntent().getStringExtra(EXTRA_HOTEL_ID);
        hotelName = getIntent().getStringExtra(EXTRA_HOTEL_NAME);

        if (hotelId == null || hotelName == null) {
            Log.e(TAG, "Datos del hotel no proporcionados");
            finish();
            return;
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupClickListeners();
        loadHotelDetails();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imageViewHotel = findViewById(R.id.imageViewHotel);
        textViewHotelName = findViewById(R.id.textViewHotelName);
        textViewLocation = findViewById(R.id.textViewLocation);
        textViewAdmin = findViewById(R.id.textViewAdmin);
        textViewDescription = findViewById(R.id.textViewDescription);
        buttonViewReports = findViewById(R.id.buttonViewReports);
        progressIndicator = findViewById(R.id.progressIndicator);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        buttonViewReports.setOnClickListener(v -> {
            // Navegar al flujo de reportes existente
            FilterReportActivity.start(this, hotelName, hotelId);
        });
    }

    private void loadHotelDetails() {
        progressIndicator.setVisibility(View.VISIBLE);
        Log.d(TAG, "Cargando detalles del hotel: " + hotelId);

        db.collection("hoteles")
                .document(hotelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Documento del hotel encontrado");

                        // Obtener datos del hotel
                        String nombre = documentSnapshot.getString("nombre");
                        String descripcion = documentSnapshot.getString("descripcion");
                        String ubicacion = documentSnapshot.getString("ubicacion");
                        String administradorId = documentSnapshot.getString("administradorAsignado");

                        // Mostrar información básica
                        textViewHotelName.setText(nombre != null ? nombre : hotelName);
                        textViewLocation.setText(ubicacion != null ? ubicacion : "Ubicación no disponible");
                        textViewDescription.setText(descripcion != null ? descripcion : "Sin descripción disponible");

                        // Cargar información del administrador
                        if (administradorId != null && !administradorId.isEmpty()) {
                            loadAdministratorInfo(administradorId);
                        } else {
                            textViewAdmin.setText("Sin administrador asignado");
                        }

                        // Cargar imagen del hotel
                        loadHotelImage();

                    } else {
                        Log.w(TAG, "El documento del hotel no existe");
                        Toast.makeText(this, "No se encontró información del hotel", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    progressIndicator.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar detalles del hotel", e);
                    Toast.makeText(this, "Error al cargar información del hotel: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    progressIndicator.setVisibility(View.GONE);
                });
    }

    private void loadAdministratorInfo(String adminId) {
        Log.d(TAG, "Cargando información del administrador: " + adminId);

        db.collection("usuarios")
                .document(adminId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Obtener nombres y apellidos
                        String nombres = documentSnapshot.getString("nombres");
                        String apellidos = documentSnapshot.getString("apellidos");

                        // Si no encuentra, intentar con campos alternativos
                        if (nombres == null) nombres = documentSnapshot.getString("nombre");
                        if (apellidos == null) apellidos = documentSnapshot.getString("apellido");

                        String nombreCompleto = "";
                        if (nombres != null) nombreCompleto += nombres;
                        if (apellidos != null) {
                            if (!nombreCompleto.isEmpty()) nombreCompleto += " ";
                            nombreCompleto += apellidos;
                        }

                        if (nombreCompleto.trim().isEmpty()) {
                            nombreCompleto = "Administrador #" + adminId.substring(0, Math.min(5, adminId.length()));
                        }

                        textViewAdmin.setText(nombreCompleto);
                        Log.d(TAG, "Información del administrador cargada: " + nombreCompleto);

                    } else {
                        textViewAdmin.setText("Administrador no encontrado");
                        Log.w(TAG, "Documento del administrador no existe: " + adminId);
                    }
                })
                .addOnFailureListener(e -> {
                    textViewAdmin.setText("Error al cargar administrador");
                    Log.e(TAG, "Error al cargar información del administrador", e);
                });
    }

    private void loadHotelImage() {
        Log.d(TAG, "Cargando imagen del hotel desde galería");

        db.collection("hoteles")
                .document(hotelId)
                .collection("galeria")
                .limit(1) // Solo obtener la primera imagen
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Obtener la primera imagen de la galería
                        String imageUrl = queryDocumentSnapshots.getDocuments().get(0).getString("url");

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Log.d(TAG, "Imagen encontrada: " + imageUrl);

                            // Cargar la imagen con Glide
                            Glide.with(this)
                                    .load(imageUrl)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(R.drawable.ic_apartment)
                                    .error(R.drawable.ic_apartment)
                                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                            Log.e(TAG, "Error cargando imagen del hotel: " + e.getMessage());
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                            Log.d(TAG, "Imagen cargada exitosamente");
                                            return false;
                                        }
                                    })
                                    .into(imageViewHotel);
                        } else {
                            Log.w(TAG, "URL de imagen vacía");
                        }
                    } else {
                        Log.w(TAG, "No se encontraron imágenes en la galería del hotel");
                        // Mantener imagen por defecto
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al consultar galería del hotel: " + e.getMessage());
                    // Mantener imagen por defecto en caso de error
                });
    }
}
