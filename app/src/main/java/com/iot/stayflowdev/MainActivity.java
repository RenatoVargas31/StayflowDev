package com.iot.stayflowdev;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvSolicitudesCercanas;
    private TextView tvNoSolicitudes;
    private MaterialSwitch statusSwitch;
    private TextView statusText;
    private SolicitudesAdapter adapter;
    private List<SolicitudModel> solicitudes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        // Maneja insets para el contenido principal
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // Note: 0 bottom padding
            return insets;
        });

        // Maneja insets específicamente para BottomNavigationView
        View bottomNav = findViewById(R.id.bottomNavigation);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        rvSolicitudesCercanas = findViewById(R.id.rvSolicitudesCercanas);
        tvNoSolicitudes = findViewById(R.id.tvNoSolicitudes);
        statusSwitch = findViewById(R.id.statusSwitch);
        statusText = findViewById(R.id.statusText);

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

        solicitudes.add(new SolicitudModel(
                "1.2 km",
                "Hotel Marriot, Miraflores",
                "Aeropuerto Jorge Chávez, Callao",
                "Hace 2 min",
                "~45 min"
        ));

        solicitudes.add(new SolicitudModel(
                "0.9 km",
                "Aeropuerto Jorge Chávez, Callao",
                "Hotel Hilton, San Isidro",
                "Hace 3 min",
                "~40 min"
        ));

        solicitudes.add(new SolicitudModel(
                "1.5 km",
                "Hotel Casa Andina, Miraflores",
                "Aeropuerto Jorge Chávez, Callao",
                "Hace 1 min",
                "~50 min"
        ));

        solicitudes.add(new SolicitudModel(
                "2.1 km",
                "Aeropuerto Jorge Chávez, Callao",
                "Hotel Los Delfines, San Isidro",
                "Hace 5 min",
                "~38 min"
        ));
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