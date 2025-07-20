package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.AdminInicioActivity;
import com.iot.stayflowdev.adminHotel.HuespedAdminActivity;
import com.iot.stayflowdev.adminHotel.PerfilAdminActivity;
import com.iot.stayflowdev.adminHotel.ReportesAdminActivity;
import com.iot.stayflowdev.adminHotel.adapter.MensajeAdapter;
import com.iot.stayflowdev.adminHotel.model.Mensaje;
import com.iot.stayflowdev.adminHotel.repository.AdminHotelViewModel;
import com.iot.stayflowdev.adminHotel.service.NotificacionService;
import com.iot.stayflowdev.databinding.ActivityMensajeriaAdminBinding;

import java.util.ArrayList;
import java.util.List;

public class MensajeriaAdminActivity extends AppCompatActivity {

    private ActivityMensajeriaAdminBinding binding;
    private List<Mensaje> mensajes = new ArrayList<>();
    private ImageView notificationIcon;
    private TextView badgeText;
    private AdminHotelViewModel viewModel;
    private NotificacionService notificacionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMensajeriaAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inicializarVistas();
        configurarViewModel();
        configurarToolbar();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Mensajería");

        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.setSelectedItemId(R.id.menu_mensajeria);
        // Guarda una referencia a la actividad actual
        final Class<?> currentActivity = this.getClass();

        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = null;
            int id = item.getItemId();

            if (id == R.id.menu_inicio && currentActivity != AdminInicioActivity.class) {
                intent = new Intent(this, AdminInicioActivity.class);
            } else if (id == R.id.menu_reportes && currentActivity != ReportesAdminActivity.class) {
                intent = new Intent(this, ReportesAdminActivity.class);
            } else if (id == R.id.menu_huesped && currentActivity != HuespedAdminActivity.class) {
                intent = new Intent(this, HuespedAdminActivity.class);
            } else if (id == R.id.menu_mensajeria && currentActivity != MensajeriaAdminActivity.class) {
                intent = new Intent(this, MensajeriaAdminActivity.class);
            } else if (id == R.id.menu_perfil && currentActivity != PerfilAdminActivity.class) {
                intent = new Intent(this, PerfilAdminActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }

            return true;
        });

        cargarMensajes();

        MensajeAdapter adapter = new MensajeAdapter(mensajes, this);
        binding.recyclerMensajes.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerMensajes.setAdapter(adapter);
    }

    private void cargarMensajes() {
        mensajes.add(new Mensaje("Ana Bonino", "¿A qué hora puedo hacer check-in?", R.drawable.img_guest_placeholder));
        mensajes.add(new Mensaje("Luis Ramírez", "¿Dónde se recoge la llave?", R.drawable.img_guest_placeholder));
        mensajes.add(new Mensaje("María López", "¿Tienen estacionamiento disponible?", R.drawable.img_guest_placeholder));
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
