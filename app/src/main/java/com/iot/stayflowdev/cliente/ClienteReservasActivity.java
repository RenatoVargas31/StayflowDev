package com.iot.stayflowdev.cliente;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.cliente.adapter.ClienteReservaAdapter;
import com.iot.stayflowdev.databinding.ActivityClienteReservasBinding;
import com.iot.stayflowdev.viewmodels.ReservaViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ClienteReservasActivity extends AppCompatActivity {

    private ActivityClienteReservasBinding binding;
    private ReservaViewModel viewModel;
    private ClienteReservaAdapter adapter;
    private static final String TAG = "ClienteReservasActivity";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Inicializar ViewBinding
        binding = ActivityClienteReservasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // Sin padding inferior
            return insets;
        });

        // Configurar navegación inferior
        setupBottomNavigation();

        // Inicializar ViewModel y Adapter
        setupViewModel();

        // Configurar la UI y eventos
        setupUI();
    }

    private void setupViewModel() {
        // Inicializar el ViewModel
        viewModel = new ViewModelProvider(this).get(ReservaViewModel.class);

        // Observar cambios en la lista de reservas
        viewModel.getReservas().observe(this, reservas -> {
            if (reservas != null) {
                Log.d(TAG, "Reservas recibidas: " + reservas.size());
                adapter.setReservas(reservas);

                // Mostrar/ocultar elementos según los resultados
                boolean hayResultados = !reservas.isEmpty();
                binding.textViewResultadosTitulo.setVisibility(hayResultados ? View.VISIBLE : View.GONE);
                binding.recyclerViewReservas.setVisibility(hayResultados ? View.VISIBLE : View.GONE);
                binding.textViewNoReservas.setVisibility(!hayResultados && !viewModel.isLoading().getValue() ? View.VISIBLE : View.GONE);
            }
        });

        // Observar estado de carga
        viewModel.isLoading().observe(this, isLoading -> {
            binding.progressBarReservas.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                binding.textViewNoReservas.setVisibility(View.GONE);
                binding.textViewError.setVisibility(View.GONE);
            }
        });

        // Observar errores
        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.textViewError.setText(error);
                binding.textViewError.setVisibility(View.VISIBLE);
            } else {
                binding.textViewError.setVisibility(View.GONE);
            }
        });
    }

    private void setupUI() {
        // Configurar RecyclerView
        adapter = new ClienteReservaAdapter(this, viewModel);
        binding.recyclerViewReservas.setAdapter(adapter);
        binding.recyclerViewReservas.setLayoutManager(new LinearLayoutManager(this));

        // Configurar campos de fecha
        updateDateFields();

        // Configurar selección de fechas
        binding.editTextFechaDesde.setOnClickListener(v -> showDatePicker(true));
        binding.editTextFechaHasta.setOnClickListener(v -> showDatePicker(false));

        // Configurar botón de búsqueda
        binding.buttonBuscarReservas.setOnClickListener(v -> {
            viewModel.buscarReservasPorFecha();
        });
    }

    private void updateDateFields() {
        binding.editTextFechaDesde.setText(viewModel.formatDate(viewModel.getFechaInicio()));
        binding.editTextFechaHasta.setText(viewModel.formatDate(viewModel.getFechaFin()));
    }

    private void showDatePicker(boolean isStartDate) {
        // Obtener la fecha actual seleccionada
        Date currentDate = isStartDate ? viewModel.getFechaInicio() : viewModel.getFechaFin();
        Calendar calendar = Calendar.getInstance();

        if (currentDate != null) {
            calendar.setTime(currentDate);
        }

        // Crear DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                // Crear nueva fecha seleccionada
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth);

                // Actualizar fecha en el ViewModel
                if (isStartDate) {
                    viewModel.setFechaInicio(selectedCalendar.getTime());
                } else {
                    viewModel.setFechaFin(selectedCalendar.getTime());
                }

                // Actualizar campos de texto
                updateDateFields();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Mostrar el diálogo
        datePickerDialog.show();
    }

    private void setupBottomNavigation() {
        // Establecer Reservas como seleccionado
        binding.bottomNavigation.setSelectedItemId(R.id.nav_reservas);

        // Configurar listener de navegación
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Si ya estamos en esta actividad, no hacer nada
            if (itemId == R.id.nav_reservas) {
                return true;
            }

            // Navegación según el ítem seleccionado
            if (itemId == R.id.nav_buscar) {
                navigateToActivity(ClienteBuscarActivity.class);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                navigateToActivity(ClientePerfilActivity.class);
                return true;
            }
            /*
            else if (itemId == R.id.nav_favoritos) {
                navigateToActivity(ClienteFavoritosActivity.class);
                return true;
            }
            */
            return false;
        });
    }

    // Método para navegar sin animación
    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        overridePendingTransition(0, 0); // Sin animación
        finish();
    }

    @Override
    public void onBackPressed() {
        // Al presionar atrás, regresar a la pantalla principal
        navigateToActivity(ClienteBuscarActivity.class);
    }
}