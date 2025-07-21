package com.iot.stayflowdev.adminHotel;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.adapter.NotificacionesAdapter;
import com.iot.stayflowdev.adminHotel.huesped.CheckoutDetalleActivity;
import com.iot.stayflowdev.adminHotel.model.NotificacionCheckout;
import com.iot.stayflowdev.adminHotel.repository.AdminHotelViewModel;
import com.iot.stayflowdev.model.Reserva;

import java.util.ArrayList;
import java.util.List;

public class NotificacionesAdminActivity extends AppCompatActivity {

    private RecyclerView rvNotificaciones;
    private NotificacionesAdapter adapter;
    private AdminHotelViewModel viewModel;
    private TextView tvSinNotificaciones; // Volvemos a TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones_admin);

        inicializarVistas();
        configurarToolbar();
        configurarRecyclerView();
        configurarViewModel();
        configurarBottomNavigation();
    }

    private void inicializarVistas() {
        rvNotificaciones = findViewById(R.id.rv_notificaciones);
        tvSinNotificaciones = findViewById(R.id.tv_sin_notificaciones); // Ahora sí es TextView

        // Configurar el click del icono de notificaciones en toolbar
        ImageView notificationIcon = findViewById(R.id.notification_icon);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> {
                // Ya estamos en esta activity, no hacer nada o mostrar alguna animación
            });
        }
    }

    private void configurarToolbar() {
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Notificaciones");
        }

        // Asignar acción al icono de retroceso
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Ocultar badge (si aplica)
        TextView badgeText = findViewById(R.id.badge_text);
        if (badgeText != null) {
            badgeText.setVisibility(View.GONE);
        }
    }

    private void configurarRecyclerView() {
        adapter = new NotificacionesAdapter(new ArrayList<>(), this::onNotificacionClick);
        rvNotificaciones.setLayoutManager(new LinearLayoutManager(this));
        rvNotificaciones.setAdapter(adapter);
    }

    private void configurarViewModel() {
        viewModel = new ViewModelProvider(this).get(AdminHotelViewModel.class);

        // Observar notificaciones de checkout
        viewModel.getNotificacionesCheckout().observe(this, notificaciones -> {
            if (notificaciones != null && !notificaciones.isEmpty()) {
                adapter.actualizarNotificaciones(notificaciones);
                rvNotificaciones.setVisibility(View.VISIBLE);
                tvSinNotificaciones.setVisibility(View.GONE);

            } else {
                rvNotificaciones.setVisibility(View.GONE);
                tvSinNotificaciones.setVisibility(View.VISIBLE);
            }
        });


        // Cargar notificaciones
        viewModel.cargarNotificacionesCheckoutTiempoReal();
    }

    private void onNotificacionClick(NotificacionCheckout notificacion) {
        String reservaId = notificacion.getReservaId();

        FirebaseFirestore.getInstance()
                .collection("reservas")
                .document(reservaId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Reserva reserva = doc.toObject(Reserva.class);
                        if (reserva != null) {
                            Intent intent = new Intent(this, CheckoutDetalleActivity.class);
                            intent.putExtra("codigoReserva", reservaId);
                            intent.putExtra("nombre", notificacion.getNombreHuesped());
                            intent.putExtra("idUsuario", notificacion.getIdUsuario());
                            intent.putExtra("costoTotal", reserva.getCostoTotal());
                            intent.putExtra("tarjeta", "•••• 1234"); // si tienes el número real, agrégalo aquí

                            // Habitaciones
                            if (reserva.getHabitaciones() != null) {
                                ArrayList<String> tipos = new ArrayList<>();
                                ArrayList<Integer> cantidades = new ArrayList<>();
                                ArrayList<String> precios = new ArrayList<>();

                                for (Reserva.Habitacion h : reserva.getHabitaciones()) {
                                    tipos.add(h.getTipo());
                                    cantidades.add(h.getCantidad());
                                    precios.add(h.getPrecio());
                                }

                                intent.putExtra("tiposHabitaciones", tipos.toArray(new String[0]));
                                intent.putExtra("cantidadesHabitaciones", cantidades.stream().mapToInt(i -> i).toArray());
                                intent.putExtra("preciosHabitaciones", precios.toArray(new String[0]));
                            }

                            // Servicios
                            if (reserva.getServicios() != null) {
                                ArrayList<String> nombres = new ArrayList<>();
                                ArrayList<String> precios = new ArrayList<>();

                                for (Reserva.Servicio s : reserva.getServicios()) {
                                    nombres.add(s.getNombre());
                                    precios.add(s.getPrecio());
                                }

                                intent.putExtra("nombresServicios", nombres.toArray(new String[0]));
                                intent.putExtra("preciosServicios", precios.toArray(new String[0]));
                            }

                            // Daños existentes
                            if (reserva.getDanios() != null) {
                                ArrayList<String> descripciones = new ArrayList<>();
                                ArrayList<String> montos = new ArrayList<>();

                                for (Reserva.Danio d : reserva.getDanios()) {
                                    descripciones.add(d.getDescripcion());
                                    montos.add(d.getPrecio());
                                }

                                intent.putExtra("descripcionesDaniosExistentes", descripciones.toArray(new String[0]));
                                intent.putExtra("preciosDaniosExistentes", montos.toArray(new String[0]));
                            }

                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Reserva no encontrada", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Documento no existe", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar reserva", Toast.LENGTH_SHORT).show();
                });
    }


    private void configurarBottomNavigation() {
        // Bottom Nav
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.menu_inicio);
        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = null;
            int id = item.getItemId();
            if (id == R.id.menu_inicio) {
                // Ya estamos aquí
                return true;
            } else if (id == R.id.menu_reportes) {
                startActivity(new Intent(this, ReportesAdminActivity.class));
                finish();
            } else if (id == R.id.menu_huesped) {
                startActivity(new Intent(this, HuespedAdminActivity.class));
                finish();
            } else if (id == R.id.menu_mensajeria) {
                startActivity(new Intent(this, MensajeriaAdminActivity.class));
                finish();
            } else if (id == R.id.menu_perfil) {
                startActivity(new Intent(this, PerfilAdminActivity.class));
                finish();
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }

            overridePendingTransition(0, 0);
            return true;
        });
    }
}