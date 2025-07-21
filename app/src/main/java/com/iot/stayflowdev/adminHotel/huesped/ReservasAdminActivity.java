package com.iot.stayflowdev.adminHotel.huesped;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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
import com.iot.stayflowdev.adminHotel.adapter.ReservaAdapter;
import com.iot.stayflowdev.adminHotel.service.NotificacionService;
import com.iot.stayflowdev.model.Reserva;
import com.iot.stayflowdev.adminHotel.repository.AdminHotelViewModel;
import com.iot.stayflowdev.databinding.ActivityReservasAdminBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservasAdminActivity extends AppCompatActivity {

    private ActivityReservasAdminBinding binding;
    private List<Reserva> reservasOriginales = new ArrayList<>(); // Lista completa sin filtrar
    private List<Reserva> reservasFiltradas = new ArrayList<>(); // Lista que se muestra
    private ReservaAdapter adapter;

    // Variables para filtros
    private Date fechaInicioFiltro = null;
    private Date fechaFinFiltro = null;
    private String textoBusqueda = "";
    private boolean ordenarPorRecientes = true; // true = recientes, false = antiguas
    private ImageView notificationIcon;
    private TextView badgeText;
    private AdminHotelViewModel viewModel;
    private NotificacionService notificacionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReservasAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inicializarVistas();
        configurarViewModel();
        configurarToolbar();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Reservas");

        configurarBottomNavigation();
        configurarRecyclerView();
        configurarFiltrosYBusqueda();

        AdminHotelViewModel adminHotelViewModel = new ViewModelProvider(this).get(AdminHotelViewModel.class);
        adminHotelViewModel.getHotelId().observe(this, hotelId -> {
            if (hotelId != null && !hotelId.isEmpty()) {
                cargarReservasDesdeFirestore(hotelId);
            } else {
                Log.e("ReservasAdmin", "Hotel ID no disponible.");
            }
        });
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

    private void configurarRecyclerView() {
        adapter = new ReservaAdapter(reservasFiltradas, this);
        binding.recyclerReservas.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerReservas.setAdapter(adapter);
    }

    private void configurarFiltrosYBusqueda() {
        // Configurar chips de ordenamiento
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

        // Por defecto, seleccionar "Más recientes"
        binding.chipRecientes.setChecked(true);

        // Configurar botón de filtrar por fechas
        binding.btnFiltrarFechas.setOnClickListener(v -> mostrarDialogoFechas());

        // Configurar botón de quitar filtro
        binding.btnQuitarFiltro.setOnClickListener(v -> quitarFiltroFechas());

        // Configurar buscador
        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                textoBusqueda = s.toString().trim();
                aplicarFiltros();
            }
        });
    }

    private void mostrarDialogoFechas() {
        Calendar calendar = Calendar.getInstance();

        // Seleccionar fecha de inicio
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar fechaInicio = Calendar.getInstance();
            fechaInicio.set(year, month, dayOfMonth, 0, 0, 0);
            fechaInicioFiltro = fechaInicio.getTime();

            // Ahora seleccionar fecha de fin
            new DatePickerDialog(this, (view2, year2, month2, dayOfMonth2) -> {
                Calendar fechaFin = Calendar.getInstance();
                fechaFin.set(year2, month2, dayOfMonth2, 23, 59, 59);
                fechaFinFiltro = fechaFin.getTime();

                // Validar que fecha fin sea después de fecha inicio
                if (fechaFinFiltro.before(fechaInicioFiltro)) {
                    Toast.makeText(this, "La fecha de fin debe ser posterior a la fecha de inicio", Toast.LENGTH_SHORT).show();
                    fechaInicioFiltro = null;
                    fechaFinFiltro = null;
                } else {
                    aplicarFiltros();
                    actualizarTextoBotonFiltro();
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void quitarFiltroFechas() {
        fechaInicioFiltro = null;
        fechaFinFiltro = null;
        binding.btnFiltrarFechas.setText("Filtrar por fechas");
        aplicarFiltros();
    }

    private void actualizarTextoBotonFiltro() {
        if (fechaInicioFiltro != null && fechaFinFiltro != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            String texto = sdf.format(fechaInicioFiltro) + " - " + sdf.format(fechaFinFiltro);
            binding.btnFiltrarFechas.setText(texto);
        } else {
            binding.btnFiltrarFechas.setText("Filtrar por fechas");
        }
    }

    private void aplicarFiltros() {
        reservasFiltradas.clear();

        for (Reserva reserva : reservasOriginales) {
            boolean cumpleFiltros = true;

            // Filtro por búsqueda (ID de reserva o código)
            if (!textoBusqueda.isEmpty()) {
                String id = reserva.getId() != null ? reserva.getId().toLowerCase() : "";
                if (!id.contains(textoBusqueda.toLowerCase())) {
                    cumpleFiltros = false;
                }
            }

            // Filtro por fechas
            if (cumpleFiltros && fechaInicioFiltro != null && fechaFinFiltro != null) {
                Date fechaInicioReserva = reserva.getFechaInicio() != null ?
                        reserva.getFechaInicio().toDate() : null;

                if (fechaInicioReserva == null ||
                        fechaInicioReserva.before(fechaInicioFiltro) ||
                        fechaInicioReserva.after(fechaFinFiltro)) {
                    cumpleFiltros = false;
                }
            }

            if (cumpleFiltros) {
                reservasFiltradas.add(reserva);
            }
        }

        // Ordenar según el chip seleccionado
        ordenarReservas();

        // Notificar al adapter
        adapter.notifyDataSetChanged();

        // Mostrar mensaje si no hay resultados
        if (reservasFiltradas.isEmpty() && !reservasOriginales.isEmpty()) {
            Toast.makeText(this, "No se encontraron reservas con los filtros aplicados", Toast.LENGTH_SHORT).show();
        }
    }

    private void ordenarReservas() {
        Collections.sort(reservasFiltradas, (r1, r2) -> {
            Date fecha1 = r1.getFechaInicio() != null ? r1.getFechaInicio().toDate() : new Date(0);
            Date fecha2 = r2.getFechaInicio() != null ? r2.getFechaInicio().toDate() : new Date(0);

            if (ordenarPorRecientes) {
                return fecha2.compareTo(fecha1); // Más recientes primero
            } else {
                return fecha1.compareTo(fecha2); // Más antiguas primero
            }
        });
    }

    private void cargarReservasDesdeFirestore(String hotelId) {
        FirebaseFirestore.getInstance()
                .collection("reservas")
                .whereEqualTo("idHotel", hotelId)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    reservasOriginales.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        Reserva reserva = doc.toObject(Reserva.class);
                        reservasOriginales.add(reserva);
                    }
                    aplicarFiltros(); // Aplicar filtros después de cargar
                })
                .addOnFailureListener(e -> {
                    Log.e("ReservasAdmin", "Error al obtener reservas", e);
                    Toast.makeText(this, "Error al cargar reservas", Toast.LENGTH_SHORT).show();
                });
    }

    private void inicializarVistas() {
        // Vistas existentes

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