package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.repository.AdminHotelViewModel;
import com.iot.stayflowdev.databinding.ActivityUbicacionAdminBinding;
import com.iot.stayflowdev.model.LugaresCercanos;
import com.iot.stayflowdev.viewmodels.LugaresCercanosViewModel;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class UbicacionAdminActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityUbicacionAdminBinding binding;
    private AdminHotelViewModel adminHotelViewModel;
    private LugaresCercanosViewModel lugaresViewModel;
    private String hotelIdGlobal = null;

    //PARA UBICACION
    private GoogleMap mMap;
    private LatLng ubicacionHotel = null;
    private String direccionExactaFinal = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUbicacionAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        lugaresViewModel = new ViewModelProvider(this).get(LugaresCercanosViewModel.class);
        this.adminHotelViewModel = new ViewModelProvider(this).get(AdminHotelViewModel.class);

        adminHotelViewModel.getHotelId().observe(this, idHotel -> {
            if (idHotel != null && !idHotel.isEmpty()) {
                hotelIdGlobal = idHotel;
                lugaresViewModel.cargarLugares(idHotel);
                observarLugares();
                cargarDireccionHotel(idHotel);
            } else {
                Toast.makeText(this, "No hay hotel asignado", Toast.LENGTH_SHORT).show();
            }
        });

        // Configura Toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        // Configura BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.menu_inicio);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_inicio) return true;

            if (id == R.id.menu_reportes) {
                startActivity(new Intent(this, ReportesAdminActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else if (id == R.id.menu_huesped) {
                startActivity(new Intent(this, HuespedAdminActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else if (id == R.id.menu_mensajeria) {
                startActivity(new Intent(this, MensajeriaAdminActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else if (id == R.id.menu_perfil) {
                startActivity(new Intent(this, PerfilAdminActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }

            overridePendingTransition(0, 0);
            return true;
        });

        // Inicializar el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setupListeners();
    }

    private void cargarDireccionHotel(String idHotel) {
        adminHotelViewModel.getUbicacionHotel().observe(this, ubicacion -> {
            if (ubicacion == null || ubicacion.trim().isEmpty()) {
                binding.chipDireccion.setText("Dirección no registrada");
                binding.chipDireccion.setVisibility(View.VISIBLE);
            } else {
                binding.chipDireccion.setText(ubicacion);
                binding.chipDireccion.setVisibility(View.VISIBLE);
            }
        });

        // Obtener también coordenadas desde Firestore y mostrarlas en el mapa
        FirebaseFirestore.getInstance().collection("hoteles")
                .document(idHotel)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("geoposicion")) {
                        GeoPoint geo = documentSnapshot.getGeoPoint("geoposicion");
                        if (geo != null && mMap != null) {
                            ubicacionHotel = new LatLng(geo.getLatitude(), geo.getLongitude());
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions()
                                    .position(ubicacionHotel)
                                    .title("Ubicación del hotel"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionHotel, 17f));
                        }
                    }
                });
    }


    private void observarLugares() {
        lugaresViewModel.getLugares().observe(this, lista -> {
            binding.layoutListaLugares.removeAllViews();
            for (LugaresCercanos lugar : lista) {
                addPlaceCard(lugar);
            }
        });
    }

    private void setupListeners() {
        binding.btnAgregarLugar.setOnClickListener(v -> showFormDialog());
        binding.btnActualizarDireccion.setOnClickListener(v -> {
            String direccionEscrita = binding.etDireccionHotel.getText().toString().trim();

            if (!direccionEscrita.isEmpty() && hotelIdGlobal != null) {
                buscarYMostrarEnMapaYGuardar(direccionEscrita);
            } else {
                showMessage("Completa la dirección");
            }
        });


    }

    private void showFormDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_form_lugar, null);

        final EditText etNombre = view.findViewById(R.id.etDialogNombreLugar);
        final EditText etDistancia = view.findViewById(R.id.etDialogDistanciaLugar);

        new AlertDialog.Builder(this)
                .setTitle("Agregar lugar")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String distancia = etDistancia.getText().toString().trim();

                    if (!nombre.isEmpty() && !distancia.isEmpty() && hotelIdGlobal != null) {
                        LugaresCercanos nuevo = new LugaresCercanos(null, distancia, "", nombre);
                        lugaresViewModel.agregarLugar(hotelIdGlobal, nuevo, () -> {
                            showMessage("Lugar agregado");
                            lugaresViewModel.cargarLugares(hotelIdGlobal);
                        });
                    } else {
                        showMessage("Completa todos los campos.");
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showEditDialog(LugaresCercanos lugar) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_form_lugar, null);

        EditText etNombre = view.findViewById(R.id.etDialogNombreLugar);
        EditText etDistancia = view.findViewById(R.id.etDialogDistanciaLugar);

        etNombre.setText(lugar.getNombre());
        etDistancia.setText(lugar.getDistancia());

        new AlertDialog.Builder(this)
                .setTitle("Editar lugar")
                .setView(view)
                .setPositiveButton("Actualizar", (dialog, which) -> {
                    String nuevoNombre = etNombre.getText().toString().trim();
                    String nuevaDistancia = etDistancia.getText().toString().trim();

                    if (!nuevoNombre.isEmpty() && !nuevaDistancia.isEmpty()) {
                        LugaresCercanos actualizado = new LugaresCercanos(lugar.getId(), nuevaDistancia, "", nuevoNombre);
                        lugaresViewModel.actualizarLugar(hotelIdGlobal, actualizado, () -> {
                            showMessage("Lugar actualizado");
                            lugaresViewModel.cargarLugares(hotelIdGlobal);
                        });
                    } else {
                        showMessage("Completa todos los campos");
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void addPlaceCard(LugaresCercanos lugar) {
        View card = LayoutInflater.from(this)
                .inflate(R.layout.item_admin_lugar_historico, binding.layoutListaLugares, false);

        ((android.widget.TextView) card.findViewById(R.id.tvNombreLugar)).setText(lugar.getNombre());
        ((Chip) card.findViewById(R.id.chipDistancia)).setText(lugar.getDistancia() + " m");

        // Botón editar
        card.findViewById(R.id.btnEditar).setOnClickListener(v -> showEditDialog(lugar));

        // Botón eliminar
        card.findViewById(R.id.btnEliminar).setOnClickListener(v -> {
            if (hotelIdGlobal != null) {
                lugaresViewModel.eliminarLugar(hotelIdGlobal, lugar.getId(), () -> {
                    showMessage("Lugar eliminado");
                    lugaresViewModel.cargarLugares(hotelIdGlobal);
                });
            }
        });

        binding.layoutListaLugares.addView(card);
    }
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void buscarYMostrarEnMapaYGuardar(String direccion) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> resultados = geocoder.getFromLocationName(direccion, 1);
            if (!resultados.isEmpty()) {
                Address address = resultados.get(0);
                ubicacionHotel = new LatLng(address.getLatitude(), address.getLongitude());
                direccionExactaFinal = address.getAddressLine(0);

                // Mostrar en el mapa
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(ubicacionHotel).title("Ubicación del hotel"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacionHotel, 17f));

                // Mostrar visualmente
                binding.chipDireccion.setText(direccionExactaFinal);
                binding.chipDireccion.setVisibility(View.VISIBLE);

                // Guardar en Firestore
                adminHotelViewModel.actualizarUbicacionConCoordenadas(hotelIdGlobal, direccionExactaFinal, ubicacionHotel,
                        () -> showMessage("Dirección y coordenadas actualizadas"),
                        () -> showMessage("Error al actualizar ubicación")
                );

            } else {
                showMessage("Dirección no encontrada");
            }
        } catch (IOException e) {
            showMessage("Error al buscar dirección");
        }
    }

    private void guardarUbicacionGeografica() {
        if (ubicacionHotel != null && hotelIdGlobal != null) {
            FirebaseFirestore.getInstance().collection("hoteles")
                    .document(hotelIdGlobal)
                    .update("geoposicion", new GeoPoint(ubicacionHotel.latitude, ubicacionHotel.longitude))
                    .addOnSuccessListener(aVoid -> showMessage("Ubicación guardada"))
                    .addOnFailureListener(e -> showMessage("Error al guardar la ubicación"));
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
