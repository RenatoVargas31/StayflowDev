package com.iot.stayflowdev.cliente;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.databinding.ActivityReservaResumenBinding;
import com.iot.stayflowdev.model.Hotel;
import com.iot.stayflowdev.model.Reserva;
import com.iot.stayflowdev.model.TarjetaCredito;
import com.iot.stayflowdev.viewmodels.HotelViewModel;
import com.iot.stayflowdev.viewmodels.TarjetaCreditoViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ReservaResumenActivity extends AppCompatActivity {

    private ActivityReservaResumenBinding binding;
    private HotelViewModel hotelViewModel;
    private TarjetaCreditoViewModel tarjetaCreditoViewModel;
    private FirebaseFirestore db;
    private Reserva reserva;
    private Hotel hotel;
    private boolean modoVisualizacion = false;
    private boolean ocultarBotonConfirmar = false;
    private Dialog tarjetaDialog;

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

        // Inicializar ViewModels
        hotelViewModel = new ViewModelProvider(this).get(HotelViewModel.class);
        tarjetaCreditoViewModel = new ViewModelProvider(this).get(TarjetaCreditoViewModel.class);
        db = FirebaseFirestore.getInstance();

        // Verificar si estamos en modo visualización
        modoVisualizacion = getIntent().getBooleanExtra("modo_visualizacion", false);

        // Verificar si debemos ocultar el botón de confirmar (cuando viene desde ClienteReservasActivity)
        ocultarBotonConfirmar = getIntent().getBooleanExtra("ocultar_boton_confirmar", false);

        Log.d(TAG, "Modo visualización: " + modoVisualizacion);
        Log.d(TAG, "Ocultar botón confirmar: " + ocultarBotonConfirmar);

        // Configurar toolbar
        configurarToolbar();

        // Observar si el usuario tiene tarjeta de crédito registrada
        configurarObservadoresTarjeta();

        // Obtener datos de la reserva
        obtenerReservaDelIntent();
    }

    private void configurarToolbar() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void configurarObservadoresTarjeta() {
        // Verificar si estamos en modo visualización o si se debe ocultar el botón
        if (modoVisualizacion || ocultarBotonConfirmar) {
            // Ocultar el botón inmediatamente
            binding.containerBotonFijo.setVisibility(View.GONE);
            return; // No necesitamos verificar tarjeta
        }

        // Verificar si el usuario tiene tarjeta registrada
        tarjetaCreditoViewModel.verificarTarjeta();

        // Observar si el usuario tiene tarjeta
        tarjetaCreditoViewModel.getHasTarjeta().observe(this, hasTarjeta -> {
            if (hasTarjeta != null) {
                configurarBotonSegunTarjeta(hasTarjeta);
            }
        });

        // Observar mensajes de éxito
        tarjetaCreditoViewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                tarjetaCreditoViewModel.limpiarMensajes();

                // Cerrar diálogo si está abierto
                if (tarjetaDialog != null && tarjetaDialog.isShowing()) {
                    tarjetaDialog.dismiss();
                }

                // Verificar nuevamente si ahora tiene tarjeta
                tarjetaCreditoViewModel.verificarTarjeta();
            }
        });

        // Observar mensajes de error
        tarjetaCreditoViewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                tarjetaCreditoViewModel.limpiarMensajes();
            }
        });
    }

    private void configurarBotonSegunTarjeta(boolean tieneTarjeta) {
        // Si estamos en modo visualización o se debe ocultar el botón, ocultamos todo el contenedor
        if (modoVisualizacion || ocultarBotonConfirmar) {
            binding.containerBotonFijo.setVisibility(View.GONE);
            return;
        }

        // Asegurarnos de que el contenedor sea visible
        binding.containerBotonFijo.setVisibility(View.VISIBLE);

        if (tieneTarjeta) {
            // Si tiene tarjeta, mostrar botón de confirmar reserva
            binding.btnConfirmarReserva.setText("Confirmar Reserva");
            binding.btnConfirmarReserva.setOnClickListener(v -> {
                // Deshabilitar botón para evitar múltiples clics
                binding.btnConfirmarReserva.setEnabled(false);
                binding.btnConfirmarReserva.setText("Procesando...");

                guardarReservaEnFirestore();
            });
        } else {
            // Si no tiene tarjeta, mostrar botón para registrar tarjeta
            binding.btnConfirmarReserva.setText("Registrar Tarjeta para Continuar");
            binding.btnConfirmarReserva.setOnClickListener(v -> {
                mostrarDialogoTarjetaCredito();
            });
        }
    }

    private void configurarBotonConfirmar() {
        // La verificación real del botón se hace en configurarBotonSegunTarjeta
        // después de verificar si el usuario tiene tarjeta
        if (modoVisualizacion) {
            // En modo visualización, ocultar el botón ya que existe una flecha para volver
            binding.btnConfirmarReserva.setVisibility(View.GONE);
            binding.containerBotonFijo.setVisibility(View.GONE);
        }
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
        // Si estamos en modo visualización, no guardar nada
        if (modoVisualizacion) {
            finish();
            return;
        }

        if (reserva == null) {
            mostrarErrorGuardado("Error: No hay datos de reserva para guardar");
            return;
        }

        Log.d(TAG, "Iniciando guardado de reserva en Firestore");

        // Generar un ID personalizado con formato "STF-AÑO-XXXX"
        String idPersonalizado = generarIdReserva();
        reserva.setId(idPersonalizado);
        reserva.setEstado("sin checkout"); // Estado inicial

        // Guardar en la colección "reservas" con el ID personalizado
        db.collection("reservas")
                .document(idPersonalizado)  // Usar el ID personalizado como nombre del documento
                .set(reserva)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Reserva guardada exitosamente con ID: " + idPersonalizado);
                    manejarExitoReserva(idPersonalizado);

                    // Disminuir cantidad de habitaciones disponibles
                    if (reserva.getHabitaciones() != null && !reserva.getHabitaciones().isEmpty()) {
                        Log.d(TAG, "Disminuyendo habitaciones disponibles para la reserva");
                        for (Reserva.Habitacion habitacion : reserva.getHabitaciones()) {
                            if (habitacion.getId() != null) {
                                hotelViewModel.disminuirHabitacionesDisponibles(hotel.getId(), habitacion.getId(), habitacion.getCantidad());
                                Log.d(TAG, "Habitación " + habitacion.getId() + " actualizada: " + habitacion.getCantidad() + " unidades menos");
                            } else {
                                Log.e(TAG, "ID de habitación no válido: " + habitacion.getId());
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar reserva", e);
                    manejarErrorGuardado();
                });
    }

    /**
     * Genera un ID personalizado para la reserva en formato "STF-AÑO-TIMESTAMP"
     * Esto garantiza IDs únicos sin necesidad de un contador en Firestore
     */
    private String generarIdReserva() {
        // Obtener el año actual
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);

        // Usar un timestamp en milisegundos para garantizar unicidad
        // Limitado a 4 dígitos para mantener el formato deseado
        long timestamp = System.currentTimeMillis() % 10000;

        // Formatear el ID: "STF-2025-XXXX"
        return String.format(Locale.getDefault(), "STF-%d-%04d", anioActual, timestamp);
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
        finish();
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

    private void mostrarDialogoTarjetaCredito() {
        // Crear diálogo personalizado
        tarjetaDialog = new Dialog(this);
        tarjetaDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        tarjetaDialog.setContentView(R.layout.dialog_regitro_tarjeta_credito);
        tarjetaDialog.setCancelable(true);

        // Obtener referencias a las vistas del diálogo
        TextInputLayout tilNumeroTarjeta = tarjetaDialog.findViewById(R.id.til_numero_tarjeta);
        TextInputEditText etNumeroTarjeta = tarjetaDialog.findViewById(R.id.et_numero_tarjeta);
        TextInputLayout tilTitular = tarjetaDialog.findViewById(R.id.til_titular);
        TextInputEditText etTitular = tarjetaDialog.findViewById(R.id.et_titular);
        TextInputLayout tilFechaExpiracion = tarjetaDialog.findViewById(R.id.til_fecha_expiracion);
        TextInputEditText etFechaExpiracion = tarjetaDialog.findViewById(R.id.et_fecha_expiracion);
        TextInputLayout tilCvv = tarjetaDialog.findViewById(R.id.til_cvv);
        TextInputEditText etCvv = tarjetaDialog.findViewById(R.id.et_cvv);
        MaterialButton btnCancelar = tarjetaDialog.findViewById(R.id.btn_cancelar);
        MaterialButton btnRegistrar = tarjetaDialog.findViewById(R.id.btn_registrar);

        // Detectar tipo de tarjeta mientras se escribe
        etNumeroTarjeta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Formatear número de tarjeta con espacios cada 4 dígitos
                String input = s.toString().replaceAll("\\s+", "");
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < input.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(input.charAt(i));
                }

                if (!s.toString().equals(formatted.toString())) {
                    etNumeroTarjeta.setText(formatted.toString());
                    etNumeroTarjeta.setSelection(formatted.length());
                }

                // Detectar tipo de tarjeta
                String tipo = detectarTipoTarjeta(input);
                tarjetaDialog.findViewById(R.id.tv_tipo_tarjeta).setVisibility(View.VISIBLE);
                ((android.widget.TextView) tarjetaDialog.findViewById(R.id.tv_tipo_tarjeta)).setText(tipo);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Formatear fecha de expiración automáticamente (MM/YY)
        etFechaExpiracion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (input.length() == 2 && before == 0) {
                    // Agregar barra después de introducir los 2 primeros dígitos (mes)
                    etFechaExpiracion.setText(input + "/");
                    etFechaExpiracion.setSelection(input.length() + 1);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Configurar botones
        btnCancelar.setOnClickListener(v -> tarjetaDialog.dismiss());

        btnRegistrar.setOnClickListener(v -> {
            // Obtener valores de los campos
            String numeroTarjeta = etNumeroTarjeta.getText().toString().replaceAll("\\s+", "");
            String titular = etTitular.getText().toString().trim();
            String fechaExpiracion = etFechaExpiracion.getText().toString();
            String cvv = etCvv.getText().toString();

            // Validar campos
            if (numeroTarjeta.isEmpty() || titular.isEmpty() || fechaExpiracion.isEmpty() || cvv.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar formato de fecha
            if (!fechaExpiracion.matches("\\d{2}/\\d{2}")) {
                tilFechaExpiracion.setError("Formato inválido. Use MM/AA");
                return;
            }

            // Extraer mes y año
            String[] fechaParts = fechaExpiracion.split("/");
            String mes = fechaParts[0];
            String anio = "20" + fechaParts[1]; // Convertir 2 dígitos a 4 dígitos

            // Validar mes
            int mesInt = Integer.parseInt(mes);
            if (mesInt < 1 || mesInt > 12) {
                tilFechaExpiracion.setError("Mes inválido (1-12)");
                return;
            }

            // Validar fecha de expiración (no vencida)
            int anioInt = Integer.parseInt(anio);
            int mesActual = Calendar.getInstance().get(Calendar.MONTH) + 1; // Calendar.MONTH es 0-based
            int anioActual = Calendar.getInstance().get(Calendar.YEAR);

            if (anioInt < anioActual || (anioInt == anioActual && mesInt < mesActual)) {
                tilFechaExpiracion.setError("La tarjeta está vencida");
                return;
            }

            // Crear objeto TarjetaCredito
            TarjetaCredito tarjeta = new TarjetaCredito(numeroTarjeta, titular, mes, anio, cvv);

            // Guardar tarjeta
            tarjetaCreditoViewModel.guardarTarjeta(tarjeta);
        });

        // Mostrar diálogo
        tarjetaDialog.show();
    }

    private String detectarTipoTarjeta(String numero) {
        if (numero.isEmpty()) {
            return "Tarjeta";
        } else if (numero.startsWith("4")) {
            return "Visa";
        } else if (numero.startsWith("5") || numero.startsWith("2")) {
            return "Mastercard";
        } else if (numero.startsWith("34") || numero.startsWith("37")) {
            return "American Express";
        } else if (numero.startsWith("6")) {
            return "Discover";
        } else {
            return "Tarjeta";
        }
    }
}
