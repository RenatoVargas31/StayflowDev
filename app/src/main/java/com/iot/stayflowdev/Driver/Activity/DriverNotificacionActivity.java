package com.iot.stayflowdev.Driver.Activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iot.stayflowdev.Driver.Adapter.NotificacionAdapter;
import com.iot.stayflowdev.R;

import java.util.ArrayList;
import java.util.List;

public class DriverNotificacionActivity extends AppCompatActivity {

    private RecyclerView rvNotificaciones;
    private TextView tvNoNotificacion;
    private NotificacionAdapter adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_notificacion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Habilitar el boton de regreso en la barra superior
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        rvNotificaciones = findViewById(R.id.rvNotificaciones);
        tvNoNotificacion = findViewById(R.id.tvNoNotificacion);

        // Crear datos de ejemplo basados en la imagen
        List<NotificacionAdapter.Notificacion> notificaciones = crearNotificacionesEjemplo();

        // Configurar el RecyclerView
        if (notificaciones.isEmpty()) {
            rvNotificaciones.setVisibility(View.GONE);
            tvNoNotificacion.setVisibility(View.VISIBLE);
        } else {
            rvNotificaciones.setVisibility(View.VISIBLE);
            tvNoNotificacion.setVisibility(View.GONE);

            adaptador = new NotificacionAdapter(this, notificaciones);
            rvNotificaciones.setAdapter(adaptador);
            rvNotificaciones.setLayoutManager(new LinearLayoutManager(this));
        }


    }

    private List<NotificacionAdapter.Notificacion> crearNotificacionesEjemplo() {
        List<NotificacionAdapter.Notificacion> lista = new ArrayList<>();

        // Viajes aceptados (3)
        for (int i = 0; i < 3; i++) {
            lista.add(new NotificacionAdapter.Notificacion(
                    NotificacionAdapter.TIPO_VIAJE_ACEPTADO,
                    "¡Viaje aceptado con éxito!",
                    "Has confirmado la solicitud. Dirígete al punto de recogida para encontrar al pasajero.",
                    "02:10"
            ));
        }

        // Viajes cancelados (2)
        for (int i = 0; i < 2; i++) {
            lista.add(new NotificacionAdapter.Notificacion(
                    NotificacionAdapter.TIPO_VIAJE_CANCELADO,
                    "El cliente ha cancelado el viaje.",
                    "La solicitud ya no está activa. Espera una nueva asignación.",
                    "02:10"
            ));
        }

        // Recogidas próximas (3)
        for (int i = 0; i < 3; i++) {
            lista.add(new NotificacionAdapter.Notificacion(
                    NotificacionAdapter.TIPO_RECOGIDA_PROXIMA,
                    "Recogida próxima en 5 minutos",
                    "Estás cerca del punto de encuentro. Verifica la ubicación del pasajero y prepárate para la recogida.",
                    "02:10"
            ));
        }

        return lista;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // vuelve a la actividad anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}