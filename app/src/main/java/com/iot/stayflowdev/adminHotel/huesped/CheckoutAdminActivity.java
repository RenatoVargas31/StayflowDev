package com.iot.stayflowdev.adminHotel.huesped;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.iot.stayflowdev.adminHotel.NotificacionesAdminActivity;
import com.iot.stayflowdev.adminHotel.PerfilAdminActivity;
import com.iot.stayflowdev.adminHotel.MensajeriaAdminActivity;
import com.iot.stayflowdev.adminHotel.ReportesAdminActivity;
import com.iot.stayflowdev.adminHotel.adapter.CheckoutAdapter;
import com.iot.stayflowdev.adminHotel.model.Checkout;
import com.iot.stayflowdev.adminHotel.repository.AdminHotelViewModel;
import com.iot.stayflowdev.adminHotel.service.NotificacionService;
import com.iot.stayflowdev.databinding.ActivityCheckoutAdminBinding;
import com.iot.stayflowdev.model.Reserva;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CheckoutAdminActivity extends AppCompatActivity {

    private ActivityCheckoutAdminBinding binding;
    private List<Checkout> checkouts = new ArrayList<>();
    private List<Checkout> checkoutsOriginales = new ArrayList<>(); // Lista completa sin filtrar
    private CheckoutAdapter adapter;
    private ImageView notificationIcon;
    private TextView badgeText;
    private AdminHotelViewModel viewModel;
    private NotificacionService notificacionService;

    // Variables para filtros
    private String busquedaActual = "";
    private String filtroMontoActual = "Todos los montos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inicializarVistas();
        configurarViewModel();
        configurarToolbar();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Checkout Pendientes");

        // Inicializar el adapter primero
        adapter = new CheckoutAdapter(checkouts, this);
        binding.recyclerCheckout.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerCheckout.setAdapter(adapter);

        configurarBottomNavigation();
        configurarFiltros();

        // Cargar checkouts pendientes
        AdminHotelViewModel viewModel = new ViewModelProvider(this).get(AdminHotelViewModel.class);
        viewModel.getHotelId().observe(this, hotelId -> {
            if (hotelId != null && !hotelId.isEmpty()) {
                cargarCheckoutsPendientes(hotelId);
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
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_reportes) {
                startActivity(new Intent(this, ReportesAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_huesped) {
                startActivity(new Intent(this, HuespedAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_mensajeria) {
                startActivity(new Intent(this, MensajeriaAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_perfil) {
                startActivity(new Intent(this, PerfilAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void configurarFiltros() {
        // Configurar filtro por monto
        String[] opcionesMontos = {
                "Todos los montos",
                "Menos de S/. 200",
                "S/. 200 - S/. 500",
                "S/. 500 - S/. 1000",
                "Más de S/. 1000"
        };

        ArrayAdapter<String> adapterMonto = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opcionesMontos
        );

        binding.inputFilter.setAdapter(adapterMonto);
        binding.inputFilter.setText(filtroMontoActual, false);

        // Listener para filtro de monto
        binding.inputFilter.setOnItemClickListener((parent, view, position, id) -> {
            filtroMontoActual = opcionesMontos[position];
            aplicarFiltros();
        });

        // Buscador en tiempo real
        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                busquedaActual = s.toString().trim();
                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void aplicarFiltros() {
        List<Checkout> checkoutsFiltrados = new ArrayList<>();

        for (Checkout checkout : checkoutsOriginales) {
            boolean pasaFiltros = true;

            // Filtro de búsqueda: código de reserva O nombre de cliente
            if (!busquedaActual.isEmpty()) {
                String busquedaLower = busquedaActual.toLowerCase();
                String nombreLower = checkout.getNombre().toLowerCase();
                String codigoReserva = checkout.getCodigoReserva().toLowerCase();

                if (!nombreLower.contains(busquedaLower) &&
                        !codigoReserva.contains(busquedaLower)) {
                    pasaFiltros = false;
                }
            }

            // Filtro por monto
            if (pasaFiltros && !filtroMontoActual.equals("Todos los montos")) {
                pasaFiltros = aplicarFiltroMonto(checkout, filtroMontoActual);
            }

            if (pasaFiltros) {
                checkoutsFiltrados.add(checkout);
            }
        }

        // Actualizar adapter
        adapter.updateItems(checkoutsFiltrados);

        // Mostrar estadísticas y estado vacío
        mostrarEstadisticasFiltros(checkoutsFiltrados.size(), checkoutsOriginales.size());
        mostrarEstadoVacio(checkoutsFiltrados.isEmpty());
    }

    private boolean aplicarFiltroMonto(Checkout checkout, String filtro) {
        double monto = obtenerMontoNumerico(checkout.getMontoHospedaje());

        switch (filtro) {
            case "Menos de S/. 200":
                return monto < 200;
            case "S/. 200 - S/. 500":
                return monto >= 200 && monto <= 500;
            case "S/. 500 - S/. 1000":
                return monto > 500 && monto <= 1000;
            case "Más de S/. 1000":
                return monto > 1000;
            default:
                return true;
        }
    }

    private double obtenerMontoNumerico(String montoTexto) {
        try {
            String numeroLimpio = montoTexto.replaceAll("[^0-9.]", "");
            return numeroLimpio.isEmpty() ? 0.0 : Double.parseDouble(numeroLimpio);
        } catch (NumberFormatException e) {
            Log.w("CheckoutAdmin", "Error al parsear monto: " + montoTexto, e);
            return 0.0;
        }
    }

    private void mostrarEstadisticasFiltros(int resultados, int total) {
        TextView tvEstadisticas = binding.tvEstadisticas;

        if (total == 0) {
            tvEstadisticas.setVisibility(View.GONE);
            return;
        }

        if (!busquedaActual.isEmpty() || !filtroMontoActual.equals("Todos los montos")) {
            // Solo mostrar si hay filtros activos
            String mensaje = resultados + " de " + total + " checkouts encontrados";
            tvEstadisticas.setText(mensaje);
            tvEstadisticas.setVisibility(View.VISIBLE);
        } else {
            tvEstadisticas.setText(total + " checkouts pendientes");
            tvEstadisticas.setVisibility(View.VISIBLE);
        }
    }

    private void mostrarEstadoVacio(boolean mostrar) {
        if (mostrar) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.recyclerCheckout.setVisibility(View.GONE);

            // Personalizar mensaje según el estado
            TextView tvEmptyMessage = binding.tvEmptyMessage;
            if (checkoutsOriginales.isEmpty()) {
                tvEmptyMessage.setText("No hay checkouts pendientes");
            } else {
                tvEmptyMessage.setText("No se encontraron resultados");
            }
        } else {
            binding.emptyStateLayout.setVisibility(View.GONE);
            binding.recyclerCheckout.setVisibility(View.VISIBLE);
        }
    }

    private void cargarCheckoutsPendientes(String hotelId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Date hoy = obtenerSoloFecha(new Date());

        Log.d("CheckoutAdmin", "Cargando checkouts pendientes para hotel: " + hotelId);

        // Limpiar listas
        checkouts.clear();
        checkoutsOriginales.clear();
        adapter.notifyDataSetChanged();

        db.collection("reservas")
                .whereEqualTo("idHotel", hotelId)
                .whereEqualTo("estado", "sin checkout")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    Log.d("CheckoutAdmin", "Total reservas sin checkout: " + querySnapshots.size());

                    if (querySnapshots.isEmpty()) {
                        mostrarEstadoVacio(true);
                        mostrarEstadisticasFiltros(0, 0);
                        return;
                    }

                    int checkoutsProcesados = 0;

                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        try {
                            Reserva reserva = doc.toObject(Reserva.class);
                            reserva.setId(doc.getId());

                            if (reserva.getFechaFin() != null) {
                                Date fechaSalida = obtenerSoloFecha(reserva.getFechaFin().toDate());

                                // Incluir checkouts de hoy y días anteriores (pendientes)
                                if (fechaSalida.equals(hoy) || fechaSalida.before(hoy)) {
                                    checkoutsProcesados++;
                                    cargarDatosUsuarioYCrearCheckout(db, reserva);
                                }
                            }
                        } catch (Exception e) {
                            Log.e("CheckoutAdmin", "Error procesando reserva: " + doc.getId(), e);
                        }
                    }

                    Log.d("CheckoutAdmin", "Checkouts pendientes procesados: " + checkoutsProcesados);

                    if (checkoutsProcesados == 0) {
                        mostrarEstadoVacio(true);
                        mostrarEstadisticasFiltros(0, 0);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar checkouts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("CheckoutAdmin", "Error Firestore", e);
                    mostrarEstadoVacio(true);
                });
    }

    private void cargarDatosUsuarioYCrearCheckout(FirebaseFirestore db, Reserva reserva) {
        String idUsuario = reserva.getIdUsuario();

        if (idUsuario == null || idUsuario.isEmpty()) {
            Log.w("CheckoutAdmin", "ID de usuario nulo o vacío para reserva: " + reserva.getId());
            crearCheckoutSinUsuario(reserva);
            return;
        }

        db.collection("usuarios")
                .document(idUsuario)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String nombreCompleto = "Huésped";
                    if (userDoc.exists()) {
                        String nombres = userDoc.getString("nombres");
                        String apellidos = userDoc.getString("apellidos");

                        if (nombres != null || apellidos != null) {
                            nombreCompleto = ((nombres != null ? nombres : "") + " " +
                                    (apellidos != null ? apellidos : "")).trim();
                        }
                    }

                    if (nombreCompleto.isEmpty()) {
                        nombreCompleto = "Huésped";
                    }

                    crearYAgregarCheckout(nombreCompleto, reserva);
                })
                .addOnFailureListener(e -> {
                    Log.w("CheckoutAdmin", "Error al cargar usuario " + idUsuario + ", usando nombre genérico", e);
                    crearCheckoutSinUsuario(reserva);
                });
    }

    private void crearCheckoutSinUsuario(Reserva reserva) {
        crearYAgregarCheckout("Huésped", reserva);
    }

    private void crearYAgregarCheckout(String nombreCompleto, Reserva reserva) {
        // Usar directamente el costoTotal de la reserva (ya está bien calculado)
        String costoReserva = reserva.getCostoTotal() != null ? reserva.getCostoTotal() : "0";
        String daniosTotales = "0";

        // Calcular solo los daños adicionales
        if (reserva.getDanios() != null && !reserva.getDanios().isEmpty()) {
            double totalDanios = 0;
            for (Reserva.Danio danio : reserva.getDanios()) {
                try {
                    if (danio.getPrecio() != null) {
                        // Remover símbolos de moneda y convertir a número
                        String precio = danio.getPrecio().replaceAll("[^0-9.]", "");
                        if (!precio.isEmpty()) {
                            totalDanios += Double.parseDouble(precio);
                        }
                    }
                } catch (NumberFormatException e) {
                    Log.w("CheckoutAdmin", "Error al parsear precio de daño", e);
                }
            }
            daniosTotales = String.format("%.2f", totalDanios);
        }

        // El total final es simplemente el costo de la reserva (por ahora)
        // Más adelante, cuando implementes la actualización por daños, será: costoReserva + daños
        String totalFinal = costoReserva;

        Checkout checkout = new Checkout(
                nombreCompleto,
                reserva.getId(),
                costoReserva, // Costo original de la reserva
                "•••• 1234", // dato simulado por ahora
                "S/. " + daniosTotales, // Daños adicionales
                totalFinal, // Por ahora igual al costo de reserva
                R.drawable.img_guest_placeholder,
                reserva // Pasamos la reserva completa
        );

        // Agregar a lista original
        checkoutsOriginales.add(checkout);

        // Aplicar filtros
        aplicarFiltros();

        Log.d("CheckoutAdmin", "Checkout agregado: " + nombreCompleto +
                " - Reserva: " + reserva.getId() +
                " - Costo: " + costoReserva +
                " - Daños: S/. " + daniosTotales);
    }

    private Date obtenerSoloFecha(Date dateTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTime);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private void inicializarVistas() {
        // Nuevas vistas para notificaciones
        notificationIcon = findViewById(R.id.notification_icon);
        badgeText = findViewById(R.id.badge_text);

        // Inicializar servicio de notificaciones
        notificacionService = new NotificacionService(this);
    }

    private void configurarViewModel() {
        // ViewModel
        viewModel = new ViewModelProvider(this).get(AdminHotelViewModel.class);

        // Observar notificaciones de checkout
        viewModel.getContadorNotificaciones().observe(this, contador -> {
            actualizarBadgeNotificaciones(contador);
        });

        // Cargar notificaciones al iniciar
        viewModel.cargarNotificacionesCheckoutTiempoReal();

        // Iniciar actualizaciones automáticas cada 5 minutos
        viewModel.iniciarActualizacionAutomatica();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener actualizaciones automáticas
        if (viewModel != null) {
            viewModel.detenerActualizacionAutomatica();
        }
    }

    private void configurarToolbar() {
        // Configurar click del icono de notificaciones
        notificationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificacionesAdminActivity.class);
            startActivity(intent);
        });
    }

    private void actualizarBadgeNotificaciones(Integer contador) {
        if (contador != null && contador > 0) {
            badgeText.setVisibility(View.VISIBLE);
            badgeText.setText(contador > 99 ? "99+" : String.valueOf(contador));
        } else {
            badgeText.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar notificaciones cuando regresamos a esta activity
        if (viewModel != null) {
            viewModel.cargarNotificacionesCheckoutTiempoReal();
        }
    }

    // Método opcional para limpiar filtros
    private void limpiarFiltros() {
        binding.inputSearch.setText("");
        binding.inputFilter.setText("Todos los montos", false);
        busquedaActual = "";
        filtroMontoActual = "Todos los montos";
        aplicarFiltros();
    }
}