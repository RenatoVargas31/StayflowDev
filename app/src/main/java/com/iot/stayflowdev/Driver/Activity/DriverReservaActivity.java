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
import com.iot.stayflowdev.Driver.Model.ReservaModel;
import com.iot.stayflowdev.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    //

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

       //Cargar datos desde Firestore
        cargarSolicitudesDesdeFirestore();

        // Inicializar adaptador con las reservas en curso por defecto
        adapter = new ReservasAdapter(this, reservasEnCurso, this);
        recyclerView.setAdapter(adapter);

        actualizarVistaVacia(reservasEnCurso, 0); // 0 → corresponde a la pestaña "En curso"

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

    // Este metodo ya está correcto según el PPT
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


    //  MeTODO PARA INICIALIZAR VISTAS
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


    // Método para mostrar/ocultar la vista vacía
    private void actualizarVistaVacia(List<ReservaModel> listaActual, int tabIndex) {
        if (listaActual.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);

            TextView emptyText = findViewById(R.id.empty_text);
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
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
    private void cargarSolicitudesDesdeFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.e("Firestore", "Usuario no autenticado");
            return;
        }

        db.collection("solicitudesTaxi")
                .whereEqualTo("esAceptada", true)
                .whereEqualTo("idTaxista", user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    reservasEnCurso.clear();

                    for (DocumentSnapshot doc : snapshot) {
                        String id = doc.getId();
                        String nombre = doc.getString("nombrePasajero");
                        String origen = doc.getString("origen");
                        String destino = doc.getString("destino");
                        Timestamp timestamp = doc.getTimestamp("fechaCreacion");
                        String fecha = timestamp != null
                                ? new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(timestamp.toDate())
                                : "Sin fecha";
                        String hora = doc.getString("horaAceptacion");
                        String estado = doc.getString("estado");

                        ReservaModel reserva = new ReservaModel(
                                0, nombre, origen, destino, "1.2 Km",
                                fecha != null ? fecha : "N/A",
                                hora != null ? hora : "N/A",
                                R.drawable.ic_carro, estado != null ? estado : "en_curso"
                        );

                        reservasEnCurso.add(reserva);
                    }

                    adapter.actualizarDatos(reservasEnCurso);
                    actualizarVistaVacia(reservasEnCurso, 0); // En curso
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error cargando solicitudesTaxi", e));
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