package com.iot.stayflowdev.adminHotel.huesped;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.AdminInicioActivity;
import com.iot.stayflowdev.adminHotel.HuespedAdminActivity;
import com.iot.stayflowdev.adminHotel.MensajeriaAdminActivity;
import com.iot.stayflowdev.adminHotel.PerfilAdminActivity;
import com.iot.stayflowdev.adminHotel.ReportesAdminActivity;
import com.iot.stayflowdev.adminHotel.adapter.TaxiAdapter;
import com.iot.stayflowdev.adminHotel.model.Taxi;
import com.iot.stayflowdev.adminHotel.repository.AdminHotelViewModel;
import com.iot.stayflowdev.databinding.ActivityTaxiAdminBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TaxiAdminActivity extends AppCompatActivity {

    private ActivityTaxiAdminBinding binding;
    private List<Taxi> taxis = new ArrayList<>();
    private String textoBusqueda = "";
    private String estadoSeleccionado = "";
    private boolean ordenarPorRecientes = true;
    private TaxiAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaxiAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Estado del Taxista");

        configurarBottomNavigation();

        adapter = new TaxiAdapter(new ArrayList<>(), this);
        binding.recyclerTaxi.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTaxi.setAdapter(adapter);

        AdminHotelViewModel vm = new ViewModelProvider(this).get(AdminHotelViewModel.class);
        vm.getHotelId().observe(this, hotelId -> {
            if (hotelId != null) {
                configurarFiltrosYBusqueda();
                configurarOrdenamiento();
                cargarTaxisDesdeFirestore(hotelId);
            }
        });
    }

    private void configurarBottomNavigation() {
        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.setSelectedItemId(R.id.menu_huesped);
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
    }

    private void cargarTaxisDesdeFirestore(String hotelId) {
        FirebaseFirestore.getInstance()
                .collection("solicitudesTaxi")
                .whereEqualTo("idHotel", "hoteles/" + hotelId)
                .get()
                .addOnSuccessListener(query -> {
                    taxis.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        String nombre = doc.getString("nombrePasajero");
                        String estado = doc.getString("estado");
                        String origen = doc.getString("origen");
                        String destino = doc.getString("destino");
                        String direccionResumen = origen + " → " + destino;
                        String hora = doc.getString("horaAceptacion");

                        // Obtener el código de reserva
                        String codigoReserva = doc.getString("codigoReserva");
                        if (codigoReserva == null || codigoReserva.isEmpty()) {
                            codigoReserva = doc.getId().substring(0, 8).toUpperCase();
                        }

                        String fecha = "";
                        if (doc.getTimestamp("fechaCreacion") != null) {
                            fecha = new SimpleDateFormat("dd-MM-yy hh:mm a", Locale.getDefault())
                                    .format(doc.getTimestamp("fechaCreacion").toDate());
                        }

                        // Crear taxi con código de reserva
                        Taxi taxi = new Taxi(nombre, codigoReserva, estado, fecha + " " + hora, direccionResumen, R.drawable.img_guest_placeholder);
                        taxis.add(taxi);
                    }
                    aplicarFiltros();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar solicitudes", Toast.LENGTH_SHORT).show());
    }

    private void configurarFiltrosYBusqueda() {
        // Configurar búsqueda
        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                textoBusqueda = s.toString().trim().toLowerCase();
                aplicarFiltros();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Configurar filtro por estado con estados reales
        String[] estados = {"", "aceptada", "en camino", "llegado", "finalizada", "cancelada"};
        String[] estadosDisplay = {"Todos los estados", "Aceptada", "En camino", "Llegado", "Finalizada", "Cancelada"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, estadosDisplay);
        binding.inputFilter.setAdapter(adapter);

        binding.inputFilter.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                estadoSeleccionado = "";
            } else {
                estadoSeleccionado = estados[position];
            }
            aplicarFiltros();
        });

        // Configurar botón limpiar filtros (si existe en el layout)
        if (binding.btnClearFilters != null) {
            binding.btnClearFilters.setOnClickListener(v -> limpiarFiltros());
        }
    }

    private void configurarOrdenamiento() {
        binding.chipRecientes.setOnClickListener(v -> {
            ordenarPorRecientes = true;
            binding.chipRecientes.setChecked(true);
            binding.chipAntiguas.setChecked(false);
            aplicarFiltros();
        });

        binding.chipAntiguas.setOnClickListener(v -> {
            ordenarPorRecientes = false;
            binding.chipRecientes.setChecked(false);
            binding.chipAntiguas.setChecked(true);
            aplicarFiltros();
        });

        binding.chipRecientes.setChecked(true);
    }

    private void limpiarFiltros() {
        // Limpiar búsqueda
        binding.inputSearch.setText("");

        // Limpiar filtro de estado
        binding.inputFilter.setText("", false);
        estadoSeleccionado = "";

        // Resetear ordenamiento a recientes
        ordenarPorRecientes = true;
        binding.chipRecientes.setChecked(true);
        binding.chipAntiguas.setChecked(false);

        // Aplicar filtros
        aplicarFiltros();

        Toast.makeText(this, "Filtros limpiados", Toast.LENGTH_SHORT).show();
    }

    private void aplicarFiltros() {
        List<Taxi> filtrados = new ArrayList<>();
        for (Taxi taxi : taxis) {
            // Buscar en nombre Y código de reserva
            boolean coincideTexto = textoBusqueda.isEmpty() ||
                    taxi.getNombre().toLowerCase().contains(textoBusqueda) ||
                    (taxi.getCodigoReserva() != null && taxi.getCodigoReserva().toLowerCase().contains(textoBusqueda));

            // Filtrar por estado
            boolean coincideEstado = estadoSeleccionado.isEmpty() ||
                    taxi.getEstadoTaxi().equalsIgnoreCase(estadoSeleccionado);

            if (coincideTexto && coincideEstado) {
                filtrados.add(taxi);
            }
        }

        // Ordenar por fecha
        if (ordenarPorRecientes) {
            Collections.reverse(filtrados);
        }

        // Actualizar contador de resultados (si existe en el layout)
        if (binding.tvResultCount != null) {
            actualizarContadorResultados(filtrados.size());
        }

        // Mostrar/ocultar estado vacío (si existe en el layout)
        if (binding.emptyState != null) {
            if (filtrados.isEmpty()) {
                binding.recyclerTaxi.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerTaxi.setVisibility(View.VISIBLE);
                binding.emptyState.setVisibility(View.GONE);
            }
        }

        adapter = new TaxiAdapter(filtrados, this);
        binding.recyclerTaxi.setAdapter(adapter);
    }

    private void actualizarContadorResultados(int cantidad) {
        String texto;
        if (cantidad == 0) {
            texto = "No se encontraron solicitudes";
        } else if (cantidad == 1) {
            texto = "Mostrando 1 solicitud";
        } else {
            texto = "Mostrando " + cantidad + " solicitudes";
        }
        if (binding.tvResultCount != null) {
            binding.tvResultCount.setText(texto);
        }
    }
}