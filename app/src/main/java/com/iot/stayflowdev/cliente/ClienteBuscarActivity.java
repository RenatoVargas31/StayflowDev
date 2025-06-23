package com.iot.stayflowdev.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.cliente.adapter.HotelesTopAdapter;
import com.iot.stayflowdev.databinding.ActivityClienteBuscarBinding;
import com.iot.stayflowdev.model.Hotel;
import com.iot.stayflowdev.viewmodels.HotelViewModel;

import java.util.List;

public class ClienteBuscarActivity extends AppCompatActivity implements HotelesTopAdapter.OnHotelClickListener {

    private ActivityClienteBuscarBinding binding;
    private static final String TAG = "ClienteBuscarActivity";
    private HotelViewModel hotelViewModel;
    private HotelesTopAdapter hotelesTopAdapter;

    // Clave para pasar términos de búsqueda en Intent
    public static final String EXTRA_BUSQUEDA = "com.iot.stayflowdev.EXTRA_BUSQUEDA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Inicializar ViewBinding
        binding = ActivityClienteBuscarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar toolbar
        setSupportActionBar(binding.toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // Sin padding inferior
            return insets;
        });

        // Inicializar ViewModel
        hotelViewModel = new ViewModelProvider(this).get(HotelViewModel.class);

        // Configurar RecyclerView de hoteles mejor valorados
        setupHotelesTopRecyclerView();

        // Cargar datos de hoteles mejor valorados
        cargarHotelesMejorValorados();

        // Configurar navegación inferior
        setupBottomNavigation();

        // Configurar botón de búsqueda
        setupBusqueda();
    }

    private void setupBusqueda() {
        binding.button.setOnClickListener(v -> {
            // Obtener texto de búsqueda del campo
            String textoBusqueda = "";
            if (binding.textInputLayout2.getEditText() != null) {
                textoBusqueda = binding.textInputLayout2.getEditText().getText().toString().trim();
            }

            // Validar si el campo no está vacío
            if (TextUtils.isEmpty(textoBusqueda)) {
                binding.textInputLayout2.setError("Ingrese un nombre de hotel para buscar");
                return;
            }

            // Si pasa la validación, limpiar error y comenzar búsqueda
            binding.textInputLayout2.setError(null);

            // Crear intent para pantalla de resultados
            Intent intent = new Intent(ClienteBuscarActivity.this, ClienteBusquedaHotelActivity.class);
            intent.putExtra(EXTRA_BUSQUEDA, textoBusqueda);
            startActivity(intent);
        });
    }

    private void setupHotelesTopRecyclerView() {
        // Inicializar adaptador con listener para los clics
        hotelesTopAdapter = new HotelesTopAdapter(this);

        // Configurar RecyclerView con LinearLayoutManager horizontal para el carrusel
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerHotelesTop.setLayoutManager(layoutManager);
        binding.recyclerHotelesTop.setAdapter(hotelesTopAdapter);
    }

    private void cargarHotelesMejorValorados() {
        // Mostrar estado de carga
        binding.progressBarHotelesTop.setVisibility(View.VISIBLE);
        binding.recyclerHotelesTop.setVisibility(View.GONE);
        binding.textErrorHotelesTop.setVisibility(View.GONE);

        // Observar los hoteles mejor valorados desde el ViewModel
        hotelViewModel.getHotelesMejorValorados().observe(this, this::mostrarHotelesMejorValorados);

        // Observar errores
        hotelViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                mostrarError(error);
            }
        });
    }

    private void mostrarHotelesMejorValorados(List<Hotel> hoteles) {
        binding.progressBarHotelesTop.setVisibility(View.GONE);

        if (hoteles != null && !hoteles.isEmpty()) {
            // Actualizar adaptador con los hoteles
            hotelesTopAdapter.setHoteles(hoteles);
            binding.recyclerHotelesTop.setVisibility(View.VISIBLE);
            binding.textErrorHotelesTop.setVisibility(View.GONE);

            Log.d(TAG, "Hoteles mejor valorados cargados: " + hoteles.size());
        } else {
            // No hay hoteles que mostrar
            binding.recyclerHotelesTop.setVisibility(View.GONE);
            binding.textErrorHotelesTop.setText("No hay hoteles disponibles");
            binding.textErrorHotelesTop.setVisibility(View.VISIBLE);

            Log.d(TAG, "No hay hoteles mejor valorados para mostrar");
        }
    }

    private void mostrarError(String mensajeError) {
        binding.progressBarHotelesTop.setVisibility(View.GONE);
        binding.recyclerHotelesTop.setVisibility(View.GONE);
        binding.textErrorHotelesTop.setText(mensajeError);
        binding.textErrorHotelesTop.setVisibility(View.VISIBLE);

        Log.e(TAG, "Error al cargar hoteles mejor valorados: " + mensajeError);
    }

    // Implementación de la interfaz OnHotelClickListener
    @Override
    public void onHotelClick(Hotel hotel) {
        // Navegar a la pantalla de detalle del hotel
        Intent intent = new Intent(this, ClienteDetalleHotelActivity.class);
        intent.putExtra(ClienteDetalleHotelActivity.EXTRA_HOTEL_ID, hotel.getId());
        startActivity(intent);
    }

    // Inflar el menú
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.buscar_menu, menu);
        return true;
    }

    // Manejar eventos del menú
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            Log.d(TAG, "Botón de notificaciones presionado");
            // Aquí implementaremos la funcionalidad de notificaciones más adelante
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupBottomNavigation() {
        // Establecer Buscar como seleccionado
        binding.bottomNavigation.setSelectedItemId(R.id.nav_buscar);

        // Configurar listener de navegación
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Si ya estamos en esta actividad, no hacer nada
            if (itemId == R.id.nav_buscar) {
                return true;
            }

            // Navegación según el ítem seleccionado
            if (itemId == R.id.nav_favoritos) {
                navigateToActivity(ClienteFavoritosActivity.class);
                return true;
            } else if (itemId == R.id.nav_reservas) {
                navigateToActivity(ClienteReservasActivity.class);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                navigateToActivity(ClientePerfilActivity.class);
                return true;
            }

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
        // Si el usuario está en la pantalla principal, comportamiento normal de retroceso
        super.onBackPressed();
    }
}