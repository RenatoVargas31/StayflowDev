package com.codebnb.stayflow.superAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codebnb.stayflow.R;
import com.codebnb.stayflow.superAdmin.adapter.UserAdapter;
import com.codebnb.stayflow.superAdmin.model.User;
import com.codebnb.stayflow.utils.LocalStorageManager;
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
                fabAddHotelAdmin.show(); // Mostrar botón
            } else if (selectedId == R.id.chipAdmins) {
                filterType = "Admin";
                fabAddHotelAdmin.show(); // Mostrar botón
            } else if (selectedId == R.id.chipTaxistas) {
                filterType = "Taxista";
                fabAddHotelAdmin.hide(); // Ocultar botón
            } else if (selectedId == R.id.chipClientes) {
                filterType = "Cliente";
                fabAddHotelAdmin.hide(); // Ocultar botón
            }

            // Guardar el filtro seleccionado
            LocalStorageManager.FilterSettings filters = localStorageManager.getFilters();
            filters.setSelectedUserType(filterType);
            localStorageManager.saveFilters(filters);

            userAdapter.filterByType(filterType);
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
        return R.layout.fragment_gestion_superadmin;
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
    public void onDetailsClick(int position) {
        User user = userList.get(position);

        // Crear intent para UserDetailActivity en lugar de fragment
        Intent intent = new Intent(GestionActivity.this, UserDetailActivity.class);
        intent.putExtra("USER_NAME", user.getName());
        intent.putExtra("USER_ROLE", user.getRole());
        intent.putExtra("USER_ROLE_DESC", user.getRoleDescription());
        intent.putExtra("USER_ENABLED", user.isEnabled());
        startActivity(intent);
    }

    @Override
    public void onStatusChanged(int position, boolean isEnabled) {
        User user = userList.get(position);
        user.setEnabled(isEnabled);
        // Aquí podrías implementar la lógica para guardar el estado en una DB
        Toast.makeText(this,
                user.getName() + " " + (isEnabled ? "habilitado" : "deshabilitado"),
                Toast.LENGTH_SHORT).show();
    }
}