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
import android.widget.TextView;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.Driver.Adapter.ReservasAdapter;
import com.iot.stayflowdev.Driver.Dtos.SolicitudReserva;
import com.iot.stayflowdev.Driver.Model.ReservaModel;
import com.iot.stayflowdev.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DriverReservaActivity extends AppCompatActivity implements ReservasAdapter.OnReservaClickListener {
    private String TAG = "DriverReservaActivity";

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private ReservasAdapter adapter;
    private LinearLayout emptyView;
    private BottomNavigationView bottomNavigation;
    private String channelId = "channelReservas";
    private List<SolicitudReserva> reservasEnCurso = new ArrayList<>();
    private List<SolicitudReserva> reservasPasadas = new ArrayList<>();
    private List<SolicitudReserva> reservasCanceladas = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate iniciado");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_reserva);
        Log.d(TAG, "Layout establecido");

        // Configurar márgenes para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        Log.d(TAG, "Window insets configurados");

        crearCanalNotificacion();
        Log.d(TAG, "Canal de notificación creado");

        // Inicializar vistas
        inicializarVistas();
        Log.d(TAG, "Vistas inicializadas");

        // Configurar BottomNavigationView con navegación fluida
        configurarBottomNavigationFluido();
        Log.d(TAG, "Bottom navigation configurado");

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        Log.d(TAG, "RecyclerView configurado");

        // Añadir algo de espacio entre elementos
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        Log.d(TAG, "Decoración del RecyclerView agregada");

        //Cargar datos desde Firestore
        cargarSolicitudesDesdeFirestore();
        Log.d(TAG, "Carga de Firestore iniciada");
        // Configurar listener del TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab seleccionado: " + tab.getPosition());
                cambiarDatosPorTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        Log.d(TAG, "TabLayout listener configurado");

        // ACTIVAR NOTIFICACIÓN AL FINAL
        mostrarNotificacionReservasActivas();
        Log.d(TAG, "onCreate completado exitosamente");
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

    public void mostrarNotificacionReservasActivas() {
        // Filtrar solo las reservas de hoy
        List<SolicitudReserva> reservasHoy = filtrarReservasDeHoy(reservasEnCurso);
        int reservasActivasHoy = reservasHoy.size();

        // Solo mostrar notificación si hay reservas para hoy
        if (reservasActivasHoy == 0) {
            Log.d(TAG, "No hay reservas para hoy, no se muestra notificación");
            return;
        }

        Intent intent = new Intent(this, DriverReservaActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        // Texto más específico para reservas de hoy
        String contentText;
        if (reservasActivasHoy == 1) {
            contentText = "Tienes 1 viaje pendiente para hoy";
        } else {
            contentText = "Tienes " + reservasActivasHoy + " viajes pendientes para hoy";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_carro)
                .setContentTitle("Viajes de Hoy")
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(100, builder.build());
            Log.d(TAG, "Notificación mostrada: " + reservasActivasHoy + " viajes para hoy");
        }
    }

    // Método auxiliar para filtrar reservas del día actual
    private List<SolicitudReserva> filtrarReservasDeHoy(List<SolicitudReserva> reservas) {
        List<SolicitudReserva> reservasHoy = new ArrayList<>();

        // Obtener la fecha de hoy
        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0);
        hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0);
        hoy.set(Calendar.MILLISECOND, 0);

        Calendar mañana = Calendar.getInstance();
        mañana.setTime(hoy.getTime());
        mañana.add(Calendar.DAY_OF_MONTH, 1);

        for (SolicitudReserva reserva : reservas) {
            if (reserva.getFechaSalida() != null) {
                Date fechaSalida = reserva.getFechaSalida();

                // Verificar si la fecha de salida está entre hoy y mañana (es decir, es hoy)
                if (fechaSalida.getTime() >= hoy.getTimeInMillis() &&
                        fechaSalida.getTime() < mañana.getTimeInMillis()) {
                    reservasHoy.add(reserva);
                    Log.d(TAG, "Reserva de hoy encontrada: " + reserva.getNombrePasajero() +
                            " - Fecha: " + reserva.getFechaHora());
                }
            }
        }

        Log.d(TAG, "Total reservas de hoy: " + reservasHoy.size());
        return reservasHoy;
    }

    private void inicializarVistas() {
        Log.d(TAG, "Iniciando inicializarVistas");

        tabLayout = findViewById(R.id.tabLayout);
        Log.d(TAG, "tabLayout: " + (tabLayout != null ? "encontrado" : "NULL"));

        recyclerView = findViewById(R.id.recyclerViewReservas);
        Log.d(TAG, "recyclerView: " + (recyclerView != null ? "encontrado" : "NULL"));

        emptyView = findViewById(R.id.empty_view);
        Log.d(TAG, "emptyView: " + (emptyView != null ? "encontrado" : "NULL"));

        bottomNavigation = findViewById(R.id.bottomNavigation);
        Log.d(TAG, "bottomNavigation: " + (bottomNavigation != null ? "encontrado" : "NULL"));

        Log.d(TAG, "inicializarVistas completado");
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
    // MeTODO PARA NAVEGAR SIN ANIMACIÓN
    private void navegarSinAnimacion(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
    // Metodo para cambiar los datos en el RecyclerView según la pestaña seleccionada
    private void cambiarDatosPorTab(int position) {
        switch (position) {
            case 0: // En curso
                adapter.actualizarDatos(reservasEnCurso);
                actualizarVistaVacia(reservasEnCurso, 0);
                break;
            case 1: // Pasado
                adapter.actualizarDatos(reservasPasadas);
                actualizarVistaVacia(reservasPasadas, 1);
                break;
            case 2: // Cancelado
                adapter.actualizarDatos(reservasCanceladas);
                actualizarVistaVacia(reservasCanceladas, 2);
                break;
        }
    }
    private void actualizarVistaVacia(List<SolicitudReserva> listaActual, int tabIndex) {
        Log.d(TAG, "actualizarVistaVacia - Lista size: " + (listaActual != null ? listaActual.size() : "NULL"));

        if (listaActual == null || listaActual.isEmpty()) {
            Log.d(TAG, "Lista vacía, mostrando empty view");

            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);

            TextView emptyText = findViewById(R.id.empty_text);
            if (emptyText != null) {
                switch (tabIndex) {
                    case 0:
                        emptyText.setText("No tienes reservas activas");
                        break;
                    case 1:
                        emptyText.setText("Aún no tienes reservas pasadas");
                        break;
                    case 2:
                        emptyText.setText("Aún no tienes reservas canceladas");
                        break;
                }
                Log.d(TAG, "Texto empty actualizado para tab: " + tabIndex);
            } else {
                Log.e(TAG, "empty_text TextView es null!");
            }
        } else {
            Log.d(TAG, "Lista tiene datos, mostrando recycler view");
            if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
            if (emptyView != null) emptyView.setVisibility(View.GONE);
        }
    }
    private void cargarSolicitudesDesdeFirestore() {
        Log.d(TAG, "Iniciando carga desde Firestore");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.e(TAG, "Usuario no autenticado");
            return;
        }

        Log.d(TAG, "Usuario autenticado: " + user.getUid());

        db.collection("solicitudesTaxi")
                .whereEqualTo("esAceptada", true)
                .whereEqualTo("idTaxista", user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    Log.d(TAG, "Firestore success - documentos encontrados: " + snapshot.size());

                    reservasEnCurso.clear();

                    for (DocumentSnapshot doc : snapshot) {
                        try {
                            Log.d(TAG, "Procesando documento: " + doc.getId());
                            SolicitudReserva solicitud = doc.toObject(SolicitudReserva.class);
                            if (solicitud != null) {
                                Log.d(TAG, "Solicitud creada exitosamente: " + solicitud.getNombrePasajero());
                                reservasEnCurso.add(solicitud);
                            } else {
                                Log.w(TAG, "Solicitud es null para documento: " + doc.getId());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando documento " + doc.getId() + ": " + e.getMessage());
                        }
                    }

                    Log.d(TAG, "Total solicitudes agregadas: " + reservasEnCurso.size());

                    // MOVER LA INICIALIZACIÓN DEL ADAPTER AQUÍ
                    if (adapter == null) {
                        adapter = new ReservasAdapter(this, reservasEnCurso, this);
                        recyclerView.setAdapter(adapter);
                        Log.d(TAG, "Adapter creado y asignado");
                    } else {
                        adapter.actualizarDatos(reservasEnCurso);
                        Log.d(TAG, "Adapter actualizado");
                    }

                    actualizarVistaVacia(reservasEnCurso, 0);
                    Log.d(TAG, "Vista vacía actualizada");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando solicitudesTaxi: " + e.getMessage());
                    e.printStackTrace();
                });
    }


    @Override
    public void onBackPressed() {
        // Redirigir a MainActivity cuando se presiona el botón atrás
        super.onBackPressed();
        navegarSinAnimacion(DriverInicioActivity.class);
    }

    // Implementación del listener de clic en reserva
    @Override
    public void onReservaClick(SolicitudReserva reserva) {
        Toast.makeText(this, "Reserva seleccionada: " + reserva.getNombrePasajero(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, DriverReservaInfoActivity.class);
        intent.putExtra("SOLICITUD_ID", reserva.getReservaId());
        startActivity(intent);
    }
}