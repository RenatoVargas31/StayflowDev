package com.iot.stayflowdev.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.cliente.adapter.HotelBusquedaAdapter;
import com.iot.stayflowdev.databinding.ActivityClienteBusquedaHotelBinding;
import com.iot.stayflowdev.model.Hotel;
import com.iot.stayflowdev.model.LugaresCercanos;
import com.iot.stayflowdev.model.Servicio;
import com.iot.stayflowdev.viewmodels.HotelViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ClienteBusquedaHotelActivity extends AppCompatActivity implements HotelBusquedaAdapter.HotelItemClickListener {

    private static final String TAG = "ClienteBusquedaActivity";
    private ActivityClienteBusquedaHotelBinding binding;
    private HotelViewModel hotelViewModel;
    private HotelBusquedaAdapter adapter;
    private List<Hotel> hotelesBusqueda = new ArrayList<>();
    private String terminoBusqueda = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Inicializar ViewBinding
        binding = ActivityClienteBusquedaHotelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar ViewModel
        hotelViewModel = new ViewModelProvider(this).get(HotelViewModel.class);

        // Obtener término de búsqueda del intent
        if (getIntent().hasExtra(ClienteBuscarActivity.EXTRA_BUSQUEDA)) {
            terminoBusqueda = getIntent().getStringExtra(ClienteBuscarActivity.EXTRA_BUSQUEDA);
            binding.tvSearchTitle.setText(terminoBusqueda);
        }

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar botones y listeners
        setupListeners();

        // Iniciar búsqueda
        if (!terminoBusqueda.isEmpty()) {
            buscarHoteles(terminoBusqueda);
        }
    }

    private void setupRecyclerView() {
        adapter = new HotelBusquedaAdapter(this);
        binding.recyclerViewHoteles.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewHoteles.setAdapter(adapter);
    }

    private void setupListeners() {
        // Botón de retroceso
        binding.btnBack.setOnClickListener(v -> onBackPressed());

        // Chips de filtrado
        binding.chipRating.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ordenarHotelesPorCalificacion();
                binding.chipLugaresHistoricos.setChecked(false);
            }
        });

        binding.chipLugaresHistoricos.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ordenarHotelesPorLugaresHistoricos();
                binding.chipRating.setChecked(false);
            }
        });
    }

    private void buscarHoteles(String query) {
        // Mostrar progreso de carga
        mostrarEstadoCarga(true);

        // Realizar búsqueda
        hotelViewModel.buscarHotelesPorNombre(query).observe(this, hoteles -> {
            hotelesBusqueda = hoteles;
            mostrarEstadoCarga(false);

            if (hoteles != null && !hoteles.isEmpty()) {
                Log.d(TAG, "Hoteles encontrados: " + hoteles.size());

                // Mostrar resultados
                adapter.setHoteles(hoteles);
                binding.recyclerViewHoteles.setVisibility(View.VISIBLE);
                binding.txtMensaje.setVisibility(View.GONE);

                // Cargar servicios y lugares históricos para cada hotel
                for (Hotel hotel : hoteles) {
                    cargarServiciosYLugares(hotel);
                }
            } else {
                Log.d(TAG, "No se encontraron hoteles con la búsqueda: " + query);
                binding.recyclerViewHoteles.setVisibility(View.GONE);
                binding.txtMensaje.setText("No se encontraron hoteles con el nombre '" + query + "'");
                binding.txtMensaje.setVisibility(View.VISIBLE);
            }
        });

        // Observar errores
        hotelViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                mostrarEstadoCarga(false);
                binding.recyclerViewHoteles.setVisibility(View.GONE);
                binding.txtMensaje.setText("Error: " + error);
                binding.txtMensaje.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error al buscar hoteles: " + error);
            }
        });
    }

    private void cargarServiciosYLugares(Hotel hotel) {
        // Cargar servicios
        hotelViewModel.getServiciosPorHotel(hotel.getId()).observe(this, servicios -> {
            if (servicios != null) {
                adapter.setServiciosPorHotel(hotel.getId(), servicios);
                Log.d(TAG, "Servicios cargados para hotel " + hotel.getNombre() + ": " + servicios.size());
            }
        });

        // Cargar lugares históricos
        hotelViewModel.getLugaresHistoricosPorHotel(hotel.getId()).observe(this, lugares -> {
            if (lugares != null) {
                adapter.setLugaresPorHotel(hotel.getId(), lugares);
                Log.d(TAG, "Lugares históricos cargados para hotel " + hotel.getNombre() + ": " + lugares.size());
            }
        });
    }

    private void ordenarHotelesPorCalificacion() {
        if (hotelesBusqueda.isEmpty()) return;

        List<Hotel> hotelesOrdenados = new ArrayList<>(hotelesBusqueda);
        Collections.sort(hotelesOrdenados, (h1, h2) -> {
            float cal1 = 0f;
            float cal2 = 0f;

            try {
                if (h1.getCalificacion() != null && !h1.getCalificacion().isEmpty()) {
                    cal1 = Float.parseFloat(h1.getCalificacion());
                }
                if (h2.getCalificacion() != null && !h2.getCalificacion().isEmpty()) {
                    cal2 = Float.parseFloat(h2.getCalificacion());
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error al convertir calificación: " + e.getMessage());
            }

            return Float.compare(cal2, cal1); // Orden descendente (mejor calificación primero)
        });

        adapter.setHoteles(hotelesOrdenados);
    }

    private void ordenarHotelesPorLugaresHistoricos() {
        if (hotelesBusqueda.isEmpty()) return;

        // Primero obtener todos los lugares históricos para poder ordenar
        List<Hotel> hotelesOrdenados = new ArrayList<>(hotelesBusqueda);

        Collections.sort(hotelesOrdenados, (h1, h2) -> {
            int lugares1 = adapter.getNumeroLugaresPorHotel(h1.getId());
            int lugares2 = adapter.getNumeroLugaresPorHotel(h2.getId());
            return Integer.compare(lugares2, lugares1); // Orden descendente (más lugares primero)
        });

        adapter.setHoteles(hotelesOrdenados);
    }

    private void mostrarEstadoCarga(boolean cargando) {
        binding.progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        binding.recyclerViewHoteles.setVisibility(cargando ? View.GONE : View.VISIBLE);
        binding.txtMensaje.setVisibility(View.GONE);
    }

    @Override
    public void onHotelClick(Hotel hotel) {
        // Navegar a la pantalla de detalles del hotel
        Intent intent = new Intent(this, ClienteDetalleHotelActivity.class);
        intent.putExtra(ClienteDetalleHotelActivity.EXTRA_HOTEL_ID, hotel.getId());
        startActivity(intent);
        Log.d(TAG, "Navegando al detalle del hotel: " + hotel.getNombre());
    }
}