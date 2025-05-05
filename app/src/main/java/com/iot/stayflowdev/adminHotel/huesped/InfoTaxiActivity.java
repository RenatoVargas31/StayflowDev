package com.iot.stayflowdev.adminHotel.huesped;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.AdminInicioActivity;
import com.iot.stayflowdev.adminHotel.HuespedAdminActivity;
import com.iot.stayflowdev.adminHotel.MensajeriaAdminActivity;
import com.iot.stayflowdev.adminHotel.PerfilAdminActivity;
import com.iot.stayflowdev.adminHotel.ReportesAdminActivity;
import com.iot.stayflowdev.databinding.ActivityInfoTaxiBinding;

public class InfoTaxiActivity extends AppCompatActivity {

    private ActivityInfoTaxiBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoTaxiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Información del Viaje");
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Menú inferior
        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.setSelectedItemId(R.id.menu_huesped);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_inicio) {
                startActivity(new Intent(this, AdminInicioActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_reportes) {
                startActivity(new Intent(this, ReportesAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_huesped) {
                startActivity(new Intent(this, HuespedAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_mensajeria) {
                startActivity(new Intent(this, MensajeriaAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_perfil) {
                startActivity(new Intent(this, PerfilAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        // Recuperar datos del Intent
        Intent intent = getIntent();
        String nombre = intent.getStringExtra("nombre");
        String codigo = intent.getStringExtra("codigo");
        String estadoTaxi = intent.getStringExtra("estadoTaxi");
        String detalleViaje = intent.getStringExtra("detalleViaje");
        String ruta = intent.getStringExtra("ruta");
        int imagenResId = intent.getIntExtra("imagenResId", R.drawable.img_guest_placeholder);

        ((TextView) findViewById(R.id.tvNombre)).setText(nombre);
        ((TextView) findViewById(R.id.tvCodigo)).setText("Código de reserva: " + codigo);
        ((TextView) findViewById(R.id.tvEstadoTaxi)).setText("Taxi: " + estadoTaxi);
        ((TextView) findViewById(R.id.tvDetalleViaje)).setText("Llegada: " + detalleViaje);
        ((TextView) findViewById(R.id.tvRuta)).setText("Ruta: " + ruta);
        ((ImageView) findViewById(R.id.imgGuest)).setImageResource(imagenResId);


    }
}
