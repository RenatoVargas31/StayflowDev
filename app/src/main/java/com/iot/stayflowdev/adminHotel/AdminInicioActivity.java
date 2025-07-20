package com.iot.stayflowdev.adminHotel;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.model.NotificacionCheckout;
import com.iot.stayflowdev.adminHotel.repository.AdminHotelViewModel;
import com.iot.stayflowdev.adminHotel.service.NotificacionService;

public class AdminInicioActivity extends AppCompatActivity {

    private TextView tvNombreAdmin;
    private TextView tvNombreHotel;
    private TextInputEditText etDescripcionHotel;
    private MaterialButton btnGuardarDescripcion;
    private String descripcionOriginal = "";

    // Nuevas variables para notificaciones
    private ImageView notificationIcon;
    private TextView badgeText;
    private AdminHotelViewModel viewModel;
    private NotificacionService notificacionService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_inicio);

        solicitarPermisoNotificaciones();
        inicializarVistas();
        configurarViewModel();
        configurarAccesosRapidos();
        configurarToolbar();
        configurarBottomNavigation();

        observarNotificacionesSistema();

    }

    private void inicializarVistas() {
        // Vistas existentes
        tvNombreAdmin = findViewById(R.id.tvNombreAdmin);
        tvNombreHotel = findViewById(R.id.tvNombreHotel);
        etDescripcionHotel = findViewById(R.id.etDescripcionHotel);
        btnGuardarDescripcion = findViewById(R.id.btnGuardarDescripcion);
        btnGuardarDescripcion.setEnabled(false);

        // Nuevas vistas para notificaciones
        notificationIcon = findViewById(R.id.notification_icon);
        badgeText = findViewById(R.id.badge_text);

        // Inicializar servicio de notificaciones
        notificacionService = new NotificacionService(this);
    }

    private void configurarViewModel() {
        // ViewModel
        viewModel = new ViewModelProvider(this).get(AdminHotelViewModel.class);

        viewModel.getNombreAdmin().observe(this, nombre -> {
            tvNombreAdmin.setText("Hola, " + nombre);
        });

        viewModel.getNombreHotel().observe(this, nombreHotel -> {
            tvNombreHotel.setText(nombreHotel);
        });

        viewModel.getDescripcionHotel().observe(this, descripcion -> {
            descripcionOriginal = (descripcion != null) ? descripcion : "";
            etDescripcionHotel.setText(descripcionOriginal);
            btnGuardarDescripcion.setEnabled(false);
        });

        // Observar notificaciones de checkout
        viewModel.getContadorNotificaciones().observe(this, contador -> {
            actualizarBadgeNotificaciones(contador);
        });

        // Configurar TextWatcher para descripción
        etDescripcionHotel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String actual = s.toString().trim();
                btnGuardarDescripcion.setEnabled(!actual.equals(descripcionOriginal));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnGuardarDescripcion.setOnClickListener(v -> {
            String nuevaDescripcion = etDescripcionHotel.getText().toString().trim();
            viewModel.actualizarDescripcionHotel(nuevaDescripcion);
            descripcionOriginal = nuevaDescripcion;
            btnGuardarDescripcion.setEnabled(false);
            Toast.makeText(this, "Descripción actualizada", Toast.LENGTH_SHORT).show();
        });

        // Cargar notificaciones al iniciar
        viewModel.cargarNotificacionesCheckout();

        // Configurar card de mensajería de pruebas
        configurarMensajeriaPruebas();

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

    // Métodos llamados desde el XML via android:onClick
    public void irUbicacion(View view) {
        startActivity(new Intent(this, UbicacionAdminActivity.class));
    }

    public void irGaleria(View view) {
        startActivity(new Intent(this, GaleriaAdminActivity.class));
    }

    public void irHabitaciones(View view) {
        startActivity(new Intent(this, HabitacionesAdminActivity.class));
    }

    public void irServicios(View view) {
        startActivity(new Intent(this, ServiciosAdminActivity.class));
    }

    private void configurarAccesosRapidos() {
        configurarAcceso(R.id.accesoUbicacion, R.drawable.ic_map, "Ubicación", UbicacionAdminActivity.class);
        configurarAcceso(R.id.accesoGaleria, R.drawable.ic_gallery, "Galería", GaleriaAdminActivity.class);
        configurarAcceso(R.id.accesoHabitaciones, R.drawable.ic_rooms, "Habitaciones", HabitacionesAdminActivity.class);
        configurarAcceso(R.id.accesoServicios, R.drawable.ic_services, "Servicios", ServiciosAdminActivity.class);
    }

    private void configurarAcceso(int includeId, int iconResId, String titulo, Class<?> destino) {
        View acceso = findViewById(includeId);
        ImageView icono = acceso.findViewById(R.id.icono);
        TextView texto = acceso.findViewById(R.id.texto);

        icono.setImageResource(iconResId);
        texto.setText(titulo);

        acceso.setOnClickListener(v -> startActivity(new Intent(this, destino)));
    }

    private void configurarBottomNavigation() {
        // Bottom Nav
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.menu_inicio);
        bottomNav.setOnItemSelectedListener(item -> {
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
            overridePendingTransition(0, 0);
            return true;
        });
    }

    private void configurarMensajeriaPruebas() {
        View cardMensajeriaPruebas = findViewById(R.id.cardMensajeriaPruebas);

        cardMensajeriaPruebas.setOnClickListener(v -> {
            // Abrir el chat con el Super Admin
            Intent intent = new Intent(this, ChatSuperAdminActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    private void solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
    }

    private void observarNotificacionesSistema() {
        boolean yaMostrada = getSharedPreferences("notificaciones", MODE_PRIVATE)
                .getBoolean("mostrada", false);

        if (!yaMostrada) {
            viewModel.getNotificacionesCheckout().observe(this, notificaciones -> {
                if (notificaciones != null && !notificaciones.isEmpty()) {
                    NotificacionCheckout ultima = notificaciones.get(0);
                    mostrarNotificacionSistema(
                            ultima.getTituloNotificacion(),
                            ultima.getMensajeNotificacion()
                    );

                    // Marcar que ya fue mostrada
                    getSharedPreferences("notificaciones", MODE_PRIVATE)
                            .edit()
                            .putBoolean("mostrada", true)
                            .apply();
                }
            });

            viewModel.cargarNotificacionesCheckout(); // fuerza la carga
        }
    }


    private void mostrarNotificacionSistema(String titulo, String mensaje) {
        String canalId = "canal_checkout";
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Crear canal si no existe (solo una vez)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    canalId,
                    "Notificaciones Checkout",
                    NotificationManager.IMPORTANCE_HIGH
            );
            canal.setDescription("Notificaciones de checkout");
            canal.enableLights(true);
            canal.enableVibration(true);
            manager.createNotificationChannel(canal);
        }

        // Intent para abrir la misma actividad (opcional: podrías ir a CheckoutDetalle)
        Intent intent = new Intent(this, NotificacionesAdminActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, canalId)
                .setSmallIcon(R.drawable.ic_notification) // asegúrate de tener un ícono
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        manager.notify(1001, builder.build());
    }

}