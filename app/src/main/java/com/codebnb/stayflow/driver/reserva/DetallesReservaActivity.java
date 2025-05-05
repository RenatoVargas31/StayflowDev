package com.codebnb.stayflow.driver.reserva;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.Toolbar;
import com.codebnb.stayflow.R;
import com.codebnb.stayflow.driver.mapa.DriverMapaFragment;

import java.util.Objects;

public class DetallesReservaActivity extends AppCompatActivity {

    private Button btnVerMapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalles_reserva);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar la Toolbar como ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Habilitar el botón de retroceso estándar (icono a la izquierda sin texto)
        if (getSupportActionBar() != null) {
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



/*
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflar el menú; esto añade items a la action bar si está presente
        getMenuInflater().inflate(R.menu.driver_reserva_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_arrow) {
            // Comportamiento del botón de regreso (igual que presionar el botón físico de atrás)
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}