package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.adapter.HabitacionAdapter;
import com.iot.stayflowdev.model.Habitacion;
import com.iot.stayflowdev.databinding.ActivityHabitacionesAdminBinding;
import com.iot.stayflowdev.viewmodels.HabitacionViewModel;

import java.util.ArrayList;
import java.util.Arrays;

public class HabitacionesAdminActivity extends AppCompatActivity {

    private ActivityHabitacionesAdminBinding binding;
    private HabitacionAdapter habitacionAdapter;
    private HabitacionViewModel viewModel;

    private LinearLayout emptyState;
    private LinearLayout contentWithRooms;
    private TextView tvRoomCount;
    private TextView tvRoomInfo;

    private FloatingActionButton fabAddRoom;

    private static final int MAX_HABITACIONES = 5;
    private final String hotelId = "OkKIOJKRI3krgDOXr1PL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHabitacionesAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(HabitacionViewModel.class);
        viewModel.cargarHabitaciones(hotelId);

        initViews();
        setupToolbar();
        setupBottomNavigation();
        setupRecyclerView();
        setupFab();

        viewModel.getHabitaciones().observe(this, habitaciones -> {
            habitacionAdapter.updateData(habitaciones);
            actualizarUI();
        });
    }

    private void initViews() {
        emptyState = binding.emptyState;
        contentWithRooms = binding.contentWithRooms;
        tvRoomCount = binding.tvRoomCount;
        tvRoomInfo = binding.tvRoomInfo;
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
            if (id == R.id.menu_reportes) startActivity(new Intent(this, ReportesAdminActivity.class));
            else if (id == R.id.menu_huesped) startActivity(new Intent(this, HuespedAdminActivity.class));
            else if (id == R.id.menu_mensajeria) startActivity(new Intent(this, MensajeriaAdminActivity.class));
            else if (id == R.id.menu_perfil) startActivity(new Intent(this, PerfilAdminActivity.class));
            else return true;
            overridePendingTransition(0, 0);
            return true;
        });
    }

    private void setupRecyclerView() {
        binding.recyclerHabitaciones.setLayoutManager(new LinearLayoutManager(this));
        habitacionAdapter = new HabitacionAdapter(new ArrayList<>(), new HabitacionAdapter.OnHabitacionActionListener() {
            @Override
            public void onEditar(Habitacion habitacion, int position) {
                if (Boolean.TRUE.equals(habitacion.getDisponible())) mostrarFormularioEditarHabitacion(habitacion);
                else mostrarError("Acción no permitida", "No se puede editar una habitación en uso.");
            }

            @Override
            public void onEliminar(Habitacion habitacion, int position) {
                if (Boolean.TRUE.equals(habitacion.getDisponible())) {
                    new MaterialAlertDialogBuilder(HabitacionesAdminActivity.this)
                            .setTitle("Eliminar habitación")
                            .setMessage("¿Seguro que deseas eliminar esta habitación?")
                            .setPositiveButton("Eliminar", (dialog, which) -> viewModel.eliminar(hotelId, habitacion.getId(), () -> {
                                runOnUiThread(() -> {
                                    Snackbar.make(binding.getRoot(), "Habitación eliminada correctamente", Snackbar.LENGTH_LONG).show();
                                });
                            }))
                            .setNegativeButton("Cancelar", null)
                            .show();
                } else {
                    mostrarError("Acción no permitida", "No se puede eliminar una habitación en uso.");
                }
            }
        });
        binding.recyclerHabitaciones.setAdapter(habitacionAdapter);
    }

    private void setupFab() {
        fabAddRoom.setOnClickListener(v -> {
            if (habitacionAdapter.getItemCount() < MAX_HABITACIONES) mostrarFormularioAgregarHabitacion();
            else mostrarMensajeLimiteAlcanzado();
        });
    }

    private void actualizarUI() {
        int count = habitacionAdapter.getItemCount();
        emptyState.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        contentWithRooms.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        tvRoomCount.setText(count + " de " + MAX_HABITACIONES + " habitaciones creadas");
        fabAddRoom.setAlpha(count >= MAX_HABITACIONES ? 0.6f : 1f);
    }

    private void mostrarFormularioAgregarHabitacion() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_habitacion, null);
        TextInputEditText etAdultos = view.findViewById(R.id.etAdultos);
        TextInputEditText etNinos = view.findViewById(R.id.etNinos);
        TextInputEditText etTamano = view.findViewById(R.id.etTamano);
        TextInputEditText etPrecio = view.findViewById(R.id.etPrecio);
        TextInputEditText etCantidad = view.findViewById(R.id.etCantidad);
        MaterialAutoCompleteTextView etTipo = view.findViewById(R.id.etTipo);

        String[] tipos = {"Individual", "Doble", "Triple", "Suite", "Familiar"};
        etTipo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tipos));
        etTipo.setInputType(0);
        etTipo.setKeyListener(null);
        etTipo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etTipo.showDropDown();
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle("Agregar habitación")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    try {
                        String adultosStr = etAdultos.getText().toString().trim();
                        String ninosStr = etNinos.getText().toString().trim();
                        String tamanoStr = etTamano.getText().toString().trim();
                        String precioStr = etPrecio.getText().toString().trim();
                        String cantidadStr = etCantidad.getText().toString().trim();
                        String tipo = etTipo.getText().toString().trim();

                        // Validación de campos vacíos
                        if (tipo.isEmpty() || tamanoStr.isEmpty() || precioStr.isEmpty() || cantidadStr.isEmpty()) {
                            mostrarError("Campos incompletos", "Completa todos los campos obligatorios.");
                            return;
                        }

                        int adultos = adultosStr.isEmpty() ? 0 : Integer.parseInt(adultosStr);
                        int ninos = ninosStr.isEmpty() ? 0 : Integer.parseInt(ninosStr);
                        int tamano = Integer.parseInt(tamanoStr);
                        double precio = Double.parseDouble(precioStr);
                        int cantidad = Integer.parseInt(cantidadStr);

                        // Validaciones lógicas
                        if (!validarCampos(tipo, adultos, ninos, tamano, precio, cantidadStr)) return;

                        // Validar que el tipo no esté repetido
                        boolean tipoYaExiste = false;
                        for (Habitacion hExistente : habitacionAdapter.getHabitaciones()) {
                            if (hExistente.getTipo().equalsIgnoreCase(tipo)) {
                                tipoYaExiste = true;
                                break;
                            }
                        }
                        if (tipoYaExiste) {
                            mostrarError("Tipo duplicado", "Ya existe una habitación registrada con el tipo \"" + tipo + "\".");
                            return;
                        }

                        // Crear objeto habitación y guardar
                        Habitacion h = new Habitacion();
                        h.setTipo(tipo);
                        h.setCapacidad(new Habitacion.Capacidad(String.valueOf(adultos), String.valueOf(ninos)));
                        h.setTamano(String.valueOf(tamano));
                        h.setPrecio(String.valueOf(precio));
                        h.setCantidad(String.valueOf(cantidad));
                        h.setDisponible(true);

                        viewModel.agregar(hotelId, h);
                        Snackbar.make(binding.getRoot(), "Habitación creada exitosamente", Snackbar.LENGTH_LONG).show();

                    } catch (NumberFormatException e) {
                        mostrarError("Error numérico", "Verifica que los valores numéricos estén correctamente escritos (usa punto para decimales).");
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void mostrarFormularioEditarHabitacion(Habitacion habitacion) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_habitacion, null);
        TextInputEditText etAdultos = view.findViewById(R.id.etAdultos);
        TextInputEditText etNinos = view.findViewById(R.id.etNinos);
        TextInputEditText etTamano = view.findViewById(R.id.etTamano);
        TextInputEditText etPrecio = view.findViewById(R.id.etPrecio);
        TextInputEditText etCantidad = view.findViewById(R.id.etCantidad);
        MaterialAutoCompleteTextView etTipo = view.findViewById(R.id.etTipo);

        String[] tipos = {"Individual", "Doble", "Triple", "Suite", "Familiar"};
        etTipo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tipos));

        // Establecer valores actuales


        // tipo de habitacion no se puede cambiar, pero lo mostramos para referencia
        // Mostrar el tipo, pero alertar si intenta cambiarlo
        etTipo.setText(habitacion.getTipo(), false);

        // Deshabilitar edición directa con teclado
        etTipo.setInputType(0);

        // Mostrar advertencia si intenta cambiar el tipo
        etTipo.setOnClickListener(v -> {
            mostrarError("Acción no permitida", "No puedes cambiar el tipo de habitación. Solo puedes editar otros campos.");
        });
        etTipo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                etTipo.clearFocus();
                mostrarError("Acción no permitida", "No puedes cambiar el tipo de habitación.");
            }
        });



        etAdultos.setText(habitacion.getCapacidad().getAdultos());
        etNinos.setText(habitacion.getCapacidad().getNinos());
        etTamano.setText(habitacion.getTamano());
        etPrecio.setText(habitacion.getPrecio());
        etCantidad.setText(habitacion.getCantidad());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Editar habitación")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    try {
                        String adultosStr = etAdultos.getText().toString().trim();
                        String ninosStr = etNinos.getText().toString().trim();
                        String tamanoStr = etTamano.getText().toString().trim();
                        String precioStr = etPrecio.getText().toString().trim();
                        String cantidadStr = etCantidad.getText().toString().trim();
                        String tipo = etTipo.getText().toString().trim(); // aunque no se puede cambiar, lo usamos igual

                        int adultos = adultosStr.isEmpty() ? 0 : Integer.parseInt(adultosStr);
                        int ninos = ninosStr.isEmpty() ? 0 : Integer.parseInt(ninosStr);
                        int tamano = Integer.parseInt(tamanoStr);
                        double precio = Double.parseDouble(precioStr);
                        int cantidad = Integer.parseInt(cantidadStr);

                        if (!validarCampos(tipo, adultos, ninos, tamano, precio, cantidadStr)) return;

                        habitacion.setCapacidad(new Habitacion.Capacidad(String.valueOf(adultos), String.valueOf(ninos)));
                        habitacion.setTamano(String.valueOf(tamano));
                        habitacion.setPrecio(String.valueOf(precio));
                        habitacion.setCantidad(String.valueOf(cantidad));

                        viewModel.actualizar(hotelId, habitacion, () -> {
                            runOnUiThread(() -> {
                                Snackbar.make(binding.getRoot(), "Habitación editada correctamente", Snackbar.LENGTH_LONG).show();
                            });
                        });

                    } catch (Exception e) {
                        mostrarError("Error", "Verifica que los números estén correctamente escritos.");
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarMensajeLimiteAlcanzado() {
        Snackbar.make(binding.getRoot(), "Has alcanzado el límite máximo de " + MAX_HABITACIONES + " habitaciones", Snackbar.LENGTH_LONG).show();
    }

    private void mostrarError(String titulo, String mensaje) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private boolean validarCampos(String tipo, int adultos, int ninos, int tamano, double precio, String cantidadStr) {
        if (!Arrays.asList("Individual", "Doble", "Triple", "Suite", "Familiar").contains(tipo)) {
            mostrarError("Error", "Tipo de habitación no válido.");
            return false;
        }
        if (cantidadStr.isEmpty()) {
            mostrarError("Error", "La cantidad es obligatoria.");
            return false;
        }
        int cantidad = Integer.parseInt(cantidadStr);
        if (cantidad <= 0 || cantidad > 30) {
            mostrarError("Error", "La cantidad debe estar entre 1 y 30.");
            return false;
        }
        if (adultos == 0 && ninos == 0) {
            mostrarError("Error", "Debe haber al menos un adulto o niño.");
            return false;
        }
        if (tamano <= 0) {
            mostrarError("Error", "El tamaño debe ser mayor a 0.");
            return false;
        }
        if (precio <= 0) {
            mostrarError("Error", "El precio debe ser mayor a 0.");
            return false;
        }
        return true;
    }

}
