package com.iot.stayflowdev.adminHotel.huesped;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private CheckoutAdapter adapter;
    private ImageView notificationIcon;
    private TextView badgeText;
    private AdminHotelViewModel viewModel;
    private NotificacionService notificacionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inicializarVistas();
        configurarViewModel();
        configurarToolbar();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Checkout");

        // Inicializar el adapter primero
        adapter = new CheckoutAdapter(checkouts, this);
        binding.recyclerCheckout.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerCheckout.setAdapter(adapter);

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

        AdminHotelViewModel viewModel = new ViewModelProvider(this).get(AdminHotelViewModel.class);
        viewModel.getHotelId().observe(this, hotelId -> {
            if (hotelId != null && !hotelId.isEmpty()) {
                cargarCheckoutsDeHoy(hotelId);
            }
        });
    }

    private void cargarCheckoutsDeHoy(String hotelId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Date hoy = obtenerSoloFecha(new Date()); // fecha actual sin hora
        Date manana = obtenerSoloFecha(new Date(hoy.getTime() + 24 * 60 * 60 * 1000)); // día siguiente

        Log.d("CheckoutAdmin", "Buscando checkouts para fecha: " + hoy);

        // Limpiar la lista antes de cargar nuevos datos
        checkouts.clear();
        adapter.notifyDataSetChanged();

        db.collection("reservas")
                .whereEqualTo("idHotel", hotelId)
                .whereEqualTo("estado", "sin checkout") // Solo reservas sin checkout
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    Log.d("CheckoutAdmin", "Total reservas encontradas: " + querySnapshots.size());

                    if (querySnapshots.isEmpty()) {
                        Toast.makeText(this, "No hay checkouts para hoy", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int reservasCheckoutHoy = 0;

                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        try {
                            Reserva reserva = doc.toObject(Reserva.class);
                            reserva.setId(doc.getId()); // Asegurar que el ID esté establecido

                            if (reserva.getFechaFin() != null) {
                                Date fechaSalida = obtenerSoloFecha(reserva.getFechaFin().toDate());

                                Log.d("CheckoutAdmin", "Reserva " + reserva.getId() +
                                        " - Fecha salida: " + fechaSalida +
                                        " - Fecha hoy: " + hoy +
                                        " - Son iguales: " + fechaSalida.equals(hoy));

                                // Verificar si la fecha de salida es hoy
                                if (fechaSalida.equals(hoy)) {
                                    reservasCheckoutHoy++;
                                    cargarDatosUsuarioYCrearCheckout(db, reserva);
                                }
                            }
                        } catch (Exception e) {
                            Log.e("CheckoutAdmin", "Error procesando reserva: " + doc.getId(), e);
                        }
                    }

                    Log.d("CheckoutAdmin", "Reservas con checkout hoy: " + reservasCheckoutHoy);

                    if (reservasCheckoutHoy == 0) {
                        Toast.makeText(this, "No hay checkouts programados para hoy", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar checkouts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("CheckoutAdmin", "Error Firestore", e);
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
        // Calcular daños totales
        String costoTotal = reserva.getCostoTotal() != null ? reserva.getCostoTotal() : "0";
        String daniosTotales = "0";

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
            daniosTotales = String.valueOf(totalDanios);
        }

        // Calcular total final
        double costoNumerico = 0;
        double daniosNumerico = 0;

        try {
            costoNumerico = Double.parseDouble(costoTotal.replaceAll("[^0-9.]", ""));
            daniosNumerico = Double.parseDouble(daniosTotales);
        } catch (NumberFormatException e) {
            Log.w("CheckoutAdmin", "Error al parsear costos", e);
        }

        double totalFinal = costoNumerico + daniosNumerico;

        Checkout checkout = new Checkout(
                nombreCompleto,
                reserva.getId(),
                "S/. " + costoTotal,
                "•••• 1234", // dato simulado por ahora
                "S/. " + daniosTotales,
                "S/. " + String.valueOf(totalFinal),
                R.drawable.img_guest_placeholder,
                reserva // Pasamos la reserva completa
        );

        // Agregar al adapter usando el método personalizado
        adapter.addItem(checkout);
        Log.d("CheckoutAdmin", "Checkout agregado: " + nombreCompleto + " - Reserva: " + reserva.getId());
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
        viewModel.cargarNotificacionesCheckout();

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
            viewModel.cargarNotificacionesCheckout();
        }
    }
}