package com.iot.stayflowdev.adminHotel.huesped;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.AdminInicioActivity;
import com.iot.stayflowdev.adminHotel.HuespedAdminActivity;
import com.iot.stayflowdev.adminHotel.MensajeriaAdminActivity;
import com.iot.stayflowdev.adminHotel.NotificacionesAdminActivity;
import com.iot.stayflowdev.adminHotel.PerfilAdminActivity;
import com.iot.stayflowdev.adminHotel.ReportesAdminActivity;
import com.iot.stayflowdev.adminHotel.adapter.TaxiAdapter;
import com.iot.stayflowdev.adminHotel.model.Taxi;
import com.iot.stayflowdev.adminHotel.repository.AdminHotelViewModel;
import com.iot.stayflowdev.adminHotel.service.NotificacionService;
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
    private ImageView notificationIcon;
    private TextView badgeText;
    private AdminHotelViewModel viewModel;
    private NotificacionService notificacionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaxiAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inicializarVistas();
        configurarViewModel();
        configurarToolbar();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Solicitudes de Taxi");

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

                        String codigoReserva = doc.getString("reservaId");
                        if (codigoReserva == null || codigoReserva.isEmpty()) {
                            codigoReserva = doc.getId().substring(0, 8).toUpperCase();
                        }


                        String detalleViaje = "";
                        if (doc.getTimestamp("fechaSalida") != null) {
                            detalleViaje = new SimpleDateFormat("dd-MM-yy hh:mm a", Locale.getDefault())
                                    .format(doc.getTimestamp("fechaSalida").toDate());
                        }

                        String idTaxista = doc.getString("idTaxista");

                        Taxi taxi = new Taxi(nombre, codigoReserva, estado, detalleViaje, direccionResumen, R.drawable.img_guest_placeholder, idTaxista);
                        taxis.add(taxi);

                    }
                    aplicarFiltros();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar solicitudes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
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

        // CORREGIR: Agregar elemento vacío al inicio para "Todos los estados"
        String[] estados = {"", "Pendiente", "aceptada", "en camino", "llegado", "finalizada", "cancelada"};
        String[] estadosDisplay = {"Todos los estados", "Pendiente", "Aceptada", "En camino", "Llegado", "Finalizada", "Cancelada"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, estadosDisplay);
        binding.inputFilter.setAdapter(adapter);

        binding.inputFilter.setOnItemClickListener((parent, view, position, id) -> {
            estadoSeleccionado = estados[position]; // Ahora los índices coinciden
            aplicarFiltros();
        });

        // Configurar botón limpiar filtros
        binding.btnClearFilters.setOnClickListener(v -> limpiarFiltros());
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

        // Actualizar contador de resultados
        actualizarContadorResultados(filtrados.size());

        // Mostrar/ocultar estado vacío
        if (filtrados.isEmpty()) {
            binding.recyclerTaxi.setVisibility(View.GONE);
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerTaxi.setVisibility(View.VISIBLE);
            binding.emptyStateLayout.setVisibility(View.GONE);
        }

        // Actualizar adapter
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

        // Mostrar estadísticas
        binding.tvEstadisticas.setText(texto);
        binding.tvEstadisticas.setVisibility(View.VISIBLE);
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
}