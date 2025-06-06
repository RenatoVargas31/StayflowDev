package com.iot.stayflowdev.superAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.HorizontalScrollView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.superAdmin.adapter.UserAdapter;
import com.iot.stayflowdev.superAdmin.model.User;
import com.iot.stayflowdev.superAdmin.utils.LocalStorageManager;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class GestionActivity extends BaseSuperAdminActivity implements UserAdapter.OnUserClickListener {

    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<User> userList;
    private EditText searchEditText;
    private ExtendedFloatingActionButton fabAddHotelAdmin;
    private ChipGroup chipGroupFiltro;
    private ChipGroup chipGroupTaxistaFiltro;
    private HorizontalScrollView subFilterScrollView;
    private ActivityResultLauncher<Intent> addAdminLauncher;
    private LocalStorageManager localStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // El layout ya se configura en BaseSuperAdminActivity

        // Inicializar LocalStorageManager
        localStorageManager = new LocalStorageManager(this);

        // Inicializar vistas
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        chipGroupFiltro = findViewById(R.id.chipGroupFiltro);
        chipGroupTaxistaFiltro = findViewById(R.id.chipGroupTaxistaFiltro);
        subFilterScrollView = findViewById(R.id.subFilterScrollView);
        searchEditText = findViewById(R.id.searchEditText);
        fabAddHotelAdmin = findViewById(R.id.fabAddHotelAdmin);

        // Configurar RecyclerView
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar datos (datos hardcodeados)
        initUserData();

        // Configurar adaptador y mostrar datos inmediatamente
        userAdapter = new UserAdapter(userList, this);
        recyclerViewUsers.setAdapter(userAdapter);
        userAdapter.notifyDataSetChanged(); // Forzar actualización de la vista

        // Registrar el launcher para el resultado de la actividad
        addAdminLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Extraer los datos del Intent
                        Intent data = result.getData();
                        String name = data.getStringExtra(AddHotelAdminActivity.EXTRA_ADMIN_NAME);
                        String role = data.getStringExtra(AddHotelAdminActivity.EXTRA_ADMIN_ROLE);
                        String roleDesc = data.getStringExtra(AddHotelAdminActivity.EXTRA_ADMIN_ROLE_DESC);
                        boolean enabled = data.getBooleanExtra(AddHotelAdminActivity.EXTRA_ADMIN_ENABLED, true);

                        // Crear y añadir el nuevo usuario
                        User newAdmin = new User(name, role, roleDesc, enabled);
                        addNewUser(newAdmin);

                        Snackbar.make(findViewById(android.R.id.content), "Administrador agregado con éxito", Snackbar.LENGTH_SHORT).show();
                    }
                });

        // Configurar listener para los chips
        chipGroupFiltro.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int selectedId = checkedIds.get(0);
            String filterType = "Todos";

            if (selectedId == R.id.chipTodos) {
                filterType = "Todos";
                fabAddHotelAdmin.show();
                subFilterScrollView.setVisibility(View.GONE);
            } else if (selectedId == R.id.chipAdmins) {
                filterType = "Admin";
                fabAddHotelAdmin.show();
                subFilterScrollView.setVisibility(View.GONE);
            } else if (selectedId == R.id.chipTaxistas) {
                filterType = "Taxista";
                fabAddHotelAdmin.hide();
                subFilterScrollView.setVisibility(View.VISIBLE);
                // Seleccionar "Todos los Taxistas" por defecto
                chipGroupTaxistaFiltro.check(R.id.chipTaxistasTodos);
            } else if (selectedId == R.id.chipClientes) {
                filterType = "Cliente";
                fabAddHotelAdmin.hide();
                subFilterScrollView.setVisibility(View.GONE);
            }

            // Guardar el filtro seleccionado
            LocalStorageManager.FilterSettings filters = localStorageManager.getFilters();
            filters.setSelectedUserType(filterType);
            localStorageManager.saveFilters(filters);

            userAdapter.filterByType(filterType);
        });

        // Configurar listener para el subfiltro de taxistas
        chipGroupTaxistaFiltro.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int selectedId = checkedIds.get(0);
            String subFilterType = "Todos";

            if (selectedId == R.id.chipTaxistasPendientes) {
                subFilterType = "Pendientes";
            } else if (selectedId == R.id.chipTaxistasHabilitados) {
                subFilterType = "Habilitados";
            }

            userAdapter.filterTaxistasByStatus(subFilterType);
        });

        // Configurar listener para la búsqueda
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                userAdapter.filterByText(s.toString());
            }
        });

        // Configurar el botón de agregar administrador
        fabAddHotelAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(GestionActivity.this, AddHotelAdminActivity.class);
            addAdminLauncher.launch(intent);
        });

        // Aplicar el filtro guardado después de mostrar los datos iniciales
        applySavedFilter();
    }

    private void applySavedFilter() {
        LocalStorageManager.FilterSettings filters = localStorageManager.getFilters();
        String savedFilter = filters.getSelectedUserType();

        // Seleccionar el chip correspondiente
        if (savedFilter != null && !savedFilter.equals("Todos")) {
            switch (savedFilter) {
                case "Admin":
                    chipGroupFiltro.check(R.id.chipAdmins);
                    fabAddHotelAdmin.show();
                    break;
                case "Taxista":
                    chipGroupFiltro.check(R.id.chipTaxistas);
                    fabAddHotelAdmin.hide();
                    break;
                case "Cliente":
                    chipGroupFiltro.check(R.id.chipClientes);
                    fabAddHotelAdmin.hide();
                    break;
                default:
                    chipGroupFiltro.check(R.id.chipTodos);
                    fabAddHotelAdmin.show();
                    break;
            }
            userAdapter.filterByType(savedFilter);
        } else {
            // Si no hay filtro guardado o es "Todos", seleccionar "Todos" por defecto
            chipGroupFiltro.check(R.id.chipTodos);
            fabAddHotelAdmin.show();
            userAdapter.filterByType("Todos");
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.superadmin_gestion_superadmin;
    }

    @Override
    protected int getBottomNavigationSelectedItem() {
        return R.id.nav_gestion;
    }

    @Override
    protected String getToolbarTitle() {
        return "Gestión";
    }

    private void initUserData() {
        // Datos hardcodeados para pruebas
        userList = new ArrayList<>();
        userList.add(new User("Julian Casablancas", "Admin", "Admin Hotel El Cielo", false));
        userList.add(new User("Juan Perez", "Taxista", "Taxista", true));
        userList.add(new User("Luis Navejas", "Cliente", "Cliente", true));
        userList.add(new User("María González", "Cliente", "Cliente", true));
        userList.add(new User("Carlos López", "Admin", "Admin Hotel Las Estrellas", true));
        userList.add(new User("Ana Torres", "Taxista", "Taxista", false));
        userList.add(new User("Mario Vargas Llosa", "Admin", "Admin Hotel Libertad", true));
        userList.add(new User("Alejandra Pizarnik", "Cliente", "Cliente", false));
        userList.add(new User("Idea Vilariño", "Cliente", "Cliente", true));
        userList.add(new User("Fiódor Dostoyevski", "Taxista", "Taxista", false));
        userList.add(new User("Franz Kafka", "Admin", "Admin Hotel Metamorfosis", true));
        userList.add(new User("Jesús Andrés Luján Carrión", "Cliente", "Cliente", true));
        userList.add(new User("Joji", "Cliente", "Cliente", true));
        userList.add(new User("Haruki Murakami", "Admin", "Admin Hotel Tokio", false));
        userList.add(new User("Albert Camus", "Cliente", "Cliente", false));
        userList.add(new User("Tralalero tralala", "Cliente", "Cliente", true));
        userList.add(new User("María Zardoya", "Admin", "Admin Hotel Espectro", true));
    }

    private void addNewUser(User newUser) {
        // Añadir al inicio de la lista para que sea visible inmediatamente
        userAdapter.addUser(newUser);
        // Hacer scroll hasta el primer elemento para mostrar el nuevo usuario
        recyclerViewUsers.smoothScrollToPosition(0);
    }

    @Override
    public void onDetailsClick(User user) {
        // Crear intent para UserDetailActivity en lugar de fragment
        Intent intent = new Intent(GestionActivity.this, UserDetailActivity.class);
        intent.putExtra("USER_NAME", user.getName());
        intent.putExtra("USER_ROLE", user.getRole());
        intent.putExtra("USER_ROLE_DESC", user.getRoleDescription());
        intent.putExtra("USER_ENABLED", user.isEnabled());
        startActivity(intent);
    }

    @Override
    public void onStatusChanged(User user, boolean isEnabled, String reason) {
        // Crear el diálogo de confirmación
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(isEnabled ? "Habilitar Usuario" : "Deshabilitar Usuario");
        
        // Crear el layout para el diálogo
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);

        // Agregar el mensaje de confirmación
        TextView messageView = new TextView(this);
        messageView.setText("¿Estás seguro que deseas " + (isEnabled ? "habilitar" : "deshabilitar") + " a " + user.getName() + "?");
        messageView.setTextSize(16);
        layout.addView(messageView);

        // Agregar espacio
        layout.addView(new View(this), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                20));

        // Agregar campo para la razón
        EditText reasonInput = new EditText(this);
        reasonInput.setHint("Ingrese la razón del cambio");
        reasonInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        reasonInput.setMinLines(2);
        reasonInput.setMaxLines(4);
        layout.addView(reasonInput);

        builder.setView(layout);

        // Configurar los botones
        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String reasonText = reasonInput.getText().toString().trim();
            if (reasonText.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese una razón", Toast.LENGTH_SHORT).show();
                // Revertir el switch y actualizar la vista
                user.setEnabled(!isEnabled);
                userAdapter.notifyDataSetChanged();
                return;
            }
            
            // Actualizar el estado del usuario
            user.setEnabled(isEnabled);
            
            // Mostrar mensaje de confirmación
            String message = user.getName() + " ha sido " + (isEnabled ? "habilitado" : "deshabilitado");
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                    .setAction("Deshacer", v -> {
                        user.setEnabled(!isEnabled);
                        userAdapter.notifyDataSetChanged();
                    })
                    .show();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            // Revertir el switch a su estado anterior
            user.setEnabled(!isEnabled);
            userAdapter.notifyDataSetChanged();
        });

        // Mostrar el diálogo
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
}