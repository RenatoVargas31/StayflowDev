package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.iot.stayflowdev.adminHotel.repository.AdminHotelViewModel;
import com.iot.stayflowdev.databinding.ActivityHabitacionesAdminBinding;
import com.iot.stayflowdev.model.Habitacion;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHabitacionesAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        setupToolbar();
        setupBottomNavigation();

        viewModel = new ViewModelProvider(this).get(HabitacionViewModel.class);
        AdminHotelViewModel adminHotelViewModel = new ViewModelProvider(this).get(AdminHotelViewModel.class);

        adminHotelViewModel.getHotelId().observe(this, hotelId -> {
            if (hotelId != null && !hotelId.isEmpty()) {
                viewModel.cargarHabitaciones(hotelId);
                viewModel.getHabitaciones().observe(this, habitaciones -> {
                    habitacionAdapter.updateData(habitaciones);
                    actualizarUI();
                });
                setupRecyclerViewConHotelId(hotelId);
                setupFabConHotelId(hotelId);
            } else {
                mostrarError("Error", "No se encontró hotel asignado.");
            }
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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true); // si quieres mostrar "Ubicación"
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.menu_inicio);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_inicio) {
                startActivity(new Intent(this, AdminInicioActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else if (id == R.id.menu_reportes) {
                startActivity(new Intent(this, ReportesAdminActivity.class));
                overridePendingTransition(0, 0);
                finish();

            } else if (id == R.id.menu_huesped) {
                startActivity(new Intent(this, HuespedAdminActivity.class));
                overridePendingTransition(0, 0);
                finish();

            } else if (id == R.id.menu_mensajeria) {
                startActivity(new Intent(this, MensajeriaAdminActivity.class));
                overridePendingTransition(0, 0);
                finish();

            } else if (id == R.id.menu_perfil) {
                startActivity(new Intent(this, PerfilAdminActivity.class));
                overridePendingTransition(0, 0);
                finish();

            }
            overridePendingTransition(0, 0);
            return true;
        });
    }

    private void setupRecyclerViewConHotelId(String hotelId) {
        binding.recyclerHabitaciones.setLayoutManager(new LinearLayoutManager(this));
        habitacionAdapter = new HabitacionAdapter(new ArrayList<>(), new HabitacionAdapter.OnHabitacionActionListener() {
            @Override
            public void onEditar(Habitacion habitacion, int position) {
                if (Boolean.TRUE.equals(habitacion.getDisponible()))
                    mostrarFormularioEditarHabitacion(habitacion, hotelId);
                else mostrarError("Acción no permitida", "No se puede editar una habitación en uso.");
            }

            @Override
            public void onEliminar(Habitacion habitacion, int position) {
                if (Boolean.TRUE.equals(habitacion.getDisponible())) {
                    new MaterialAlertDialogBuilder(HabitacionesAdminActivity.this)
                            .setTitle("Eliminar habitación")
                            .setMessage("¿Seguro que deseas eliminar esta habitación?")
                            .setPositiveButton("Eliminar", (dialog, which) -> viewModel.eliminar(hotelId, habitacion.getId(), () -> {
                                runOnUiThread(() -> Toast.makeText(HabitacionesAdminActivity.this, "Habitación eliminada correctamente", Toast.LENGTH_SHORT).show());
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

    private void setupFabConHotelId(String hotelId) {
        fabAddRoom.setOnClickListener(v -> {
            if (habitacionAdapter.getItemCount() < MAX_HABITACIONES) {
                mostrarFormularioAgregarHabitacion(hotelId);
            } else {
                mostrarMensajeLimiteAlcanzado();
            }
        });
    }

    private void mostrarFormularioAgregarHabitacion(String hotelId) {
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

                        if (tipo.isEmpty() || tamanoStr.isEmpty() || precioStr.isEmpty() || cantidadStr.isEmpty()) {
                            mostrarError("Campos incompletos", "Completa todos los campos obligatorios.");
                            return;
                        }

                        int adultos = adultosStr.isEmpty() ? 0 : Integer.parseInt(adultosStr);
                        int ninos = ninosStr.isEmpty() ? 0 : Integer.parseInt(ninosStr);
                        int tamano = Integer.parseInt(tamanoStr);
                        double precio = Double.parseDouble(precioStr);
                        int cantidad = Integer.parseInt(cantidadStr);

                        if (!validarCampos(tipo, adultos, ninos, tamano, precio, cantidadStr)) return;

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

                        Habitacion h = new Habitacion();
                        h.setTipo(tipo);
                        h.setCapacidad(new Habitacion.Capacidad(String.valueOf(adultos), String.valueOf(ninos)));
                        h.setTamano(String.valueOf(tamano));
                        h.setPrecio(String.valueOf(precio));
                        h.setCantidad(String.valueOf(cantidad));
                        h.setDisponible(true);

                        viewModel.agregar(hotelId, h);
                        Toast.makeText(this, "Habitación creada exitosamente", Toast.LENGTH_SHORT).show();

                    } catch (NumberFormatException e) {
                        mostrarError("Error numérico", "Verifica que los valores numéricos estén correctamente escritos (usa punto para decimales).");
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarFormularioEditarHabitacion(Habitacion habitacion, String hotelId) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_habitacion, null);
        TextInputEditText etAdultos = view.findViewById(R.id.etAdultos);
        TextInputEditText etNinos = view.findViewById(R.id.etNinos);
        TextInputEditText etTamano = view.findViewById(R.id.etTamano);
        TextInputEditText etPrecio = view.findViewById(R.id.etPrecio);
        TextInputEditText etCantidad = view.findViewById(R.id.etCantidad);
        MaterialAutoCompleteTextView etTipo = view.findViewById(R.id.etTipo);

        String[] tipos = {"Individual", "Doble", "Triple", "Suite", "Familiar"};
        etTipo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tipos));
        etTipo.setText(habitacion.getTipo(), false);
        etTipo.setInputType(0);
        etTipo.setOnClickListener(v -> mostrarError("Acción no permitida", "No puedes cambiar el tipo de habitación."));
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
                        int adultos = Integer.parseInt(etAdultos.getText().toString().trim());
                        int ninos = Integer.parseInt(etNinos.getText().toString().trim());
                        int tamano = Integer.parseInt(etTamano.getText().toString().trim());
                        double precio = Double.parseDouble(etPrecio.getText().toString().trim());
                        int cantidad = Integer.parseInt(etCantidad.getText().toString().trim());
                        String tipo = etTipo.getText().toString().trim();

                        if (!validarCampos(tipo, adultos, ninos, tamano, precio, String.valueOf(cantidad))) return;

                        habitacion.setCapacidad(new Habitacion.Capacidad(String.valueOf(adultos), String.valueOf(ninos)));
                        habitacion.setTamano(String.valueOf(tamano));
                        habitacion.setPrecio(String.valueOf(precio));
                        habitacion.setCantidad(String.valueOf(cantidad));

                        viewModel.actualizar(hotelId, habitacion, () ->
                                runOnUiThread(() -> Snackbar.make(binding.getRoot(), "Habitación editada correctamente", Snackbar.LENGTH_LONG).show())
                        );

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

    private void actualizarUI() {
        int count = habitacionAdapter.getItemCount();
        emptyState.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        contentWithRooms.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        tvRoomCount.setText(count + " de " + MAX_HABITACIONES + " habitaciones creadas");
        fabAddRoom.setAlpha(count >= MAX_HABITACIONES ? 0.6f : 1f);
    }

}
