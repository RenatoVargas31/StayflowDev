package com.iot.stayflowdev.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.databinding.ActivityReservaResumenBinding;
import com.iot.stayflowdev.model.Hotel;
import com.iot.stayflowdev.model.Reserva;
import com.iot.stayflowdev.viewmodels.HotelViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ReservaResumenActivity extends AppCompatActivity {

    private ActivityReservaResumenBinding binding;
    private HotelViewModel hotelViewModel;
    private FirebaseFirestore db;
    private Reserva reserva;
    private Hotel hotel;

    private static final String TAG = "ReservaResumen";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Inicializar ViewBinding
        binding = ActivityReservaResumenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar ViewModel
        hotelViewModel = new ViewModelProvider(this).get(HotelViewModel.class);
        db = FirebaseFirestore.getInstance();

        // Configurar toolbar
        configurarToolbar();

        // Configurar botón confirmar
        configurarBotonConfirmar();

        // Obtener datos de la reserva
        obtenerReservaDelIntent();
    }

    private void configurarToolbar() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }
    private void configurarBotonConfirmar() {
        binding.btnConfirmarReserva.setOnClickListener(v -> {
            // Deshabilitar botón para evitar múltiples clics
            binding.btnConfirmarReserva.setEnabled(false);
            binding.btnConfirmarReserva.setText("Procesando...");

            guardarReservaEnFirestore();
        });
    }
    private void obtenerReservaDelIntent() {
        try {
            String reservaJson = getIntent().getStringExtra("reserva_data");

            if (reservaJson != null && !reservaJson.isEmpty()) {
                Gson gson = new Gson();
                reserva = gson.fromJson(reservaJson, Reserva.class);

                if (reserva != null) {
                    Log.d(TAG, "Reserva recibida correctamente");
                    cargarDatosHotel();
                } else {
                    mostrarError("Error al procesar los datos de la reserva");
                }
            } else {
                mostrarError("No se recibieron datos de la reserva");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al deserializar reserva", e);
            mostrarError("Error al procesar los datos de la reserva");
        }
    }

    private void cargarDatosHotel() {
        if (reserva.getIdHotel() != null) {
            hotelViewModel.getHotelPorId(reserva.getIdHotel()).observe(this, hotel -> {
                if (hotel != null) {
                    this.hotel = hotel;
                    mostrarTodosLosDatos();
                } else {
                    Log.e(TAG, "Hotel no encontrado");
                    mostrarDatosReservaSinHotel();
                }
            });
        } else {
            mostrarError("ID de hotel no válido");
        }
    }

    private void mostrarTodosLosDatos() {
        mostrarInformacionHotel();
        mostrarFechasYHuespedes();
        mostrarHabitaciones();
        mostrarServicios();
        mostrarResumenCostos();
    }

    private void mostrarDatosReservaSinHotel() {
        // Mostrar datos básicos aunque no se pueda cargar el hotel
        binding.tvHotelName.setText("Hotel no disponible");
        binding.tvAddress.setText("Información no disponible");
        binding.ratingBar.setVisibility(View.GONE);
        binding.ivHotelImage.setImageResource(R.drawable.ic_hotel);

        mostrarFechasYHuespedes();
        mostrarHabitaciones();
        mostrarServicios();
        mostrarResumenCostos();
    }

    private void mostrarInformacionHotel() {
        if (hotel != null) {
            // Nombre del hotel
            binding.tvHotelName.setText(hotel.getNombre());

            // Dirección
            binding.tvAddress.setText(hotel.getUbicacion());

            // Rating
            try {
                float rating = Float.parseFloat(hotel.getCalificacion());
                binding.ratingBar.setRating(rating);
                binding.ratingBar.setVisibility(View.VISIBLE);
            } catch (NumberFormatException e) {
                binding.ratingBar.setVisibility(View.GONE);
            }

            // Imagen del hotel
            if (hotel.getFotos() != null && !hotel.getFotos().isEmpty()) {
                Glide.with(this)
                        .load(hotel.getFotos().get(0))
                        .placeholder(R.drawable.ic_hotel)
                        .error(R.drawable.ic_hotel)
                        .centerCrop()
                        .into(binding.ivHotelImage);
            } else {
                binding.ivHotelImage.setImageResource(R.drawable.ic_hotel);
            }

            Log.d(TAG, "Información del hotel mostrada: " + hotel.getNombre());
        }
    }

    private void mostrarFechasYHuespedes() {
        // Fechas
        if (reserva.getFechaInicio() != null) {
            binding.tvFechaEntrada.setText(dateFormat.format(reserva.getFechaInicio().toDate()));
        }

        if (reserva.getFechaFin() != null) {
            binding.tvFechaSalida.setText(dateFormat.format(reserva.getFechaFin().toDate()));
        }

        // Calcular y mostrar número de noches
        long noches = calcularNumeroNoches();
        binding.tvNochesEstancia.setText(noches + " noche" + (noches == 1 ? "" : "s") + " de estancia");

        // Huéspedes
        if (reserva.getCantHuespedes() != null) {
            binding.tvAdultos.setText(reserva.getCantHuespedes().getAdultos());
            binding.tvNinos.setText(reserva.getCantHuespedes().getNinos());
        }

        Log.d(TAG, "Fechas y huéspedes mostrados - Noches: " + noches);
    }

    private void mostrarHabitaciones() {
        LinearLayout layoutHabitaciones = binding.layoutHabitaciones;
        layoutHabitaciones.removeAllViews(); // Limpiar vistas existentes

        if (reserva.getHabitaciones() != null && !reserva.getHabitaciones().isEmpty()) {
            for (Reserva.Habitacion habitacion : reserva.getHabitaciones()) {
                View habitacionView = crearVistaHabitacion(habitacion);
                layoutHabitaciones.addView(habitacionView);
            }
            Log.d(TAG, "Habitaciones mostradas: " + reserva.getHabitaciones().size());
        } else {
            TextView sinHabitaciones = new TextView(this);
            sinHabitaciones.setText("No hay habitaciones seleccionadas");
            sinHabitaciones.setTextColor(getColor(R.color.md_theme_error));
            layoutHabitaciones.addView(sinHabitaciones);
        }
    }

    private View crearVistaHabitacion(Reserva.Habitacion habitacion) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_resumen_habitacion, null);

        TextView tvDescripcion = view.findViewById(R.id.tvDescripcionHabitacion);
        TextView tvPrecio = view.findViewById(R.id.tvPrecioHabitacion);

        // Crear descripción de la habitación
        String descripcion = habitacion.getCantidad() + "x Habitación " + habitacion.getTipo();
        tvDescripcion.setText(descripcion);

        // Mostrar precio
        try {
            double precio = Double.parseDouble(habitacion.getPrecio());
            tvPrecio.setText(String.format(Locale.getDefault(), "S/. %.2f", precio));
        } catch (NumberFormatException e) {
            tvPrecio.setText("S/. " + habitacion.getPrecio());
        }

        return view;
    }

    private void mostrarServicios() {
        // Servicio de taxi
        if (reserva.isQuieroTaxi()) {
            binding.tvTaxiStatus.setText("Incluido");
            binding.tvTaxiStatus.setTextColor(getColor(R.color.md_theme_primary));
            binding.layoutServicioTaxi.setVisibility(View.VISIBLE);
        } else {
            binding.tvTaxiStatus.setText("No solicitado");
            binding.tvTaxiStatus.setTextColor(getColor(R.color.md_theme_outline));
            binding.layoutServicioTaxi.setVisibility(View.VISIBLE);
        }

        // Otros servicios
        LinearLayout layoutOtrosServicios = binding.layoutOtrosServicios;
        layoutOtrosServicios.removeAllViews();

        if (reserva.getServicios() != null && !reserva.getServicios().isEmpty()) {
            for (Reserva.Servicio servicio : reserva.getServicios()) {
                View servicioView = crearVistaServicio(servicio);
                layoutOtrosServicios.addView(servicioView);
            }
            Log.d(TAG, "Servicios adicionales mostrados: " + reserva.getServicios().size());
        }
    }

    private View crearVistaServicio(Reserva.Servicio servicio) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(0, 8, 0, 0);

        TextView tvNombre = new TextView(this);
        tvNombre.setText(servicio.getNombre());
        tvNombre.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvPrecio = new TextView(this);
        tvPrecio.setText("S/. " + servicio.getPrecio());

        layout.addView(tvNombre);
        layout.addView(tvPrecio);

        return layout;
    }

    private void mostrarResumenCostos() {
        long noches = calcularNumeroNoches();

        try {
            // Costo total de la reserva (ya incluye habitaciones × noches)
            double costoTotal = Double.parseDouble(reserva.getCostoTotal());

            // Calcular costo por noche (dividir el total entre las noches)
            double costoPorNoche = noches > 0 ? costoTotal / noches : 0;

            // Mostrar valores
            binding.tvCostoPorNoche.setText(String.format(Locale.getDefault(), "S/. %.2f", costoPorNoche));
            binding.tvLabelNoches.setText("Total por " + noches + " noche" + (noches == 1 ? "" : "s") + ":");
            binding.tvCostoTotalNoches.setText(String.format(Locale.getDefault(), "S/. %.2f", costoTotal));

            // Total final (igual al costo total por ahora)
            binding.tvCostoTotal.setText(String.format(Locale.getDefault(), "S/. %.2f", costoTotal));

            // Ocultar servicios adicionales por ahora
            binding.layoutCostoServicios.setVisibility(View.GONE);

            Log.d(TAG, "Resumen de costos mostrado - Total: S/. " + costoTotal);

        } catch (NumberFormatException e) {
            Log.e(TAG, "Error al parsear costo total", e);
            binding.tvCostoTotal.setText("S/. " + reserva.getCostoTotal());
        }
    }

    private long calcularNumeroNoches() {
        if (reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
            long diferenciaMilis = reserva.getFechaFin().toDate().getTime() -
                    reserva.getFechaInicio().toDate().getTime();
            return TimeUnit.DAYS.convert(diferenciaMilis, TimeUnit.MILLISECONDS);
        }
        return 0;
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + mensaje);
        finish(); // Cerrar activity si hay error crítico
    }

    // ================= MÉTODOS PARA GUARDAR EN FIRESTORE =================

    private void guardarReservaEnFirestore() {
        if (reserva == null) {
            mostrarErrorGuardado("Error: No hay datos de reserva para guardar");
            return;
        }

        Log.d(TAG, "Iniciando guardado de reserva en Firestore");

        // Limpiar el ID para que Firestore genere uno nuevo
        reserva.setId(null);
        reserva.setEstado("sin chekout"); // Estado inicial

        // Guardar en la colección "reservas"
        db.collection("reservas")
                .add(reserva)
                .addOnSuccessListener(documentReference -> {
                    String reservaId = documentReference.getId();
                    Log.d(TAG, "Reserva guardada exitosamente con ID: " + reservaId);

                    // Actualizar el objeto con el ID generado
                    reserva.setId(reservaId);

                    manejarExitoReserva(reservaId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar reserva", e);
                    manejarErrorGuardado();
                });
    }

    private void manejarExitoReserva(String reservaId) {
        // Restaurar botón
        binding.btnConfirmarReserva.setEnabled(true);
        binding.btnConfirmarReserva.setText("Confirmar Reserva");

        // Mostrar mensaje de éxito
        Toast.makeText(this, "¡Reserva confirmada exitosamente!", Toast.LENGTH_LONG).show();

        // Aquí puedes navegar a una pantalla de confirmación
        Intent intent = new Intent(this, ClienteReservasActivity.class);
        intent.putExtra("reserva_id", reservaId);
        startActivity(intent);
    }

    private void manejarErrorGuardado() {
        // Restaurar botón
        binding.btnConfirmarReserva.setEnabled(true);
        binding.btnConfirmarReserva.setText("Confirmar Reserva");

        mostrarErrorGuardado("Error al confirmar la reserva. Intenta nuevamente.");
    }

    private void mostrarErrorGuardado(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error en guardado: " + mensaje);
    }
}