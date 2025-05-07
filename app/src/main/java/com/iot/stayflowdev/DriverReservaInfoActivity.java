package com.iot.stayflowdev;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class DriverReservaInfoActivity extends AppCompatActivity {

    private MaterialButton btn_ver_mapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_reserva_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar el botón
        btn_ver_mapa = findViewById(R.id.btn_ver_mapa);

        // Configurar el listener del botón para abrir DriverMapaActivity
        btn_ver_mapa.setOnClickListener(v -> {
            // Crear un nuevo Intent para abrir la actividad DriverMapaActivity
            Intent intent = new Intent(DriverReservaInfoActivity.this, DriverMapaActivity.class);

            // Opcionalmente, puedes pasar datos a la nueva actividad
            // intent.putExtra("RESERVA_ID", reservaId);

            // Iniciar la actividad
            startActivity(intent);
        });

        // Habilitar el boton de regreso en la barra superior
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
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