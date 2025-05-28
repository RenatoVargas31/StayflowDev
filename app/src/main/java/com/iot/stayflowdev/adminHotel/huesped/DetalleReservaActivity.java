package com.iot.stayflowdev.adminHotel.huesped;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.databinding.ActivityDetalleReservaBinding;

public class DetalleReservaActivity extends AppCompatActivity {

    private ActivityDetalleReservaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetalleReservaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configura Toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Configura BottomNavigationView
        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.setSelectedItemId(R.id.menu_huesped);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_inicio) {
                finish();
                return true;
            } else if (id == R.id.menu_reportes) {
                finish();
                return true;
            } else if (id == R.id.menu_huesped) {
                return true;
            } else if (id == R.id.menu_mensajeria) {
                finish();
                return true;
            } else if (id == R.id.menu_perfil) {
                finish();
                return true;
            }
            return false;
        });

        // Obtén los datos del Intent
        String nombre = getIntent().getStringExtra("nombre");
        String habitacion = getIntent().getStringExtra("habitacion");
        String codigo = getIntent().getStringExtra("codigo");
        String llegada = getIntent().getStringExtra("llegada");
        String salida = getIntent().getStringExtra("salida");
        String huespedes = getIntent().getStringExtra("huespedes");

        // Rellena las vistas
        binding.textNombre.setText(nombre);
        binding.textCodigoReserva.setText("Código de reserva: " + codigo);
        binding.textLlegada.setText(llegada);
        binding.textSalida.setText(salida);
        binding.textHuespedes.setText(huespedes);
        binding.textHabitacion1.setText(habitacion);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
