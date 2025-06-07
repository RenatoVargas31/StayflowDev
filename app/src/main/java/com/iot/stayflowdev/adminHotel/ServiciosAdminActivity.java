package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.adapter.ServicioAdapter;
import com.iot.stayflowdev.adminHotel.model.Servicio;
import com.iot.stayflowdev.databinding.ActivityServiciosAdminBinding;
import java.util.ArrayList;
import java.util.List;

public class ServiciosAdminActivity extends AppCompatActivity {

    private ActivityServiciosAdminBinding binding;
    private ServicioAdapter servicioAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServiciosAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Configura Toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // Configura BottomNavigationView
        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.setSelectedItemId(R.id.menu_inicio);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_inicio) return true;
            if (id == R.id.menu_reportes) startActivity(new Intent(this, ReportesAdminActivity.class));
            else if (id == R.id.menu_huesped) startActivity(new Intent(this, HuespedAdminActivity.class));
            else if (id == R.id.menu_mensajeria) startActivity(new Intent(this, MensajeriaAdminActivity.class));
            else if (id == R.id.menu_perfil) startActivity(new Intent(this, PerfilAdminActivity.class));
            overridePendingTransition(0, 0);
            return true;
        });

        /*
        servicioAdapter = new ServicioAdapter(new ArrayList<>(), new ServicioAdapter.OnServicioActionListener() {
            @Override
            public void onEditar(Servicio servicio) {
                mostrarDialogoEditar(servicio);
            }

            @Override
            public void onEliminar(Servicio servicio) {
                confirmarEliminar(servicio);
            }
        });*/

        binding.recyclerServicios.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerServicios.setAdapter(servicioAdapter);

        /*binding.fabAddService.setOnClickListener(v -> mostrarDialogoAgregar());*/

        /*
        cargarServicios();*/
    }


    /*
    private void cargarServicios() {
        new Thread(() -> {
            List<ServicioEntity> entidades = db.servicioDao().obtenerTodos();
            List<Servicio> servicios = new ArrayList<>();
            for (ServicioEntity e : entidades) {
                servicios.add(new Servicio(e.id, e.nombre, e.descripcion, e.precio, e.esGratis));
            }
            runOnUiThread(() -> servicioAdapter.updateData(servicios));
        }).start();
    }*/
    /*

    private void mostrarDialogoAgregar() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_servicio, null);
        EditText etNombre = view.findViewById(R.id.inputServiceName);
        EditText etDescripcion = view.findViewById(R.id.inputDescription);
        EditText etPrecio = view.findViewById(R.id.inputPrice);
        RadioButton radioFree = view.findViewById(R.id.radioFree);
        RadioButton radioWithPrice = view.findViewById(R.id.radioWithPrice);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Agregar Servicio")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String descripcion = etDescripcion.getText().toString().trim();
                    String precio = etPrecio.getText().toString().trim();
                    boolean esGratis = radioFree.isChecked();

                    if (nombre.isEmpty() || descripcion.isEmpty() || (!esGratis && precio.isEmpty())) {
                        mostrarError("Error", "Completa todos los campos antes de guardar.");
                        return;
                    }

                    new Thread(() -> {
                        db.servicioDao().insertar(new ServicioEntity(nombre, descripcion, esGratis ? "Gratis" : precio, esGratis));
                        runOnUiThread(() -> {
                            cargarServicios();
                            mostrarMensaje("Servicio agregado exitosamente.");
                        });
                    }).start();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }*/

    /*
    private void mostrarDialogoEditar(Servicio servicio) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_servicio, null);
        EditText etNombre = view.findViewById(R.id.inputServiceName);
        EditText etDescripcion = view.findViewById(R.id.inputDescription);
        EditText etPrecio = view.findViewById(R.id.inputPrice);
        RadioButton radioFree = view.findViewById(R.id.radioFree);
        RadioButton radioWithPrice = view.findViewById(R.id.radioWithPrice);

        etNombre.setText(servicio.getNombre());
        etDescripcion.setText(servicio.getDescripcion());
        if (servicio.isEsGratis()) {
            radioFree.setChecked(true);
            etPrecio.setText("");
        } else {
            radioWithPrice.setChecked(true);
            etPrecio.setText(servicio.getPrecio());
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Editar Servicio")
                .setView(view)
                .setPositiveButton("Actualizar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String descripcion = etDescripcion.getText().toString().trim();
                    String precio = etPrecio.getText().toString().trim();
                    boolean esGratis = radioFree.isChecked();

                    if (nombre.isEmpty() || descripcion.isEmpty() || (!esGratis && precio.isEmpty())) {
                        mostrarError("Error", "Completa todos los campos antes de guardar.");
                        return;
                    }

                    new Thread(() -> {
                        ServicioEntity entity = new ServicioEntity(nombre, descripcion, esGratis ? "Gratis" : precio, esGratis);
                        entity.id = servicio.getId();
                        db.servicioDao().actualizar(entity);
                        runOnUiThread(() -> {
                            cargarServicios();
                            mostrarMensaje("Servicio actualizado exitosamente.");
                        });
                    }).start();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarEliminar(Servicio servicio) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Deseas eliminar el servicio '" + servicio.getNombre() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    new Thread(() -> {
                        ServicioEntity entity = new ServicioEntity(servicio.getNombre(), servicio.getDescripcion(), servicio.getPrecio(), servicio.isEsGratis());
                        entity.id = servicio.getId();
                        db.servicioDao().eliminar(entity);
                        runOnUiThread(() -> {
                            cargarServicios();
                            mostrarMensaje("Servicio eliminado exitosamente.");
                        });
                    }).start();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarError(String titulo, String mensaje) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private void mostrarMensaje(String mensaje) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Información")
                .setMessage(mensaje)
                .setPositiveButton("OK", null)
                .show();
    }*/
}
