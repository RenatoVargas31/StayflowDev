package com.iot.stayflowdev.adminHotel.huesped;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.AdminInicioActivity;
import com.iot.stayflowdev.adminHotel.HuespedAdminActivity;
import com.iot.stayflowdev.adminHotel.MensajeriaAdminActivity;
import com.iot.stayflowdev.adminHotel.PerfilAdminActivity;
import com.iot.stayflowdev.adminHotel.ReportesAdminActivity;
import com.iot.stayflowdev.databinding.ActivityInfoTaxiBinding;

public class InfoTaxiActivity extends AppCompatActivity {

    private ActivityInfoTaxiBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoTaxiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Menú inferior
        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.setSelectedItemId(R.id.menu_huesped);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Class<?> currentClass = this.getClass();

            if (id == R.id.menu_inicio && !currentClass.equals(AdminInicioActivity.class)) {
                startActivity(new Intent(this, AdminInicioActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_reportes && !currentClass.equals(ReportesAdminActivity.class)) {
                startActivity(new Intent(this, ReportesAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_huesped && !currentClass.equals(ReservasAdminActivity.class)) {
                startActivity(new Intent(this, ReservasAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_mensajeria && !currentClass.equals(MensajeriaAdminActivity.class)) {
                startActivity(new Intent(this, MensajeriaAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_perfil && !currentClass.equals(PerfilAdminActivity.class)) {
                startActivity(new Intent(this, PerfilAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return true;
        });

        // Recuperar datos del Intent
        Intent intent = getIntent();
        String idTaxista = getIntent().getStringExtra("idTaxista");
        if (idTaxista != null && !idTaxista.isEmpty()) {
            cargarInfoVehiculoDesdeFirestore(idTaxista);
            cargarDatosDelTaxista(idTaxista);
        }
        String nombre = intent.getStringExtra("nombre");
        String codigo = intent.getStringExtra("codigo");
        String estadoTaxi = intent.getStringExtra("estadoTaxi");
        String detalleViaje = intent.getStringExtra("detalleViaje");
        String ruta = intent.getStringExtra("ruta");
        int imagenResId = intent.getIntExtra("imagenResId", R.drawable.img_guest_placeholder);

        // Mostrar datos del item_taxi (no tocar)
        ((TextView) findViewById(R.id.tvNombre)).setText(nombre);
        ((TextView) findViewById(R.id.tvCodigo)).setText("Código de reserva: " + codigo);
        ((TextView) findViewById(R.id.tvEstadoTaxi)).setText("Taxi: " + estadoTaxi);
        ((TextView) findViewById(R.id.tvDetalleViaje)).setText("Salida: " + detalleViaje);
        ((TextView) findViewById(R.id.tvRuta)).setText("Ruta: " + ruta);
        ((ImageView) findViewById(R.id.imgGuest)).setImageResource(imagenResId);

        // Cargar datos del conductor y vehículo
        if (idTaxista != null && !idTaxista.isEmpty()) {
            cargarInfoVehiculoDesdeFirestore(idTaxista);
            cargarDatosDelTaxista(idTaxista);
        }
    }

    private void cargarInfoVehiculoDesdeFirestore(String idTaxista) {
        FirebaseFirestore.getInstance()
                .collection("vehiculo")
                .whereEqualTo("driverId", idTaxista)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        String modelo = doc.getString("modelo");
                        String placa = doc.getString("placa");
                        String fotoUrl = doc.getString("fotoVehiculo");

                        binding.tvModelo.setText("Modelo: " + (modelo != null ? modelo : "No disponible"));
                        binding.tvPlaca.setText("Placa Vehicular: " + (placa != null ? placa : "No disponible"));

                        if (fotoUrl != null && !fotoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(fotoUrl)
                                    .placeholder(R.drawable.ic_taxi)
                                    .error(R.drawable.ic_taxi)
                                    .into(binding.imgVehiculo);
                        }
                    } else {
                        Log.d("InfoTaxi", "No se encontró vehículo para el taxista.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("InfoTaxi", "Error cargando info del vehículo", e);
                    Toast.makeText(this, "Error al cargar vehículo", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarDatosDelTaxista(String idTaxista) {
        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(idTaxista)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombres = documentSnapshot.getString("nombres");
                        String apellidos = documentSnapshot.getString("apellidos");
                        String telefono = documentSnapshot.getString("telefono");
                        String dni = documentSnapshot.getString("numeroDocumento"); // o "dni"

                        String nombreCompleto = (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
                        binding.tvNombreConductor.setText(nombreCompleto.trim());
                        binding.tvTelefono.setText("Teléfono: " + (telefono != null ? telefono : "No disponible"));
                        binding.tvDni.setText("DNI: " + (dni != null ? dni : "No disponible"));
                    } else {
                        Log.w("InfoTaxi", "No se encontró usuario con idTaxista: " + idTaxista);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("InfoTaxi", "Error al obtener datos del taxista", e);
                    Toast.makeText(this, "Error al cargar datos del conductor", Toast.LENGTH_SHORT).show();
                });
    }
}
