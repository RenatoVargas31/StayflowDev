package com.iot.stayflowdev.adminHotel.huesped;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.AdminInicioActivity;
import com.iot.stayflowdev.adminHotel.MensajeriaAdminActivity;
import com.iot.stayflowdev.adminHotel.PerfilAdminActivity;
import com.iot.stayflowdev.adminHotel.ReportesAdminActivity;
import com.iot.stayflowdev.databinding.ActivityDetalleReservaBinding;
import com.iot.stayflowdev.model.Reserva;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DetalleReservaActivity extends AppCompatActivity {

    private ActivityDetalleReservaBinding binding;
    private FirebaseFirestore db;
    private String idReserva;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetalleReservaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Obtener ID de la reserva del Intent
        idReserva = getIntent().getStringExtra("idReserva");

        if (idReserva == null || idReserva.isEmpty()) {
            Toast.makeText(this, "Error: ID de reserva no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        configurarToolbar();
        configurarBottomNavigation();
        cargarDetallesReserva();
    }

    private void configurarToolbar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle de Reserva");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void configurarBottomNavigation() {
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
    }

    private void cargarDetallesReserva() {
        db.collection("reservas")
                .document(idReserva)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Reserva reserva = documentSnapshot.toObject(Reserva.class);
                        if (reserva != null) {
                            mostrarDetallesReserva(reserva);
                        } else {
                            mostrarError("Error al procesar los datos de la reserva");
                        }
                    } else {
                        mostrarError("Reserva no encontrada");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DetalleReserva", "Error al cargar reserva", e);
                    mostrarError("Error al cargar los detalles de la reserva");
                });
    }

    private void mostrarDetallesReserva(Reserva reserva) {
        try {
            // Información básica - Buscar nombre real del usuario
            buscarNombreUsuario(reserva.getIdUsuario());

            binding.textCodigoReserva.setText("Código de reserva: " + reserva.getId());

            // Fechas
            if (reserva.getFechaInicio() != null) {
                binding.textLlegada.setText(dateFormat.format(reserva.getFechaInicio().toDate()));
            } else {
                binding.textLlegada.setText("Fecha no disponible");
            }

            if (reserva.getFechaFin() != null) {
                binding.textSalida.setText(dateFormat.format(reserva.getFechaFin().toDate()));
            } else {
                binding.textSalida.setText("Fecha no disponible");
            }

            // Huéspedes
            mostrarInformacionHuespedes(reserva.getCantHuespedes());

            // Habitaciones (AHORA COMPLETAMENTE DINÁMICO)
            mostrarHabitaciones(reserva.getHabitaciones());

            // Precio total y servicios adicionales
            mostrarPrecioYServicios(reserva);

        } catch (Exception e) {
            Log.e("DetalleReserva", "Error al mostrar detalles", e);
            mostrarError("Error al mostrar los detalles de la reserva");
        }
    }

    private void buscarNombreUsuario(String idUsuario) {
        if (idUsuario == null || idUsuario.isEmpty()) {
            binding.textNombre.setText("Usuario desconocido");
            return;
        }

        db.collection("usuarios")
                .document(idUsuario)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d("DEBUG", "Datos recibidos: " + documentSnapshot.getData());

                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombres");
                        String apellido = documentSnapshot.getString("apellidos");

                        if (nombre != null && apellido != null) {
                            String nombreCompleto = nombre + " " + apellido;
                            binding.textNombre.setText(nombreCompleto);
                        } else {
                            binding.textNombre.setText("⚠ Nombre o apellido vacíos");
                        }
                    } else {
                        binding.textNombre.setText("⚠ Documento no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERROR_FIRESTORE", "Fallo al leer usuario", e);
                    binding.textNombre.setText("⚠ Error de conexión");
                });
    }



    private void mostrarInformacionHuespedes(Reserva.CantHuespedes cantHuespedes) {
        if (cantHuespedes != null) {
            int adultos = 0;
            int ninos = 0;

            try {
                adultos = Integer.parseInt(cantHuespedes.getAdultos());
            } catch (NumberFormatException e) {
                adultos = 0;
            }

            try {
                ninos = Integer.parseInt(cantHuespedes.getNinos());
            } catch (NumberFormatException e) {
                ninos = 0;
            }

            String textoHuespedes = adultos + " adulto" + (adultos > 1 ? "s" : "");
            if (ninos > 0) {
                textoHuespedes += " + " + ninos + " niño" + (ninos > 1 ? "s" : "");
            }

            binding.textHuespedes.setText(textoHuespedes);
        } else {
            binding.textHuespedes.setText("Información no disponible");
        }
    }

    // NUEVO MÉTODO COMPLETAMENTE DINÁMICO PARA HABITACIONES
    private void mostrarHabitaciones(List<Reserva.Habitacion> habitaciones) {
        Log.d("DetalleReserva", "=== HABITACIONES DEBUG ===");

        // Obtener el container donde vamos a agregar las habitaciones dinámicamente
        LinearLayout containerHabitaciones = binding.getRoot().findViewById(R.id.containerHabitaciones);

        if (containerHabitaciones == null) {
            Log.e("DetalleReserva", "containerHabitaciones es NULL");
            return;
        }

        // LIMPIAR todas las habitaciones existentes (esto es seguro porque vamos a recrear todo)
        containerHabitaciones.removeAllViews();

        if (habitaciones == null || habitaciones.isEmpty()) {
            Log.d("DetalleReserva", "No hay habitaciones disponibles");
            // Crear un card que diga "No hay habitaciones"
            MaterialCardView cardVacio = crearCardHabitacion("Información no disponible", "Sin habitaciones", 0);
            containerHabitaciones.addView(cardVacio);
            return;
        }

        Log.d("DetalleReserva", "Número total de habitaciones: " + habitaciones.size());

        // Crear un card para CADA habitación dinámicamente
        for (int i = 0; i < habitaciones.size(); i++) {
            Reserva.Habitacion habitacion = habitaciones.get(i);
            if (habitacion != null) {
                String tipoHabitacion = habitacion.getTipo() != null ? habitacion.getTipo() : "Sin tipo";
                String descripcion = "Habitación " + (i + 1);

                Log.d("DetalleReserva", "Creando card para habitación " + (i+1) + ": " + tipoHabitacion);

                MaterialCardView cardHabitacion = crearCardHabitacion(
                        "Habitación " + tipoHabitacion,
                        descripcion,
                        i + 1
                );

                containerHabitaciones.addView(cardHabitacion);
            }
        }

        Log.d("DetalleReserva", "=== FIN HABITACIONES DEBUG ===");
    }

    // NUEVO MÉTODO PARA CREAR CARDS DINÁMICOS
    private MaterialCardView crearCardHabitacion(String tipoHabitacion, String descripcion, int numero) {
        // Crear el card principal
        MaterialCardView card = new MaterialCardView(this);

        // Configurar parámetros del card
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 24); // margin bottom (8dp = 24px)
        card.setLayoutParams(cardParams);

        // Estilo del card (igual que en el XML)
        card.setCardElevation(0f);
        card.setRadius(36f); // 12dp = 36px
        card.setStrokeWidth(3); // 1dp = 3px
        card.setStrokeColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
        card.setCardBackgroundColor(getResources().getColor(android.R.color.white, getTheme()));

        // Crear el LinearLayout interno
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(36, 36, 36, 36); // 12dp = 36px

        // Texto del tipo de habitación
        TextView textTipo = new TextView(this);
        textTipo.setText(tipoHabitacion);
        textTipo.setTextSize(16f);
        textTipo.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
        textTipo.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Texto de descripción
        TextView textDescripcion = new TextView(this);
        textDescripcion.setText(descripcion);
        textDescripcion.setTextSize(14f);
        textDescripcion.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));

        // Agregar los textos al layout
        layout.addView(textTipo);
        layout.addView(textDescripcion);

        // Agregar el layout al card
        card.addView(layout);

        Log.d("DetalleReserva", "Card creado para: " + tipoHabitacion);

        return card;
    }

    private void mostrarPrecioYServicios(Reserva reserva) {
        // Precio total
        String precio = "Precio total: ";
        if (reserva.getCostoTotal() != null && !reserva.getCostoTotal().isEmpty()) {
            precio += "$" + reserva.getCostoTotal();
        } else {
            precio += "No disponible";
        }

        TextView textPrecio = binding.getRoot().findViewById(R.id.textPrecioTotal);
        if (textPrecio != null) {
            textPrecio.setText(precio);
        }

        // Servicios adicionales
        String serviciosTexto = "";
        if (reserva.isQuieroTaxi()) {
            serviciosTexto += "• Incluye servicio de taxi gratuito\n";
        }

        if (reserva.getServicios() != null && !reserva.getServicios().isEmpty()) {
            for (Reserva.Servicio servicio : reserva.getServicios()) {
                serviciosTexto += "• " + servicio.getNombre();
                if (servicio.getPrecio() != null && !servicio.getPrecio().equals("0")) {
                    serviciosTexto += " ($" + servicio.getPrecio() + ")";
                }
                serviciosTexto += "\n";
            }
        }

        if (serviciosTexto.isEmpty()) {
            serviciosTexto = "Sin servicios adicionales";
        }

        TextView textServicios = binding.getRoot().findViewById(R.id.textServicios);
        if (textServicios != null) {
            textServicios.setText(serviciosTexto.trim());
        }

        // Estado de la reserva
        mostrarEstadoReserva(reserva);
    }

    private void mostrarEstadoReserva(Reserva reserva) {
        TextView textEstado = binding.getRoot().findViewById(R.id.textEstado);
        if (textEstado != null) {
            String estado = reserva.getEstado();
            if (estado != null && !estado.isEmpty()) {
                // Capitalizar primera letra
                estado = estado.substring(0, 1).toUpperCase() + estado.substring(1).toLowerCase();
                textEstado.setText(estado);

                // Cambiar color según el estado
                switch (estado.toLowerCase()) {
                    case "confirmada":
                    case "activa":
                        textEstado.setTextColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
                        break;
                    case "cancelada":
                        textEstado.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
                        break;
                    case "pendiente":
                        textEstado.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, getTheme()));
                        break;
                    default:
                        textEstado.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
                        break;
                }
            } else {
                textEstado.setText("Estado no disponible");
                textEstado.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
            }
        }
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        // Opcional: mostrar información de error en la UI
        binding.textNombre.setText("Error al cargar");
        binding.textCodigoReserva.setText(mensaje);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}