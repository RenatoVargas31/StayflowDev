package com.iot.stayflowdev;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvSolicitudesCercanas;
    private TextView tvNoSolicitudes;
    private MaterialSwitch statusSwitch;
    private TextView statusText;
    private SolicitudesAdapter adapter;
    private List<SolicitudModel> solicitudes;
    private BottomNavigationView bottomNavigation;
    private ImageView notificationIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Inicializar vistas
        rvSolicitudesCercanas = findViewById(R.id.rvSolicitudesCercanas);
        tvNoSolicitudes = findViewById(R.id.tvNoSolicitudes);
        statusSwitch = findViewById(R.id.statusSwitch);
        statusText = findViewById(R.id.statusText);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        notificationIcon = findViewById(R.id.notification_icon);

        // Ir a la vista de Notificacion
        irNotificacion();

        // Configurar el BottomNavigationView
        configurarBottomNavigation();

        // Crear datos
        crearDatosDeSolicitudes();

        // Configurar RecyclerView
        configurarRecyclerView();

        // Configurar Switch
        configurarSwitch();

        // Establecer estado inicial
        actualizarEstadoUI(statusSwitch.isChecked());
    }

    private void crearDatosDeSolicitudes() {
        solicitudes = new ArrayList<>();

        // Solicitud 1
        solicitudes.add(new SolicitudModel(
                "1.2 km",
                "Hotel Marriot, Miraflores\nAv. Malecón de la Reserva 615, Miraflores 15074, Lima",
                "Aeropuerto Jorge Chávez, Callao\nAv. Elmer Faucett s/n, Callao 07031",
                "Hace 2 min",
                "~45 min",
                "Carlos Rodríguez",
                "+51 945 123 456",
                "2",
                "Sedán ejecutivo",
                "Llevo una maleta grande y necesito ayuda con el equipaje. Agradecería puntualidad.",
                "Pendiente"
        ));

        // Solicitud 2
        solicitudes.add(new SolicitudModel(
                "0.9 km",
                "Aeropuerto Jorge Chávez, Callao\nAv. Elmer Faucett s/n, Callao 07031",
                "Hotel Hilton, San Isidro\nAv. La Paz 1099, San Isidro 15073, Lima",
                "Hace 3 min",
                "~40 min",
                "María Fernández",
                "+51 987 654 321",
                "1",
                "Confort",
                "Preferencia por un conductor que hable inglés. Viajo por negocios.",
                "Pendiente"
        ));

        // Solicitud 3
        solicitudes.add(new SolicitudModel(
                "1.5 km",
                "Hotel Casa Andina, Miraflores\nAv. La Paz 463, Miraflores 15074, Lima",
                "Aeropuerto Jorge Chávez, Callao\nAv. Elmer Faucett s/n, Callao 07031",
                "Hace 1 min",
                "~50 min",
                "Juan Torres",
                "+51 923 456 789",
                "3",
                "SUV",
                "Somos 3 pasajeros con equipaje para 5 días. Necesitamos espacio en la maletera.",
                "Pendiente"
        ));

        // Solicitud 4
        solicitudes.add(new SolicitudModel(
                "2.1 km",
                "Aeropuerto Jorge Chávez, Callao\nAv. Elmer Faucett s/n, Callao 07031",
                "Hotel Los Delfines, San Isidro\nCalle Los Eucaliptos 555, San Isidro 15073, Lima",
                "Hace 5 min",
                "~38 min",
                "Ana Sánchez",
                "+51 912 345 678",
                "4",
                "Van",
                "Viajamos con dos niños pequeños. Necesitamos asientos para niños.",
                "Pendiente"
        ));
    }

    private void irNotificacion(){
        notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DriverNotificacionActivity.class);
                startActivity(intent);
            }
        });
    }
    private void configurarBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_inicio) {
                    // Ya estamos en la actividad principal
                    return true;
                } else if (itemId == R.id.nav_reservas) {
                    // Navegar a DriverReservaActivity
                    Intent intent = new Intent(MainActivity.this, DriverReservaActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_mapa) {
                    // Aquí iría la navegación a la actividad de mapa
                    Intent intent = new Intent(MainActivity.this, DriverMapaActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_perfil) {
                    // Aquí iría la navegación a la actividad de perfil
                    Intent intent = new Intent(MainActivity.this, DriverPerfilActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false;
            }
        });
    }
    private void configurarRecyclerView() {
        adapter = new SolicitudesAdapter();
        adapter.setContext(this);
        adapter.setListaSolicitudes(solicitudes);

        rvSolicitudesCercanas.setAdapter(adapter);
        rvSolicitudesCercanas.setLayoutManager(new LinearLayoutManager(this));
    }
    private void configurarSwitch() {
        statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                actualizarEstadoUI(isChecked);
            }
        });
    }
    private void actualizarEstadoUI(boolean disponible) {
        if (disponible) {
            // Usuario disponible
            statusText.setText("Disponible");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

            // Mostrar RecyclerView, ocultar mensaje
            rvSolicitudesCercanas.setVisibility(View.VISIBLE);
            tvNoSolicitudes.setVisibility(View.GONE);
        } else {
            // Usuario no disponible
            statusText.setText("No disponible");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

            // Ocultar RecyclerView, mostrar mensaje
            rvSolicitudesCercanas.setVisibility(View.GONE);
            tvNoSolicitudes.setVisibility(View.VISIBLE);
        }
    }
}