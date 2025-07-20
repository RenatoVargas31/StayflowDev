package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.LoginFireBaseActivity;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.repository.AdminHotelViewModel;
import com.iot.stayflowdev.adminHotel.service.NotificacionService;
import com.iot.stayflowdev.databinding.ActivityPerfilAdminBinding;
import com.iot.stayflowdev.model.User;

public class PerfilAdminActivity extends AppCompatActivity {

    private ActivityPerfilAdminBinding binding;
    private FirebaseAuth mAuth;
    private static final String TAG = "PerfilAdmin";

    private ImageView notificationIcon;
    private TextView badgeText;
    private AdminHotelViewModel viewModel;
    private NotificacionService notificacionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPerfilAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inicializarVistas();
        configurarViewModel();
        configurarToolbar();

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();

            db.collection("usuarios")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            Log.d(TAG, "Snapshot data: " + snapshot.getData()); // Log para depuración

                            User usuario = snapshot.toObject(User.class);
                            if (usuario != null) {
                                String nombreCompleto = usuario.getNombres() + " " + usuario.getApellidos();
                                binding.tvNombreCompleto.setText(nombreCompleto);
                                binding.tvNombreAdmin.setText(nombreCompleto);


                                if (usuario.getEmail() != null) {
                                    binding.tvCorreoElectronico.setText(usuario.getEmail());
                                } else {
                                    Log.w(TAG, "El campo 'correo' es null en Firestore");
                                    binding.tvCorreoElectronico.setText("Correo no disponible");
                                }
                            } else {
                                Log.e(TAG, "No se pudo convertir snapshot a User");
                            }
                        } else {
                            Log.e(TAG, "Documento de usuario no encontrado");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al obtener datos del usuario", e);
                        binding.tvNombreCompleto.setText("Error al cargar datos");
                        binding.tvCorreoElectronico.setText("Error");
                    });
        }

        // Configurar Toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Perfil");

        // Configurar menú inferior
        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.setSelectedItemId(R.id.menu_perfil);
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

        // Cerrar sesión
        binding.btnCerrarSesion.setOnClickListener(v -> {
            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            Intent intent = new Intent(this, LoginFireBaseActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        getSharedPreferences("notificaciones", MODE_PRIVATE)
                .edit()
                .remove("mostrada")
                .apply();

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
