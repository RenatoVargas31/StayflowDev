package com.iot.stayflowdev.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.databinding.ActivityClienteDetalleHotelBinding;
import com.iot.stayflowdev.model.Hotel;
import com.iot.stayflowdev.model.LugaresCercanos;
import com.iot.stayflowdev.model.Servicio;
import com.iot.stayflowdev.viewmodels.HotelViewModel;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.iot.stayflowdev.model.Habitacion;
import com.iot.stayflowdev.viewmodels.HabitacionViewModel;
import com.iot.stayflowdev.cliente.adapter.ClienteHabitacionAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

public class ClienteDetalleHotelActivity extends AppCompatActivity implements ClienteHabitacionAdapter.OnHabitacionSeleccionadaListener {

    private ActivityClienteDetalleHotelBinding binding;
    private HotelViewModel hotelViewModel;
    private static final String TAG = "ClienteDetalleHotel";

    // Constante para recibir el ID del hotel
    public static final String EXTRA_HOTEL_ID = "com.iot.stayflowdev.EXTRA_HOTEL_ID";

    // ID del hotel seleccionado
    private String hotelId;
    // ViewModel para manejar las habitaciones
    private HabitacionViewModel habitacionViewModel;
    private ClienteHabitacionAdapter habitacionAdapter;
    private RecyclerView rvHabitaciones;
    private Map<String, Integer> habitacionesSeleccionadas = new HashMap<>();
    private double totalHabitaciones = 0.0;
    // Objeto hotel
    private Hotel hotel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Inicializar ViewBinding
        binding = ActivityClienteDetalleHotelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar ViewModel
        hotelViewModel = new ViewModelProvider(this).get(HotelViewModel.class);
        habitacionViewModel = new ViewModelProvider(this).get(HabitacionViewModel.class);

        // Configurar RecyclerView de habitaciones
        configurarRecyclerViewHabitaciones();

        // Configurar el botón de retroceso
        binding.btnBack.setOnClickListener(v -> onBackPressed());

