package com.iot.stayflowdev.cliente;

import android.content.Intent;
import java.util.TimeZone;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.databinding.ActivityClienteDetalleHotelBinding;
import com.iot.stayflowdev.model.Hotel;
import com.iot.stayflowdev.model.LugaresCercanos;
import com.iot.stayflowdev.model.Reserva;
import com.iot.stayflowdev.model.Servicio;
import com.iot.stayflowdev.viewmodels.HotelViewModel;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.iot.stayflowdev.model.Habitacion;
import com.iot.stayflowdev.viewmodels.HabitacionViewModel;
import com.iot.stayflowdev.cliente.adapter.ClienteHabitacionAdapter;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import android.text.Editable;
import android.text.TextWatcher;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

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

    // NUEVAS VARIABLES PARA EL FORMULARIO
    private Timestamp fechaInicio;
    private Timestamp fechaFin;
    private SimpleDateFormat dateFormat;

    // Objeto hotel
    private Hotel hotel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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

        // Configurar el formulario de reserva
        configurarFormularioReserva();

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
        binding.btnContinuarReserva.setOnClickListener(v -> {
            Reserva reserva = generarReserva();
            Gson gson = new Gson();
            String reservaJson = gson.toJson(reserva);

            Intent intent = new Intent(this, ReservaResumenActivity.class);
            intent.putExtra("reserva_data", reservaJson);
            Log.d(TAG, "Reserva JSON: " + reservaJson);
            startActivity(intent);
        });
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
    private void configurarFormularioReserva() {
        configurarCamposFecha();
        configurarCamposHuespedes();
        //configurarValidacionesFormulario();

        Log.d(TAG, "Formulario de reserva configurado");
    }
    private void configurarCamposFecha() {
        // Configurar campo fecha de inicio
        binding.etFechaInicio.setOnClickListener(v -> mostrarDatePicker(true));

        // Configurar campo fecha de fin
        binding.etFechaFin.setOnClickListener(v -> mostrarDatePicker(false));

        // Evitar que aparezca teclado
        binding.etFechaInicio.setKeyListener(null);
        binding.etFechaFin.setKeyListener(null);
    }
    private void mostrarDatePicker(boolean esFechaInicio) {
        // Configurar restricciones de calendario
        Calendar today = Calendar.getInstance();

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now()); // Solo fechas futuras

        // Si es fecha de fin y ya hay fecha de inicio, restringir desde fecha inicio + 1
        if (!esFechaInicio && fechaInicio != null) {
            Calendar minDate = Calendar.getInstance();
            minDate.setTime(fechaInicio.toDate());
            minDate.add(Calendar.DAY_OF_MONTH, 1); // Mínimo 1 día después
            constraintsBuilder.setStart(minDate.getTimeInMillis());
        }

        // Crear DatePicker
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(esFechaInicio ? "Fecha de entrada" : "Fecha de salida")
                .setSelection(esFechaInicio ?
                        MaterialDatePicker.todayInUtcMilliseconds() :
                        (fechaInicio != null ? fechaInicio.toDate().getTime() + TimeUnit.DAYS.toMillis(1) :
                                MaterialDatePicker.todayInUtcMilliseconds() + TimeUnit.DAYS.toMillis(1)))
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        // Configurar listener de selección
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Date fechaSeleccionada = new Date(selection);
            String fechaFormateada = dateFormat.format(fechaSeleccionada);

            if (esFechaInicio) {
                fechaInicio = new Timestamp(fechaSeleccionada);
                binding.etFechaInicio.setText(fechaFormateada);

                // Si ya hay fecha fin y es anterior a la nueva fecha inicio, limpiarla
                if (fechaFin != null && fechaFin.toDate().before(fechaSeleccionada)) {
                    fechaFin = null;
                    binding.etFechaFin.setText("");
                    binding.tilFechaFin.setError(null);
                }

                binding.tilFechaInicio.setError(null);
                Log.d(TAG, "Fecha de inicio seleccionada: " + fechaFormateada);
            } else {
                fechaFin = new Timestamp(fechaSeleccionada);
                binding.etFechaFin.setText(fechaFormateada);
                binding.tilFechaFin.setError(null);
                Log.d(TAG, "Fecha de fin seleccionada: " + fechaFormateada);
            }

            // Validar fechas después de selección
            //validarFechas();
            //actualizarEstadoBoton();
        });

        // Mostrar DatePicker
        datePicker.show(getSupportFragmentManager(), esFechaInicio ? "DATE_PICKER_INICIO" : "DATE_PICKER_FIN");
    }
    private void configurarCamposHuespedes() {
        // TextWatcher para adultos
        binding.etAdultos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // TextWatcher para niños
        binding.etNinos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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
    private String obtenerIdUsuarioActual(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
           String userId = user.getUid();
           Log.d(TAG, "ID de usuario actual: " + userId);
        } else {
           Log.e(TAG, "No hay usuario autenticado");
        }
        assert user != null;
        return user.getUid();
    }
    private List<Reserva.Servicio> obtenerServiciosParaReserva(String hotelId){
        //Obtener los servicios del viewmodel
        List<Servicio> servicios = hotelViewModel.getServiciosPorHotel(hotelId).getValue();
        //Nuestra lista de servicios para la reserva
        List<Reserva.Servicio> serviciosReserva = new ArrayList<>();
        if (servicios != null) {
            for (Servicio servicio : servicios) {
                // Crear un objeto Servicio para la reserva
                Reserva.Servicio servicioReserva = new Reserva.Servicio();
                servicioReserva.setNombre(servicio.getNombre());
                servicioReserva.setDescripcion(servicio.getDescripcion());
                servicioReserva.setPrecio(servicio.getPrecio());
                serviciosReserva.add(servicioReserva);
            }
        } else {
            Log.e(TAG, "No se encontraron servicios para el hotel con ID: " + hotelId);
        }
        return serviciosReserva;
    }
    // Metodo para convertir las habitaciones seleccionadas a una lista de Reserva.Habitacion
    private List<Reserva.Habitacion> obtenerHabitacionesParaReserva() {
        List<Reserva.Habitacion> habitacionesReserva = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : habitacionesSeleccionadas.entrySet()) {
            String habitacionId = entry.getKey();
            int cantidad = entry.getValue();

            // Buscar la habitación por ID en el adapter
            Habitacion habitacion = habitacionAdapter.encontrarHabitacionPorId(habitacionId);
            if (habitacion != null) {
                // Crear objeto Reserva.Habitacion
                Reserva.Habitacion habitacionReserva = new Reserva.Habitacion();
                habitacionReserva.setId(habitacionId);
                habitacionReserva.setTipo(habitacion.getTipo());
                habitacionReserva.setCantidad(cantidad);
                habitacionReserva.setPrecio(habitacion.getPrecio());
                habitacionesReserva.add(habitacionReserva);
            } else {
                Log.e(TAG, "Habitación no encontrada con ID: " + habitacionId);
            }
        }
        return habitacionesReserva;
    }
    //Metodo para crear Reserva.CantHuespedes
    private Reserva.CantHuespedes obtenerCantHuespedes() {
        Reserva.CantHuespedes cantHuespedes = new Reserva.CantHuespedes();
        String adultos = binding.etAdultos.getText().toString().trim();
        String ninos = binding.etNinos.getText().toString().trim();

        // Validar y asignar valores
        cantHuespedes.setAdultos(adultos.isEmpty() ? "0" : adultos);
        cantHuespedes.setNinos(ninos.isEmpty() ? "0" : ninos);

        return cantHuespedes;
    }
    //Metodo para obtener el estado del checkbox de taxi
    private boolean obtenerQuieroTaxi() {
        return binding.checkBoxTaxi.isChecked();
    }
    //Metodo para generar el costo total de la reserva consirando solo las habitaciones y las fechas (caalcular noches)
    private String calcularCostoTotalFinal() {
        if (fechaInicio == null || fechaFin == null) {
            return "0.00"; // Si no hay fechas, el costo es 0
        }

        long diferenciaMillis = fechaFin.toDate().getTime() - fechaInicio.toDate().getTime();
        long noches = TimeUnit.DAYS.convert(diferenciaMillis, TimeUnit.MILLISECONDS);

        if (noches <= 0) {
            return "0.00"; // Si las fechas son inválidas, el costo es 0
        }

        double costoTotal = totalHabitaciones * noches;
        return String.format(Locale.getDefault(), "%.2f", costoTotal);
    }
    // Metodo para generar el objeto Reserva
    private Reserva generarReserva() {
        Reserva reserva = new Reserva();
        reserva.setIdUsuario(obtenerIdUsuarioActual());
        reserva.setIdHotel(hotelId);
        reserva.setFechaCreacion(Timestamp.now());
        reserva.setFechaInicio(fechaInicio);
        reserva.setFechaFin(fechaFin);
        reserva.setServicios(obtenerServiciosParaReserva(hotelId));
        reserva.setCantHuespedes(obtenerCantHuespedes());
        reserva.setHabitaciones(obtenerHabitacionesParaReserva());
        reserva.setCostoTotal(calcularCostoTotalFinal());
        reserva.setEstado("pendiente"); // Estado inicial
        reserva.setQuieroTaxi(obtenerQuieroTaxi());
        return reserva;
    }


}
