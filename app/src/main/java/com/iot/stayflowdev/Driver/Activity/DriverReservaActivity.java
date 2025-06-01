package com.iot.stayflowdev.Driver.Activity;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.iot.stayflowdev.Driver.Adapter.ReservasAdapter;
import com.iot.stayflowdev.Driver.Model.ReservaModel;
import com.iot.stayflowdev.R;

import java.util.ArrayList;
import java.util.List;

public class DriverReservaActivity extends AppCompatActivity implements ReservasAdapter.OnReservaClickListener {

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private ReservasAdapter adapter;
    private LinearLayout emptyView;
    private BottomNavigationView bottomNavigation;

    // Cambiar de Reserva a ReservaModel
    private List<ReservaModel> reservasEnCurso = new ArrayList<>();
    private List<ReservaModel> reservasPasadas = new ArrayList<>();
    private List<ReservaModel> reservasCanceladas = new ArrayList<>();
    private String channelId = "channelReservas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_reserva);

        // Configurar márgenes para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        crearCanalNotificacion();

        // Inicializar vistas
        inicializarVistas();

        // Configurar BottomNavigationView con navegación fluida
        configurarBottomNavigationFluido();
        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        // Añadir algo de espacio entre elementos
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Cargar datos de ejemplo
        cargarDatosDeEjemplo();

        // Inicializar adaptador con las reservas en curso por defecto
        adapter = new ReservasAdapter(this, reservasEnCurso, this);
        recyclerView.setAdapter(adapter);

        // Mostrar vista vacía si no hay reservas
        actualizarVistaVacia(reservasEnCurso);

        // Configurar listener del TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                cambiarDatosPorTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // No necesitamos hacer nada aquí
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // No necesitamos hacer nada aquí
            }
        });

        // ACTIVAR NOTIFICACIÓN AL FINAL
        mostrarNotificacionReservasActivas();
    }

    private void crearCanalNotificacion() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Canal notificaciones de reservas",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Canal para notificaciones de reservas con prioridad default");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            askPermission();
        }
    }

    private void askPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(DriverReservaActivity.this,
                    new String[]{POST_NOTIFICATIONS}, 102);
        }
    }

    // Este método ya está correcto según el PPT
    public void mostrarNotificacionReservasActivas() {
        int reservasActivas = reservasEnCurso.size();

        Intent intent = new Intent(this, DriverReservaActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_carro)
                .setContentTitle("Reservas Activas")
                .setContentText("Tienes " + reservasActivas + " reservas activas")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(100, builder.build());
        }
    }


    //  MÉTODO PARA INICIALIZAR VISTAS
    private void inicializarVistas() {
        tabLayout = findViewById(R.id.tabLayout);
        recyclerView = findViewById(R.id.recyclerViewReservas);
        emptyView = findViewById(R.id.empty_view);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    // CONFIGURACIÓN DE BOTTOM NAVIGATION CON ANIMACIÓN FLUIDA
    private void configurarBottomNavigationFluido() {
        if (bottomNavigation == null) {
            Log.e("DriverReservaActivity", "bottomNavigation es null");
            return;
        }

        // Establecer ítem seleccionado
        bottomNavigation.setSelectedItemId(R.id.nav_reservas);

        // Configurar listener con navegación sin animación
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Evitar acción si ya estamos en la actividad actual
            if (itemId == R.id.nav_reservas) {
                return true;
            }

            // Navegación según el ítem seleccionado
            if (itemId == R.id.nav_inicio) {
                navegarSinAnimacion(DriverInicioActivity.class);
                return true;
            } else if (itemId == R.id.nav_mapa) {
                navegarSinAnimacion(DriverMapaActivity.class);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                navegarSinAnimacion(DriverPerfilActivity.class);
                return true;
            }

            return false;
        });
    }
    // MÉTODO PARA NAVEGAR SIN ANIMACIÓN
    private void navegarSinAnimacion(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
    // Método para cambiar los datos en el RecyclerView según la pestaña seleccionada
    private void cambiarDatosPorTab(int position) {
        switch (position) {
            case 0: // En curso
                adapter.actualizarDatos(reservasEnCurso);
                actualizarVistaVacia(reservasEnCurso);
                break;
            case 1: // Pasado
                adapter.actualizarDatos(reservasPasadas);
                actualizarVistaVacia(reservasPasadas);
                break;
            case 2: // Cancelado
                adapter.actualizarDatos(reservasCanceladas);
                actualizarVistaVacia(reservasCanceladas);
                break;
        }
    }

    // Método para mostrar/ocultar la vista vacía
    private void actualizarVistaVacia(List<ReservaModel> listaActual) {
        if (listaActual.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    // Método para cargar datos de ejemplo
    private void cargarDatosDeEjemplo() {
        // Reservas En Curso - Cambiar de Reserva a ReservaModel
        reservasEnCurso.add(new ReservaModel(1, "Luis Quispe Rojas", "Altura Real Hotel", "Aeropuerto",
                "1.2 Km", "23 de Abril", "10:30", R.drawable.ic_hotel, "en_curso"));
        reservasEnCurso.add(new ReservaModel(2, "Ana Ramirez Campos", "Aeropuerto", "Oasis Urbano",
                "1.0 Km", "23 de Abril", "12:00", R.drawable.ic_aeropuerto, "en_curso"));

        // Reservas Pasadas - Cambiar de Reserva a ReservaModel
        reservasPasadas.add(new ReservaModel(3, "Laura González", "La Casona del Lago", "Aeropuerto",
                "1.5 Km", "23 de Abril", "14:30", R.drawable.ic_hotel, "pasado"));
        reservasPasadas.add(new ReservaModel(4, "Jorge Enrique Vidal", "Aeropuerto", "La Perla",
                "1.2 Km", "23 de Abril", "15:30", R.drawable.ic_aeropuerto, "pasado"));

        // Reservas Canceladas - Cambiar de Reserva a ReservaModel
        reservasCanceladas.add(new ReservaModel(5, "Miguel Castro", "Hotel Mirador", "Centro Comercial",
                "2.0 Km", "22 de Abril", "09:15", R.drawable.ic_hotel, "cancelado"));
    }



    @Override
    public void onBackPressed() {
        // Redirigir a MainActivity cuando se presiona el botón atrás
        super.onBackPressed();
        navegarSinAnimacion(DriverInicioActivity.class);
    }

    // Implementación del listener de clic en reserva
    @Override
    public void onReservaClick(ReservaModel reserva) {
        // Aquí puedes manejar el clic en una reserva, como abrir detalles
        Toast.makeText(this, "Reserva seleccionada: " + reserva.getNombrePasajero(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, DriverReservaInfoActivity.class);
        intent.putExtra("RESERVA_ID", reserva.getId());
        startActivity(intent);

    }
}