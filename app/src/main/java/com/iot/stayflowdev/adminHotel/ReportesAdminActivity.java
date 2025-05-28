package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.adapter.ServicioAdapter;
import com.iot.stayflowdev.adminHotel.adapter.UsuarioAdapter;
import com.iot.stayflowdev.adminHotel.model.Servicio;
import com.iot.stayflowdev.adminHotel.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class ReportesAdminActivity extends AppCompatActivity {

    private ServicioAdapter servicioAdapter;
    private UsuarioAdapter usuarioAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes_admin);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.menu_reportes);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_inicio) {
                startActivity(new Intent(this, AdminInicioActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_reportes) {
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

        // RecyclerViews y ChipGroup
        RecyclerView recyclerServicios = findViewById(R.id.recyclerServicios);
        RecyclerView recyclerUsuarios = findViewById(R.id.recyclerUsuarios);
        ChipGroup chipGroupFiltros = findViewById(R.id.chipGroupFiltros);

        recyclerServicios.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsuarios.setLayoutManager(new LinearLayoutManager(this));

        /*
        servicioAdapter = new ServicioAdapter(new ArrayList<>());
        usuarioAdapter = new UsuarioAdapter(new ArrayList<>());

        recyclerServicios.setAdapter(servicioAdapter);
        recyclerUsuarios.setAdapter(usuarioAdapter);

        cargarDatosDiarios();

        chipGroupFiltros.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipDiario) {
                cargarDatosDiarios();
            } else if (checkedId == R.id.chipMensual) {
                cargarDatosMensuales();
            } else if (checkedId == R.id.chipAnual) {
                cargarDatosAnuales();
            }
        });
    }

    private void cargarDatosDiarios() {
        List<Servicio> servicios = new ArrayList<>();
        servicios.add(new Servicio("Lavandería", 30, "S/.300"));
        servicios.add(new Servicio("Desayuno", 55, "S/.550"));
        servicios.add(new Servicio("Spa", 70, "S/.700"));

        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(new Usuario("Juan Pérez", 80, "S/.1000"));
        usuarios.add(new Usuario("Ana Torres", 65, "S/.800"));
        usuarios.add(new Usuario("Luis Gómez", 20, "S/.200"));
        usuarios.add(new Usuario("Luisa Paverti", 20, "S/.100"));


        servicioAdapter.updateData(servicios);
        usuarioAdapter.updateData(usuarios);
    }

    private void cargarDatosMensuales() {
        List<Servicio> servicios = new ArrayList<>();
        servicios.add(new Servicio("Lavandería", 45, "S/.450"));
        servicios.add(new Servicio("Desayuno", 60, "S/.600"));
        servicios.add(new Servicio("Spa", 75, "S/.750"));

        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(new Usuario("Juan Pérez", 70, "S/.900"));
        usuarios.add(new Usuario("Ana Torres", 85, "S/.1100"));
        usuarios.add(new Usuario("Luis Gómez", 40, "S/.400"));

        servicioAdapter.updateData(servicios);
        usuarioAdapter.updateData(usuarios);
    }

    private void cargarDatosAnuales() {
        List<Servicio> servicios = new ArrayList<>();
        servicios.add(new Servicio("Lavandería", 60, "S/.600"));
        servicios.add(new Servicio("Desayuno", 80, "S/.800"));
        servicios.add(new Servicio("Spa", 90, "S/.900"));

        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(new Usuario("Juan Pérez", 85, "S/.1200"));
        usuarios.add(new Usuario("Ana Torres", 75, "S/.1000"));
        usuarios.add(new Usuario("Luis Gómez", 65, "S/.700"));

        servicioAdapter.updateData(servicios);
        usuarioAdapter.updateData(usuarios);

         */
    }
}
