package com.iot.stayflowdev.adminHotel.huesped;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.AdminInicioActivity;
import com.iot.stayflowdev.adminHotel.HuespedAdminActivity;
import com.iot.stayflowdev.adminHotel.MensajeriaAdminActivity;
import com.iot.stayflowdev.adminHotel.PerfilAdminActivity;
import com.iot.stayflowdev.adminHotel.ReportesAdminActivity;
import com.iot.stayflowdev.databinding.ActivityCheckoutDetalleBinding;
import com.iot.stayflowdev.model.Reserva;

import java.util.ArrayList;
import java.util.List;

public class CheckoutDetalleActivity extends AppCompatActivity {
    private ActivityCheckoutDetalleBinding binding;

    private String codigoReserva;
    private double montoTotalReserva = 0;
    private double totalDaniosOriginales = 0;
    private double totalFinal = 0;

    // Referencias a las vistas
    private LinearLayout containerDanios;
    private LinearLayout containerHabitaciones;
    private LinearLayout containerServicios;
    private MaterialButton btnAgregarDanio;
    private TextView tvSubtotalHospedaje;
    private TextView tvResumenDaniosMonto;
    private TextView tvTotalMonto;
    private MaterialButton btnConfirmarCobrar;
    private TextView tvSinDanios;

    // Lista para mantener los daños agregados dinámicamente
    private List<DanioItem> daniosAgregados = new ArrayList<>();
    private int contadorDanios = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutDetalleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Configurar bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.menu_huesped);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_inicio) {
                startActivity(new Intent(this, AdminInicioActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else if (id == R.id.menu_reportes) {
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
            return true;
        });

        inicializarVistas();
        cargarDatosIntent();
        configurarListeners();
        actualizarTotales();

        // Actualizar el resumen inicial
        TextView tvResumenHospedajeMonto = findViewById(R.id.tvResumenHospedajeMonto);
        tvResumenHospedajeMonto.setText(String.format("S/. %.2f", montoTotalReserva));
    }

    private void inicializarVistas() {
        containerDanios = findViewById(R.id.containerDanios);
        containerHabitaciones = findViewById(R.id.containerHabitaciones);
        containerServicios = findViewById(R.id.containerServicios);
        btnAgregarDanio = findViewById(R.id.btnAgregarDanio);
        tvSubtotalHospedaje = findViewById(R.id.tvSubtotalHospedaje);
        tvResumenDaniosMonto = findViewById(R.id.tvResumenDaniosMonto);
        tvTotalMonto = findViewById(R.id.tvTotalMonto);
        btnConfirmarCobrar = findViewById(R.id.btnConfirmarCobrar);
        tvSinDanios = findViewById(R.id.tvSinDanios);

        // REMOVIDO: No buscar btnBack porque no existe en el layout actual
        // El botón de retroceso ahora se maneja en la toolbar
    }

    private void cargarDatosIntent() {
        Intent intent = getIntent();
        String nombre = intent.getStringExtra("nombre");
        String idUsuario = intent.getStringExtra("idUsuario");

        codigoReserva = intent.getStringExtra("codigoReserva");
        String costoTotal = intent.getStringExtra("costoTotal");
        String tarjeta = intent.getStringExtra("tarjeta");

        // Log para debugging
        android.util.Log.d("CheckoutDetalle", "Nombre recibido: '" + nombre + "'");
        android.util.Log.d("CheckoutDetalle", "Código reserva: " + codigoReserva);
        android.util.Log.d("CheckoutDetalle", "ID Usuario recibido: " + idUsuario);


        // Validar y usar nombre por defecto si es necesario
        if (nombre == null || nombre.trim().isEmpty() || nombre.equalsIgnoreCase("Usuario") || nombre.equalsIgnoreCase("null")) {
            if (idUsuario != null && !idUsuario.trim().isEmpty()) {
                obtenerNombreUsuario(idUsuario); // Usar directamente
            } else if (codigoReserva != null) {
                obtenerNombreDesdeReserva(codigoReserva); // Fallback
            } else {
                establecerTituloCheckout("Huésped");
            }
        } else {
            establecerTituloCheckout(nombre); // Nombre válido
        }

        // Parsear costo total de la reserva
        try {
            montoTotalReserva = Double.parseDouble(costoTotal != null ? costoTotal.replaceAll("[^0-9.]", "") : "0");
        } catch (NumberFormatException e) {
            montoTotalReserva = 0;
        }

        // Cargar habitaciones
        String[] tiposHab = intent.getStringArrayExtra("tiposHabitaciones");
        int[] cantidadesHab = intent.getIntArrayExtra("cantidadesHabitaciones");
        String[] preciosHab = intent.getStringArrayExtra("preciosHabitaciones");

        if (tiposHab != null && cantidadesHab != null && preciosHab != null) {
            cargarHabitaciones(tiposHab, cantidadesHab, preciosHab);
        }

        // Cargar servicios
        String[] nombresServ = intent.getStringArrayExtra("nombresServicios");
        String[] preciosServ = intent.getStringArrayExtra("preciosServicios");

        if (nombresServ != null && preciosServ != null) {
            cargarServicios(nombresServ, preciosServ);
        }

        // Cargar daños existentes
        String[] descripcionesDanios = intent.getStringArrayExtra("descripcionesDaniosExistentes");
        String[] preciosDanios = intent.getStringArrayExtra("preciosDaniosExistentes");

        if (descripcionesDanios != null && preciosDanios != null) {
            for (int i = 0; i < descripcionesDanios.length; i++) {
                try {
                    totalDaniosOriginales += Double.parseDouble(preciosDanios[i].replaceAll("[^0-9.]", ""));
                } catch (NumberFormatException e) {
                    // Ignorar valores inválidos
                }
            }
        }

        // Mostrar en las vistas solo si tenemos un nombre válido
        if (nombre != null && !nombre.equals("Huésped")) {
            establecerTituloCheckout(nombre);
        }

        // Buscar tvTarjetaNumero con verificación de null
        TextView tvTarjetaNumero = findViewById(R.id.tvTarjetaNumero);
        if (tvTarjetaNumero != null) {
            tvTarjetaNumero.setText(tarjeta != null ? tarjeta : "•••• 1234");
        }

        // Actualizar subtotal
        if (tvSubtotalHospedaje != null) {
            tvSubtotalHospedaje.setText(String.format("S/. %.2f", montoTotalReserva));
        }
    }

    private void obtenerNombreDesdeReserva(String reservaId) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("reservas")
                .document(reservaId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String idUsuario = doc.getString("idUsuario");
                        if (idUsuario != null) {
                            android.util.Log.d("CheckoutDetalle", "Obteniendo nombre del usuario: " + idUsuario);
                            obtenerNombreUsuario(idUsuario);
                        } else {
                            establecerTituloCheckout("Huésped");
                        }
                    } else {
                        establecerTituloCheckout("Huésped");
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CheckoutDetalle", "Error obteniendo reserva", e);
                    establecerTituloCheckout("Huésped");
                });
    }

    private void obtenerNombreUsuario(String idUsuario) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(idUsuario)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String nombreReal = "Huésped";
                    if (userDoc.exists()) {
                        // Intentar varios campos
                        String nombre = userDoc.getString("nombre");
                        String firstName = userDoc.getString("firstName");
                        String apellidos = userDoc.getString("apellidos");
                        String email = userDoc.getString("email");
                        String displayName = userDoc.getString("displayName");

                        android.util.Log.d("CheckoutDetalle", "Campos del usuario:");
                        android.util.Log.d("CheckoutDetalle", "- nombre: " + nombre);
                        android.util.Log.d("CheckoutDetalle", "- firstName: " + firstName);
                        android.util.Log.d("CheckoutDetalle", "- apellidos: " + apellidos);
                        android.util.Log.d("CheckoutDetalle", "- email: " + email);
                        android.util.Log.d("CheckoutDetalle", "- displayName: " + displayName);

                        if (nombre != null && !nombre.trim().isEmpty()) {
                            nombreReal = nombre;
                        } else if (firstName != null && !firstName.trim().isEmpty()) {
                            nombreReal = firstName;
                            if (apellidos != null && !apellidos.trim().isEmpty()) {
                                nombreReal += " " + apellidos;
                            }
                        } else if (apellidos != null && !apellidos.trim().isEmpty()) {
                            nombreReal = apellidos; // ✅ usar apellidos si no hay nada más
                        } else if (email != null && !email.trim().isEmpty()) {
                            nombreReal = email.split("@")[0];
                        } else if (displayName != null && !displayName.trim().isEmpty()) {
                            nombreReal = displayName;
                        }

                    }
                    android.util.Log.d("CheckoutDetalle", "Nombre final obtenido: " + nombreReal);
                    establecerTituloCheckout(nombreReal);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CheckoutDetalle", "Error obteniendo usuario", e);
                    establecerTituloCheckout("Huésped");
                });
    }

    private void establecerTituloCheckout(String nombre) {
        TextView tvTituloCheckout = findViewById(R.id.tvTituloCheckout);
        if (tvTituloCheckout != null) {
            tvTituloCheckout.setText("Checkout de " + nombre);
            android.util.Log.d("CheckoutDetalle", "Título establecido: Checkout de " + nombre);
        }
    }

    private void cargarHabitaciones(String[] tipos, int[] cantidades, String[] precios) {
        containerHabitaciones.removeAllViews();

        // Agregar título de sección
        TextView titulo = new TextView(this);
        titulo.setText("Habitaciones");
        titulo.setTextSize(14);
        titulo.setTextColor(getResources().getColor(android.R.color.black));
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);
        titulo.setPadding(0, 0, 0, 24);
        containerHabitaciones.addView(titulo);

        for (int i = 0; i < tipos.length; i++) {
            View habitacionView = LayoutInflater.from(this).inflate(R.layout.item_desglose_simple, containerHabitaciones, false);

            TextView tvDescripcion = habitacionView.findViewById(R.id.tvDescripcionItem);
            TextView tvPrecio = habitacionView.findViewById(R.id.tvPrecioItem);

            String descripcion = tipos[i];
            tvDescripcion.setText(descripcion);
            tvPrecio.setText("S/. " + precios[i]);

            containerHabitaciones.addView(habitacionView);
        }
    }

    private void cargarServicios(String[] nombres, String[] precios) {
        if (nombres.length == 0) return;

        containerServicios.removeAllViews();

        // Agregar título de sección
        TextView titulo = new TextView(this);
        titulo.setText("Servicios adicionales");
        titulo.setTextSize(14);
        titulo.setTextColor(getResources().getColor(android.R.color.black));
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);
        titulo.setPadding(0, 24, 0, 24);
        containerServicios.addView(titulo);

        for (int i = 0; i < nombres.length; i++) {
            View servicioView = LayoutInflater.from(this).inflate(R.layout.item_desglose_simple, containerServicios, false);

            TextView tvDescripcion = servicioView.findViewById(R.id.tvDescripcionItem);
            TextView tvPrecio = servicioView.findViewById(R.id.tvPrecioItem);

            tvDescripcion.setText(nombres[i]);
            tvPrecio.setText("S/. " + precios[i]);

            containerServicios.addView(servicioView);
        }
    }

    private void configurarListeners() {
        btnAgregarDanio.setOnClickListener(v -> agregarNuevoDanio());
        btnConfirmarCobrar.setOnClickListener(v -> confirmarCheckout());
    }

    private void agregarNuevoDanio() {
        contadorDanios++;

        // Crear vista para el nuevo daño
        View danioView = LayoutInflater.from(this).inflate(R.layout.item_danio_dinamico, containerDanios, false);

        // Referencias a los componentes del item
        TextInputLayout layoutDescripcion = danioView.findViewById(R.id.inputLayoutDescripcion);
        TextInputLayout layoutMonto = danioView.findViewById(R.id.inputLayoutMonto);
        TextInputEditText inputDescripcion = danioView.findViewById(R.id.inputDescripcion);
        TextInputEditText inputMonto = danioView.findViewById(R.id.inputMonto);
        MaterialButton btnEliminar = danioView.findViewById(R.id.btnEliminarDanio);

        // Configurar hints dinámicos
        layoutDescripcion.setHint("Descripción del daño " + contadorDanios);
        layoutMonto.setHint("Monto (S/.)");

        // Crear objeto para mantener referencia
        DanioItem danioItem = new DanioItem(danioView, inputDescripcion, inputMonto);
        daniosAgregados.add(danioItem);

        // Listener para actualizar totales cuando cambie el monto
        inputMonto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                actualizarTotales();
            }
        });

        // Listener para eliminar daño
        btnEliminar.setOnClickListener(v -> {
            containerDanios.removeView(danioView);
            daniosAgregados.remove(danioItem);
            actualizarTotales();
            actualizarVisibilidadMensaje();
        });

        // Agregar la vista al contenedor
        containerDanios.addView(danioView);

        // Actualizar totales y visibilidad
        actualizarTotales();
        actualizarVisibilidadMensaje();
    }

    private void actualizarTotales() {
        double totalDaniosNuevos = 0;

        // Sumar todos los daños agregados dinámicamente
        for (DanioItem danio : daniosAgregados) {
            String montoStr = danio.inputMonto.getText().toString().trim();
            if (!montoStr.isEmpty()) {
                try {
                    totalDaniosNuevos += Double.parseDouble(montoStr);
                } catch (NumberFormatException e) {
                    // Ignorar valores inválidos
                }
            }
        }

        // Calcular totales
        double totalDaniosCompleto = totalDaniosOriginales + totalDaniosNuevos;
        double totalFinalCompleto = montoTotalReserva + totalDaniosCompleto;

        // Actualizar vistas
        tvResumenDaniosMonto.setText(String.format("S/. %.2f", totalDaniosCompleto));
        tvTotalMonto.setText(String.format("S/. %.2f", totalFinalCompleto));

        // Actualizar el texto del label si hay daños
        TextView tvResumenDaniosLabel = findViewById(R.id.tvResumenDaniosLabel);
        if (tvResumenDaniosLabel != null) {
            if (totalDaniosCompleto > 0) {
                tvResumenDaniosLabel.setText("Daños y cargos adicionales");
            } else {
                tvResumenDaniosLabel.setText("Sin daños adicionales");
            }
        }
    }

    private void actualizarVisibilidadMensaje() {
        if (daniosAgregados.isEmpty()) {
            tvSinDanios.setVisibility(View.VISIBLE);
        } else {
            tvSinDanios.setVisibility(View.GONE);
        }
    }

    private void confirmarCheckout() {
        // Validar que todos los daños tengan descripción y monto
        for (DanioItem danio : daniosAgregados) {
            String descripcion = danio.inputDescripcion.getText().toString().trim();
            String monto = danio.inputMonto.getText().toString().trim();

            if (!descripcion.isEmpty() && monto.isEmpty()) {
                Toast.makeText(this, "Complete el monto para: " + descripcion, Toast.LENGTH_SHORT).show();
                return;
            }

            if (descripcion.isEmpty() && !monto.isEmpty()) {
                Toast.makeText(this, "Complete la descripción para el monto S/. " + monto, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Crear lista de daños para guardar
        List<Reserva.Danio> listaDaniosCompleta = new ArrayList<>();

        // Agregar daños dinámicos válidos
        for (DanioItem danio : daniosAgregados) {
            String descripcion = danio.inputDescripcion.getText().toString().trim();
            String monto = danio.inputMonto.getText().toString().trim();

            if (!descripcion.isEmpty() && !monto.isEmpty()) {
                listaDaniosCompleta.add(new Reserva.Danio(descripcion, monto));
            }
        }

        // Guardar en Firestore
        guardarDaniosYCompletarCheckout(listaDaniosCompleta);
    }

    private void guardarDaniosYCompletarCheckout(List<Reserva.Danio> danios) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Actualizar tanto los daños como el estado
        db.collection("reservas")
                .document(codigoReserva)
                .update(
                        "danios", danios,
                        "estado", "completada"
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "¡Checkout completado exitosamente!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al completar checkout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Clase auxiliar para mantener referencias a los componentes de cada daño
    private static class DanioItem {
        View view;
        TextInputEditText inputDescripcion;
        TextInputEditText inputMonto;

        DanioItem(View view, TextInputEditText inputDescripcion, TextInputEditText inputMonto) {
            this.view = view;
            this.inputDescripcion = inputDescripcion;
            this.inputMonto = inputMonto;
        }
    }
}