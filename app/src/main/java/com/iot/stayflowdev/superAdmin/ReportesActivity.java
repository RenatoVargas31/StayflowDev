package com.iot.stayflowdev.superAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.Hotel;
import com.iot.stayflowdev.superAdmin.adapter.HotelAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportesActivity extends BaseSuperAdminActivity {

    private static final String TAG = "ReportesActivity";
    public static final String EXTRA_HOTEL_NAME = "HOTEL_NAME";
    public static final String EXTRA_HOTEL_ID = "HOTEL_ID";
    private static final int REQUEST_ADD_HOTEL = 200;

    private RecyclerView recyclerViewHotels;
    private HotelAdapter hotelAdapter;
    private List<Hotel> hotelList;
    private Map<String, String> adminNamesMap;
    private EditText searchEditText;
    private CircularProgressIndicator progressIndicator;

    // Firebase
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // El layout ya se configura en BaseSuperAdminActivity

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        setupSearch();
        setupAddHotelButton();
        loadHotelsFromFirestore();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.superadmin_reportes_superadmin;
    }

    @Override
    protected int getBottomNavigationSelectedItem() {
        return R.id.nav_reportes;
    }

    @Override
    protected String getToolbarTitle() {
        return "Reportes";
    }

    private void initViews() {
        recyclerViewHotels = findViewById(R.id.recyclerViewHotels);
        searchEditText = findViewById(R.id.searchEditText);
        progressIndicator = findViewById(R.id.progressIndicator);

        if (progressIndicator == null) {
            // Si no existe en el layout, lo inicializamos programáticamente
            progressIndicator = new CircularProgressIndicator(this);
            progressIndicator.setIndeterminate(true);
            // Añadir al layout según sea necesario
        }
    }

    private void setupRecyclerView() {
        recyclerViewHotels.setLayoutManager(new LinearLayoutManager(this));
        hotelList = new ArrayList<>();
        adminNamesMap = new HashMap<>();

        hotelAdapter = new HotelAdapter(hotelList, this::navigateToFilterView);
        recyclerViewHotels.setAdapter(hotelAdapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hotelAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadHotelsFromFirestore() {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "Iniciando carga de hoteles desde Firestore...");

        // Limpiar listas anteriores
        hotelList.clear();
        adminNamesMap.clear();

        // Cargar hoteles de Firestore
        db.collection("hoteles")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Consulta de hoteles exitosa, documentos encontrados: " + task.getResult().size());
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "Procesando hotel ID: " + document.getId() + ", datos: " + document.getData());
                            try {
                                Hotel hotel = document.toObject(Hotel.class);
                                hotelList.add(hotel);
                                Log.d(TAG, "Hotel añadido a la lista: " + hotel.getNombre() + ", ID: " + hotel.getId());

                                // Si hay un administrador asignado, obtener su nombre
                                if (hotel.getAdministradorAsignado() != null && !hotel.getAdministradorAsignado().isEmpty()) {
                                    Log.d(TAG, "El hotel tiene administrador asignado: " + hotel.getAdministradorAsignado());
                                    loadAdminName(hotel.getAdministradorAsignado());
                                } else {
                                    Log.d(TAG, "El hotel NO tiene administrador asignado");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error al convertir documento a Hotel: " + e.getMessage());
                            }
                        }

                        // Actualizar el adaptador con la nueva lista de hoteles
                        hotelAdapter.updateHotels(hotelList);

                        Log.d(TAG, "Lista de hoteles actualizada, tamaño: " + hotelList.size());
                    } else {
                        Log.w(TAG, "Error al cargar hoteles", task.getException());
                        Toast.makeText(this, "Error al cargar hoteles: " +
                                (task.getException() != null ? task.getException().getMessage() : "Error desconocido"),
                                Toast.LENGTH_SHORT).show();
                    }

                    if (progressIndicator != null) {
                        progressIndicator.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error completo al cargar hoteles", e);
                    Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (progressIndicator != null) {
                        progressIndicator.setVisibility(View.GONE);
                    }
                });
    }

    private void loadAdminName(String adminId) {
        Log.d(TAG, "Cargando información del administrador ID: " + adminId);
        db.collection("usuarios")
                .document(adminId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Documento del administrador encontrado: " + documentSnapshot.getId());

                        // Primero intentar con campos nombres/apellidos
                        String nombres = documentSnapshot.getString("nombres");
                        String apellidos = documentSnapshot.getString("apellidos");

                        // Si no encuentra, intentar con nombre/apellidos
                        if (nombres == null) nombres = documentSnapshot.getString("nombre");
                        if (apellidos == null) apellidos = documentSnapshot.getString("apellido");

                        Log.d(TAG, "Datos del administrador: nombres=" + nombres + ", apellidos=" + apellidos);

                        String nombreCompleto = (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
                        nombreCompleto = nombreCompleto.trim();
                        if (nombreCompleto.isEmpty()) {
                            nombreCompleto = "Administrador #" + adminId.substring(0, Math.min(5, adminId.length()));
                            Log.d(TAG, "Usando nombre genérico para administrador: " + nombreCompleto);
                        }

                        adminNamesMap.put(adminId, nombreCompleto);
                        Log.d(TAG, "Nombre de administrador guardado: " + nombreCompleto);
                        hotelAdapter.setAdminNamesMap(adminNamesMap);
                    } else {
                        Log.w(TAG, "El documento del administrador no existe: " + adminId);
                        adminNamesMap.put(adminId, "Admin desconocido");
                        hotelAdapter.setAdminNamesMap(adminNamesMap);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos del administrador: " + adminId, e);
                    adminNamesMap.put(adminId, "Error al cargar admin");
                    hotelAdapter.setAdminNamesMap(adminNamesMap);
                });
    }

    private void setupAddHotelButton() {
        com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabAddHotel = findViewById(R.id.fabAddHotel);
        if (fabAddHotel != null) {
            fabAddHotel.setOnClickListener(v -> {
                Intent intent = new Intent(ReportesActivity.this, AddHotelActivity.class);
                startActivityForResult(intent, REQUEST_ADD_HOTEL);
            });
        } else {
            Log.e(TAG, "Error: No se encontró el botón fabAddHotel");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_HOTEL && resultCode == RESULT_OK) {
            // Recargar la lista de hoteles cuando se agrega uno nuevo
            loadHotelsFromFirestore();
        }
    }

    private void navigateToFilterView(Hotel hotel) {
        Intent intent = new Intent(ReportesActivity.this, FilterReportActivity.class);
        intent.putExtra(EXTRA_HOTEL_NAME, hotel.getNombre());
        intent.putExtra(EXTRA_HOTEL_ID, hotel.getId());
        startActivity(intent);
    }
}