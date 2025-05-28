package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.databinding.ActivityUbicacionAdminBinding;
import com.google.android.material.chip.Chip;

public class UbicacionAdminActivity extends AppCompatActivity {

    private ActivityUbicacionAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion_admin);

        binding = ActivityUbicacionAdminBinding.bind(findViewById(R.id.rootUbicacionAdmin));

        // Configura Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configura BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
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

        setupAutocomplete();
        setupListeners();
        loadMockupData();
    }

    private void setupAutocomplete() {
        String[] suggestions = {"Av. El Sol 123", "Jr. Cusco 456", "Plaza Mayor 789"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
        binding.autoCompleteDireccion.setAdapter(adapter);
        binding.autoCompleteDireccion.setThreshold(1);

        binding.autoCompleteDireccion.setOnItemClickListener((parent, view, position, id) -> {
            String address = parent.getItemAtPosition(position).toString();
            showSelectedAddress(address);
        });

        binding.autoCompleteDireccion.setOnDismissListener(() -> {
            String text = binding.autoCompleteDireccion.getText().toString();
            if (!text.isEmpty()) {
                showSelectedAddress(text);
            }
        });
    }

    private void showSelectedAddress(String address) {
        binding.chipDireccion.setText(address);
        binding.chipDireccion.setVisibility(View.VISIBLE);
    }

    private void setupListeners() {
        binding.btnAgregarLugar.setOnClickListener(v -> showBottomSheet());
        binding.btnCancelar.setOnClickListener(v -> hideBottomSheet());
        binding.scrim.setOnClickListener(v -> hideBottomSheet());
        binding.btnGuardarLugar.setOnClickListener(v -> savePlace());
    }

    private void showBottomSheet() {
        binding.cardFormularioLugar.setTranslationY(500);
        binding.bottomSheetFormulario.setVisibility(View.VISIBLE);
        binding.cardFormularioLugar.animate().translationY(0).setDuration(300).start();
    }

    private void hideBottomSheet() {
        binding.cardFormularioLugar.animate()
                .translationY(500)
                .setDuration(250)
                .withEndAction(() -> {
                    binding.bottomSheetFormulario.setVisibility(View.GONE);
                    clearForm();
                })
                .start();
    }

    private void clearForm() {
        binding.etNombreLugar.setText("");
        binding.etDistanciaLugar.setText("");
    }

    private void savePlace() {
        String name = binding.etNombreLugar.getText().toString().trim();
        String distance = binding.etDistanciaLugar.getText().toString().trim();

        if (!name.isEmpty() && !distance.isEmpty()) {
            addPlaceCard(name, distance + " m");
            showMessage("Lugar agregado");
            hideBottomSheet();
        } else {
            showMessage("Por favor completa ambos campos");
        }
    }

    private void addPlaceCard(String name, String distance) {
        View card = LayoutInflater.from(this)
                .inflate(R.layout.item_admin_lugar_historico, binding.layoutListaLugares, false);

        ((android.widget.TextView) card.findViewById(R.id.tvNombreLugar)).setText(name);
        ((Chip) card.findViewById(R.id.chipDistancia)).setText(distance);

        card.findViewById(R.id.btnEditar).setOnClickListener(v -> editPlace(name, distance));
        card.findViewById(R.id.btnEliminar).setOnClickListener(v -> removePlace(card));

        card.setAlpha(0f);
        card.setTranslationY(50);
        binding.layoutListaLugares.addView(card);

        card.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(300)
                .start();
    }

    private void editPlace(String name, String distance) {
        binding.etNombreLugar.setText(name);
        binding.etDistanciaLugar.setText(distance.replace(" m", ""));
        showBottomSheet();
    }

    private void removePlace(View card) {
        card.animate()
                .alpha(0f)
                .translationX(card.getWidth())
                .setDuration(300)
                .withEndAction(() -> {
                    binding.layoutListaLugares.removeView(card);
                    showMessage("Lugar eliminado");
                })
                .start();
    }

    private void loadMockupData() {
        addPlaceCard("Plaza de Armas", "300 m");
        addPlaceCard("Museo Central", "500 m");
        addPlaceCard("Catedral Histórica", "450 m");

        binding.autoCompleteDireccion.setText("Av. El Sol 123");
        binding.chipDireccion.setText("Av. El Sol 123");
        binding.chipDireccion.setVisibility(View.VISIBLE);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
