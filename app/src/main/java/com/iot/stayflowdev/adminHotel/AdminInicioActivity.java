package com.iot.stayflowdev.adminHotel;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.repository.AdminHotelViewModel;

public class AdminInicioActivity extends AppCompatActivity {

    private TextView tvNombreAdmin;
    private TextView tvNombreHotel;
    private TextInputEditText etDescripcionHotel;
    private MaterialButton btnGuardarDescripcion;
    private String descripcionOriginal = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_inicio);

        solicitarPermisoNotificaciones();


        // Vistas
        tvNombreAdmin = findViewById(R.id.tvNombreAdmin);
        tvNombreHotel = findViewById(R.id.tvNombreHotel);
        etDescripcionHotel = findViewById(R.id.etDescripcionHotel);
        btnGuardarDescripcion = findViewById(R.id.btnGuardarDescripcion);
        btnGuardarDescripcion.setEnabled(false);

        // ViewModel
        AdminHotelViewModel viewModel = new ViewModelProvider(this).get(AdminHotelViewModel.class);

        viewModel.getNombreAdmin().observe(this, nombre -> {
            tvNombreAdmin.setText("Hola, " + nombre);
        });

        viewModel.getNombreHotel().observe(this, nombreHotel -> {
            tvNombreHotel.setText(nombreHotel);
        });

        viewModel.getDescripcionHotel().observe(this, descripcion -> {
            descripcionOriginal = (descripcion != null) ? descripcion : "";
            etDescripcionHotel.setText(descripcionOriginal);
            btnGuardarDescripcion.setEnabled(false);
        });

        etDescripcionHotel.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String actual = s.toString().trim();
                btnGuardarDescripcion.setEnabled(!actual.equals(descripcionOriginal));
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        btnGuardarDescripcion.setOnClickListener(v -> {
            String nuevaDescripcion = etDescripcionHotel.getText().toString().trim();
            viewModel.actualizarDescripcionHotel(nuevaDescripcion);
            descripcionOriginal = nuevaDescripcion;
            btnGuardarDescripcion.setEnabled(false);
            Toast.makeText(this, "Descripción actualizada", Toast.LENGTH_SHORT).show();
        });

        configurarAccesosRapidos();


        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Bottom Nav
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.menu_inicio);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_inicio) {
                startActivity(new Intent(this, AdminInicioActivity.class));
                finish();
            } else if (id == R.id.menu_reportes) {
                startActivity(new Intent(this, ReportesAdminActivity.class));
                finish();

            } else if (id == R.id.menu_huesped) {
                startActivity(new Intent(this, HuespedAdminActivity.class));
                finish();

            } else if (id == R.id.menu_mensajeria) {
                startActivity(new Intent(this, MensajeriaAdminActivity.class));
                finish();

            } else if (id == R.id.menu_perfil) {
                startActivity(new Intent(this, PerfilAdminActivity.class));
                finish();

            }
            overridePendingTransition(0, 0);
            return true;
        });
    }

    // Métodos llamados desde el XML via android:onClick
    public void irUbicacion(View view) {
        startActivity(new Intent(this, UbicacionAdminActivity.class));
    }

    public void irGaleria(View view) {
        startActivity(new Intent(this, GaleriaAdminActivity.class));
    }

    public void irHabitaciones(View view) {
        startActivity(new Intent(this, HabitacionesAdminActivity.class));
    }

    public void irServicios(View view) {
        startActivity(new Intent(this, ServiciosAdminActivity.class));
    }

    private void configurarAccesosRapidos() {
        configurarAcceso(R.id.accesoUbicacion, R.drawable.ic_map, "Ubicación", UbicacionAdminActivity.class);
        configurarAcceso(R.id.accesoGaleria, R.drawable.ic_gallery, "Galería", GaleriaAdminActivity.class);
        configurarAcceso(R.id.accesoHabitaciones, R.drawable.ic_rooms, "Habitaciones", HabitacionesAdminActivity.class);
        configurarAcceso(R.id.accesoServicios, R.drawable.ic_services, "Servicios", ServiciosAdminActivity.class);
    }

    private void configurarAcceso(int includeId, int iconResId, String titulo, Class<?> destino) {
        View acceso = findViewById(includeId);
        ImageView icono = acceso.findViewById(R.id.icono);
        TextView texto = acceso.findViewById(R.id.texto);

        icono.setImageResource(iconResId);
        texto.setText(titulo);

        acceso.setOnClickListener(v -> startActivity(new Intent(this, destino)));
    }

    private void solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
    }
}