        // Obtener el ID del hotel desde el intent
        if (getIntent().hasExtra(EXTRA_HOTEL_ID)) {
            hotelId = getIntent().getStringExtra(EXTRA_HOTEL_ID);
            cargarDatosHotel(hotelId);
        } else {
            // Si no hay ID, mostrar mensaje de error y cerrar
            Toast.makeText(this, "Error: No se pudo obtener información del hotel", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void cargarDatosHotel(String hotelId) {
        // Cargar información básica del hotel
        hotelViewModel.getHotelPorId(hotelId).observe(this, hotel -> {
            if (hotel != null) {
                this.hotel = hotel;
                mostrarDatosHotel(hotel);

                // Una vez cargado el hotel, cargar los servicios y lugares históricos
                cargarServiciosYLugares(hotelId);
            } else {
                Toast.makeText(this, "No se pudo cargar la información del hotel", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Hotel no encontrado con ID: " + hotelId);
            }
        });
        // Cargar habitaciones del hotel
        cargarHabitacionesDisponibles(hotelId);
    }
    private void configurarRecyclerViewHabitaciones() {
        rvHabitaciones = findViewById(R.id.rvHabitacionesDinamicas);

        if (rvHabitaciones != null) {
            rvHabitaciones.setLayoutManager(new LinearLayoutManager(this));
            rvHabitaciones.setNestedScrollingEnabled(false);

            habitacionAdapter = new ClienteHabitacionAdapter(this);
            rvHabitaciones.setAdapter(habitacionAdapter);

            Log.d(TAG, "RecyclerView de habitaciones configurado");
        } else {
            Log.e(TAG, "No se encontró RecyclerView rvHabitacionesDinamicas");
        }
    }
    private void cargarHabitacionesDisponibles(String hotelId) {
        mostrarEstadoCargaHabitaciones(true);

        habitacionViewModel.cargarHabitaciones(hotelId);

        habitacionViewModel.getHabitaciones().observe(this, habitaciones -> {
            mostrarEstadoCargaHabitaciones(false);

            if (habitaciones != null && !habitaciones.isEmpty()) {
                // Filtrar solo habitaciones disponibles
                List<Habitacion> disponibles = new ArrayList<>();
                for (Habitacion hab : habitaciones) {
                    if (hab.getDisponible() != null && hab.getDisponible()) {
                        disponibles.add(hab);
                    }
                }

                if (!disponibles.isEmpty()) {
                    habitacionAdapter.setHabitaciones(disponibles);
                    mostrarHabitaciones(true);
                    Log.d(TAG, "Habitaciones disponibles cargadas: " + disponibles.size());
                } else {
                    mostrarMensajeSinHabitaciones();
                    Log.d(TAG, "No hay habitaciones disponibles");
                }
            } else {
                mostrarMensajeSinHabitaciones();
                Log.d(TAG, "No se encontraron habitaciones para el hotel");
            }
        });
    }
    private void mostrarEstadoCargaHabitaciones(boolean mostrar) {
        ProgressBar progressBar = findViewById(R.id.progressBarHabitaciones);
        if (progressBar != null) {
            progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
    }
    private void mostrarHabitaciones(boolean mostrar) {
        if (rvHabitaciones != null) {
            rvHabitaciones.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }

        TextView tvNoHabitaciones = findViewById(R.id.tvNoHabitaciones);
        if (tvNoHabitaciones != null) {
            tvNoHabitaciones.setVisibility(mostrar ? View.GONE : View.VISIBLE);
        }
    }
    private void mostrarMensajeSinHabitaciones() {
        mostrarHabitaciones(false);
        TextView tvNoHabitaciones = findViewById(R.id.tvNoHabitaciones);
        if (tvNoHabitaciones != null) {
            tvNoHabitaciones.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onSeleccionCambiada(String habitacionId, int cantidad, double subtotal) {
        Log.d(TAG, "Habitación " + habitacionId + " - Cantidad: " + cantidad + " - Subtotal: " + subtotal);
        // Aquí puedes agregar lógica adicional si necesitas reaccionar a cambios individuales
    }
    @Override
    public void onTotalCambiado(double total, Map<String, Integer> selecciones) {
        this.totalHabitaciones = total;
        this.habitacionesSeleccionadas = new HashMap<>(selecciones);

        Log.d(TAG, "Total habitaciones: S/. " + total);
        Log.d(TAG, "Habitaciones seleccionadas: " + selecciones.size());

        actualizarResumenHabitaciones();
        actualizarBotonContinuar();
    }
    private void actualizarResumenHabitaciones() {
        TextView tvResumenHabitaciones = findViewById(R.id.tvResumenHabitaciones);
        TextView tvTotalHabitaciones = findViewById(R.id.tvTotalHabitaciones);
        View cardResumen = findViewById(R.id.cardResumenHabitaciones);

        if (habitacionesSeleccionadas.isEmpty()) {
            // Ocultar resumen si no hay selecciones
            if (cardResumen != null) {
                cardResumen.setVisibility(View.GONE);
            }
            return;
        }

        // Mostrar card de resumen
        if (cardResumen != null) {
            cardResumen.setVisibility(View.VISIBLE);
        }

        // Crear texto de resumen
        StringBuilder resumen = new StringBuilder();
        int totalHabitacionesCount = 0;

        for (Map.Entry<String, Integer> entry : habitacionesSeleccionadas.entrySet()) {
            String habitacionId = entry.getKey();
            int cantidad = entry.getValue();

            // Buscar tipo de habitación por ID
            String tipoHabitacion = obtenerTipoHabitacionPorId(habitacionId);

            if (resumen.length() > 0) {
                resumen.append(", ");
            }
            resumen.append(cantidad).append(" ").append(tipoHabitacion);
            totalHabitacionesCount += cantidad;
        }

        // Actualizar textos
        if (tvResumenHabitaciones != null) {
            tvResumenHabitaciones.setText(resumen.toString());
        }

        if (tvTotalHabitaciones != null) {
            tvTotalHabitaciones.setText(String.format("S/. %.2f", totalHabitaciones));
        }

        Log.d(TAG, "Resumen actualizado: " + resumen.toString() + " - Total: S/. " + totalHabitaciones);
    }
    private String obtenerTipoHabitacionPorId(String habitacionId) {
        if (habitacionAdapter != null) {
            // Necesitamos acceder a las habitaciones del adapter
            // Por simplicidad, usaremos un método genérico
            return "Habitación"; // Puedes mejorar esto después
        }
        return "Habitación";
    }
    private void actualizarBotonContinuar() {
        Button btnContinuar = findViewById(R.id.btnContinuarReserva);

        if (btnContinuar != null) {
            boolean haySeleccion = !habitacionesSeleccionadas.isEmpty();
            btnContinuar.setEnabled(haySeleccion);

            if (haySeleccion) {
                int totalHabitacionesCount = 0;
                for (int cantidad : habitacionesSeleccionadas.values()) {
                    totalHabitacionesCount += cantidad;
                }

                String textoBoton = "Continuar con " + totalHabitacionesCount +
                        " habitación" + (totalHabitacionesCount == 1 ? "" : "es");
                btnContinuar.setText(textoBoton);
            } else {
                btnContinuar.setText("Selecciona habitaciones");
            }
        }
    }

    private void mostrarDatosHotel(Hotel hotel) {
        // Mostrar nombre del hotel
        binding.tvHotelName.setText(hotel.getNombre());

        // Mostrar calificación
        try {
            float calificacion = 0;
            if (hotel.getCalificacion() != null && !hotel.getCalificacion().isEmpty()) {
                calificacion = Float.parseFloat(hotel.getCalificacion());
            }
            binding.ratingBar.setRating(calificacion);
            binding.tvRating.setText(String.format("%.1f/5", calificacion));
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error al convertir calificación: " + e.getMessage());
            binding.ratingBar.setRating(0);
            binding.tvRating.setText("0.0/5");
        }

        // Mostrar dirección
        binding.tvAddress.setText(hotel.getUbicacion());

        // Mostrar descripción
        binding.tvDescription.setText(hotel.getDescripcion());

        // Cargar imagen con Glide
        if (hotel.getFotos() != null && !hotel.getFotos().isEmpty()) {
            // Usar Glide para cargar la imagen
            com.bumptech.glide.Glide.with(this)
                .load(hotel.getFotos().get(0))
                .placeholder(R.drawable.ic_hotel)
                .error(R.drawable.ic_hotel)
                .into(binding.ivHotelImage);
        } else {
            // Si no hay fotos, mostrar una imagen por defecto
            binding.ivHotelImage.setImageResource(R.drawable.ic_hotel);
        }
    }

    private void cargarServiciosYLugares(String hotelId) {
        // Cargar servicios
        hotelViewModel.getServiciosPorHotel(hotelId).observe(this, servicios -> {
            if (servicios != null && !servicios.isEmpty()) {
                mostrarServicios(servicios);
                Log.d(TAG, "Servicios cargados para hotel: " + servicios.size());
            } else {
                // No hay servicios para mostrar
                Log.d(TAG, "No hay servicios para el hotel con ID: " + hotelId);
            }
        });

        // Cargar lugares históricos cercanos
        hotelViewModel.getLugaresHistoricosPorHotel(hotelId).observe(this, lugares -> {
            if (lugares != null && !lugares.isEmpty()) {
                mostrarLugaresHistoricos(lugares);
                Log.d(TAG, "Lugares históricos cargados para hotel: " + lugares.size());
            } else {
                // No hay lugares históricos para mostrar
                binding.tvNoLugares.setVisibility(View.VISIBLE);
                Log.d(TAG, "No hay lugares históricos para el hotel con ID: " + hotelId);
            }
        });
    }

    private void mostrarServicios(List<Servicio> servicios) {
        // Primero ocultamos todos los servicios
        binding.layoutServicioRestaurante.setVisibility(View.GONE);
        binding.layoutServicioPiscina.setVisibility(View.GONE);
        binding.layoutServicioWifi.setVisibility(View.GONE);
        binding.layoutServicioEstacionamiento.setVisibility(View.GONE);
        binding.layoutServicioMascotas.setVisibility(View.GONE);

        // Luego mostramos solo los disponibles
        for (Servicio servicio : servicios) {
            if (servicio.getNombre() == null) continue;

            // Usar el mismo enfoque que en HotelBusquedaAdapter
            switch (servicio.getNombre().toLowerCase()) {
                case "restaurante":
                    binding.layoutServicioRestaurante.setVisibility(View.VISIBLE);
                    break;
                case "piscina":
                    binding.layoutServicioPiscina.setVisibility(View.VISIBLE);
                    break;
                case "wifi":
                    binding.layoutServicioWifi.setVisibility(View.VISIBLE);
                    break;
                case "estacionamiento":
                    binding.layoutServicioEstacionamiento.setVisibility(View.VISIBLE);
                    break;
                case "mascotas":
                    binding.layoutServicioMascotas.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private void mostrarLugaresHistoricos(List<LugaresCercanos> lugares) {
        // Limpiamos cualquier lugar previo
        binding.layoutLugaresHistoricos.removeAllViews();
        binding.tvNoLugares.setVisibility(View.GONE);

        // Si no hay lugares, mostramos el mensaje
        if (lugares.isEmpty()) {
            binding.tvNoLugares.setVisibility(View.VISIBLE);
            return;
        }

        // Para cada lugar, creamos un TextView con el nombre y la distancia
        for (LugaresCercanos lugar : lugares) {
            TextView textView = new TextView(this);

            // Formatear la distancia en kilómetros
            float distanciaKm = 0;
            try {
                if (lugar.getDistancia() != null && !lugar.getDistancia().isEmpty()) {
                    distanciaKm = Float.parseFloat(lugar.getDistancia()) / 1000; // Convertir a km si está en metros
                }
            } catch (NumberFormatException e) {
                // Manejar error de conversión
                distanciaKm = 0;
            }

            // Crear el texto con nombre y distancia
            String textoLugar = String.format("%s - %.1f km", lugar.getNombre(), distanciaKm);
            textView.setText(textoLugar);
            textView.setTextAppearance(android.R.style.TextAppearance_Medium);

            // Configurar el layout
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 8, 0, 8);
            textView.setLayoutParams(params);

            // Añadir al contenedor
            binding.layoutLugaresHistoricos.addView(textView);
        }
    }
}
