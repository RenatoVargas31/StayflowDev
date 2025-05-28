package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.adapter.HabitacionAdapter;
import com.iot.stayflowdev.adminHotel.database.AppDatabase;
import com.iot.stayflowdev.adminHotel.database.HabitacionEntity;
import com.iot.stayflowdev.adminHotel.model.Habitacion;
import com.iot.stayflowdev.databinding.ActivityHabitacionesAdminBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HabitacionesAdminActivity extends AppCompatActivity {

    private ActivityHabitacionesAdminBinding binding;
    private HabitacionAdapter habitacionAdapter;
    private AppDatabase db;

    // Nuevas vistas para el layout mejorado
    private LinearLayout emptyState;
    private LinearLayout contentWithRooms;
    private TextView tvRoomCount;
    private FloatingActionButton fabAddRoom;

    private static final int MAX_HABITACIONES = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHabitacionesAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar DB
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "hotel_db").build();

        // Inicializar vistas
        initViews();

        // Configurar toolbar
        setupToolbar();

        // Configurar navegación inferior
        setupBottomNavigation();

        // Configurar RecyclerView
        setupRecyclerView();

        // Cargar datos
        cargarHabitaciones();

        // Configurar FAB
        setupFab();
    }

    private void initViews() {
        emptyState = binding.emptyState;
        contentWithRooms = binding.contentWithRooms;
        tvRoomCount = binding.tvRoomCount;
        fabAddRoom = binding.fabAddRoom;
    }

    private void setupToolbar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.setSelectedItemId(R.id.menu_inicio);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_inicio) {
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
    }

    private void setupRecyclerView() {
        binding.recyclerHabitaciones.setLayoutManager(new LinearLayoutManager(this));
        habitacionAdapter = new HabitacionAdapter(new ArrayList<>(), new HabitacionAdapter.OnHabitacionActionListener() {
            @Override
            public void onEditar(Habitacion habitacion, int position) {
                if (habitacion.isEnUso()) {
                    mostrarError("Acción no permitida", "No se puede editar una habitación en uso.");
                    return;
                }
                mostrarFormularioEditarHabitacion(habitacion);
            }

            @Override
            public void onEliminar(Habitacion habitacion, int position) {
                if (habitacion.isEnUso()) {
                    mostrarError("Acción no permitida", "No se puede eliminar una habitación en uso.");
                    return;
                }
                new MaterialAlertDialogBuilder(HabitacionesAdminActivity.this)
                        .setTitle("Eliminar habitación")
                        .setMessage("¿Seguro que deseas eliminar esta habitación?")
                        .setPositiveButton("Eliminar", (dialog, which) -> eliminarHabitacion(habitacion))
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });
        binding.recyclerHabitaciones.setAdapter(habitacionAdapter);
    }

    private void setupFab() {
        fabAddRoom.setOnClickListener(v -> {
            if (habitacionAdapter.getItemCount() < MAX_HABITACIONES) {
                mostrarFormularioAgregarHabitacion();
            } else {
                mostrarMensajeLimiteAlcanzado();
            }
        });
    }

    private void cargarHabitaciones() {
        new Thread(() -> {
            List<HabitacionEntity> habitacionesRoom = db.habitacionDao().obtenerTodas();
            List<com.iot.stayflowdev.adminHotel.model.Habitacion> habitaciones = new ArrayList<>();
            for (HabitacionEntity entity : habitacionesRoom) {
                habitaciones.add(new Habitacion(entity.id, entity.tipo, entity.capacidad, entity.tamano, entity.precio, entity.enUso));

            }
            runOnUiThread(() -> {
                habitacionAdapter.updateData(habitaciones);
                actualizarUI();
            });
        }).start();
    }

    private void actualizarUI() {
        int roomCount = habitacionAdapter.getItemCount();

        if (roomCount == 0) {
            // Mostrar estado vacío
            emptyState.setVisibility(View.VISIBLE);
            contentWithRooms.setVisibility(View.GONE);
        } else {
            // Mostrar contenido con habitaciones
            emptyState.setVisibility(View.GONE);
            contentWithRooms.setVisibility(View.VISIBLE);

            // Actualizar contador
            actualizarContador(roomCount);
        }

        // Actualizar estado del FAB
        actualizarEstadoFab(roomCount);
    }

    private void actualizarContador(int roomCount) {
        String contadorTexto = roomCount + " de " + MAX_HABITACIONES + " habitaciones creadas";
        tvRoomCount.setText(contadorTexto);
    }

    private void actualizarEstadoFab(int roomCount) {
        if (roomCount >= MAX_HABITACIONES) {
            fabAddRoom.setAlpha(0.6f);
        } else {
            fabAddRoom.setAlpha(1.0f);
        }
    }


    private void mostrarMensajeLimiteAlcanzado() {
        Snackbar.make(
                binding.getRoot(),
                "Has alcanzado el límite máximo de " + MAX_HABITACIONES + " habitaciones",
                Snackbar.LENGTH_LONG
        ).setAction("OK", v -> {
            // Acción opcional
        }).show();
    }

    private void mostrarFormularioAgregarHabitacion() {
        // Verificar límite antes de mostrar el diálogo
        if (habitacionAdapter.getItemCount() >= MAX_HABITACIONES) {
            mostrarMensajeLimiteAlcanzado();
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_habitacion, null);
        TextInputEditText etCapacidad = view.findViewById(R.id.etCapacidad);
        TextInputEditText etTamano = view.findViewById(R.id.etTamano);
        TextInputEditText etPrecio = view.findViewById(R.id.etPrecio);
        MaterialAutoCompleteTextView etTipo = view.findViewById(R.id.etTipo);

        String[] tipos = {"Individual", "Doble", "Triple", "Suite", "Familiar"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, tipos);
        etTipo.setAdapter(adapter);

        etTipo.setInputType(0);
        etTipo.setKeyListener(null);
        etTipo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etTipo.showDropDown();
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle("Agregar habitación")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    procesarFormularioHabitacion(etTipo, etCapacidad, etTamano, etPrecio, tipos);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void procesarFormularioHabitacion(MaterialAutoCompleteTextView etTipo,
                                              TextInputEditText etCapacidad,
                                              TextInputEditText etTamano,
                                              TextInputEditText etPrecio,
                                              String[] tipos) {
        String tipo = etTipo.getText().toString().trim();
        String capacidadStr = etCapacidad.getText().toString().trim();
        String tamanoStr = etTamano.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (tipo.isEmpty() || capacidadStr.isEmpty() || tamanoStr.isEmpty() || precioStr.isEmpty()) {
            mostrarError("Campos incompletos", "Completa todos los campos antes de guardar.");
            return;
        }

        if (!Arrays.asList(tipos).contains(tipo)) {
            mostrarError("Error", "Selecciona un tipo válido.");
            return;
        }

        try {
            int capacidad = Integer.parseInt(capacidadStr);
            int tamano = Integer.parseInt(tamanoStr);
            double precio = Double.parseDouble(precioStr.replace(",", "."));  // 🔥 Permitir coma o punto

            if (capacidad > 4) {
                mostrarError("Error", "Máximo 4 personas por habitación.");
                return;
            }
            if (capacidad <= 0) {
                mostrarError("Error", "La capacidad debe ser mayor a 0.");
                return;
            }
            if (tamano <= 0) {
                mostrarError("Error", "El tamaño debe ser mayor a 0.");
                return;
            }
            if (precio <= 0) {
                mostrarError("Error", "El precio debe ser mayor a 0.");
                return;
            }

            // Verificar límite
            if (habitacionAdapter.getItemCount() >= MAX_HABITACIONES) {
                mostrarMensajeLimiteAlcanzado();
                return;
            }

            guardarHabitacion(tipo, capacidad, tamano, precio);

        } catch (NumberFormatException e) {
            mostrarError("Error", "Verifica que los números estén correctamente escritos.");
        }
    }

    private void eliminarHabitacion(Habitacion habitacion) {
        new Thread(() -> {
            HabitacionEntity entity = new HabitacionEntity(
                    habitacion.getTipo(), habitacion.getCapacidad(), habitacion.getTamano(), habitacion.getPrecio(), habitacion.isEnUso()
            );
            entity.id = habitacion.getId();
            db.habitacionDao().eliminar(entity);

            runOnUiThread(() -> {
                cargarHabitaciones();
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Habitación eliminada")
                        .setMessage("La habitación fue eliminada exitosamente.")
                        .setPositiveButton("OK", null)
                        .show();
            });
        }).start();
    }


    private void mostrarFormularioEditarHabitacion(Habitacion habitacion) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_habitacion, null);
        TextInputEditText etCapacidad = view.findViewById(R.id.etCapacidad);
        TextInputEditText etTamano = view.findViewById(R.id.etTamano);
        TextInputEditText etPrecio = view.findViewById(R.id.etPrecio);
        MaterialAutoCompleteTextView etTipo = view.findViewById(R.id.etTipo);

        String[] tipos = {"Individual", "Doble", "Triple", "Suite", "Familiar"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tipos);
        etTipo.setAdapter(adapter);

        // Prefill data
        etTipo.setText(habitacion.getTipo(), false);
        etCapacidad.setText(String.valueOf(habitacion.getCapacidad()));
        etTamano.setText(String.valueOf(habitacion.getTamano()));
        etPrecio.setText(String.valueOf(habitacion.getPrecio()));

        etTipo.setInputType(0);
        etTipo.setKeyListener(null);
        etTipo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etTipo.showDropDown();
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle("Editar habitación")
                .setView(view)
                // Dentro del setPositiveButton:
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String tipo = etTipo.getText().toString().trim();
                    String capacidadStr = etCapacidad.getText().toString().trim();
                    String tamanoStr = etTamano.getText().toString().trim();
                    String precioStr = etPrecio.getText().toString().trim();

                    if (tipo.isEmpty() || capacidadStr.isEmpty() || tamanoStr.isEmpty() || precioStr.isEmpty()) {
                        mostrarError("Campos incompletos", "Completa todos los campos antes de guardar.");
                        return;
                    }

                    if (!Arrays.asList(tipos).contains(tipo)) {
                        mostrarError("Error", "Selecciona un tipo válido.");
                        return;
                    }

                    try {
                        int capacidad = Integer.parseInt(capacidadStr);
                        int tamano = Integer.parseInt(tamanoStr);
                        double precio = Double.parseDouble(precioStr.replace(",", "."));  // 🔥 Permitir coma o punto

                        if (capacidad > 4) {
                            mostrarError("Error", "Máximo 4 personas por habitación.");
                            return;
                        }
                        if (capacidad <= 0) {
                            mostrarError("Error", "La capacidad debe ser mayor a 0.");
                            return;
                        }
                        if (tamano <= 0) {
                            mostrarError("Error", "El tamaño debe ser mayor a 0.");
                            return;
                        }
                        if (precio <= 0) {
                            mostrarError("Error", "El precio debe ser mayor a 0.");
                            return;
                        }

                        actualizarHabitacion(habitacion, tipo, capacidad, tamano, precio);

                    } catch (NumberFormatException e) {
                        mostrarError("Error", "Verifica que los números estén correctamente escritos.");
                    }
                })

                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarHabitacion(Habitacion habitacion, String nuevoTipo, int nuevaCapacidad, int nuevoTamano, double nuevoPrecio) {
        new Thread(() -> {
            // Crea la entidad actualizada
            HabitacionEntity entity = new HabitacionEntity(nuevoTipo, nuevaCapacidad, nuevoTamano, nuevoPrecio, habitacion.isEnUso());
            entity.id = habitacion.getId();
            db.habitacionDao().actualizar(entity);

            runOnUiThread(() -> {
                cargarHabitaciones();
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Habitación actualizada")
                        .setMessage("La habitación fue actualizada exitosamente.")
                        .setPositiveButton("OK", null)
                        .show();
            });
        }).start();
    }




    private void guardarHabitacion(String tipo, int capacidad, int tamano, double precio) {
        new Thread(() -> {
            db.habitacionDao().insertar(new HabitacionEntity(tipo, capacidad, tamano, precio, false));
            runOnUiThread(() -> {
                cargarHabitaciones();
                new MaterialAlertDialogBuilder(this)
                        .setTitle("¡Habitación agregada exitosamente!")
                        .setMessage("La habitación " + tipo + " ha sido registrada.")
                        .setPositiveButton("OK", null)
                        .show();
            });
        }).start();
    }

    private void mostrarError(String titulo, String mensaje) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private void actualizarVisibilidad() {
        actualizarUI();
    }
}