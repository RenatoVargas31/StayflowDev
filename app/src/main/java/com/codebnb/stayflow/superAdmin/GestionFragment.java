package com.codebnb.stayflow.superAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codebnb.stayflow.R;
import com.codebnb.stayflow.superAdmin.adapter.UserAdapter;
import com.codebnb.stayflow.superAdmin.model.User;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class GestionFragment extends Fragment implements UserAdapter.OnUserClickListener {

    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<User> userList;
    private EditText searchEditText;
    private ExtendedFloatingActionButton fabAddHotelAdmin;
    private ChipGroup chipGroupFiltro;
    private ActivityResultLauncher<Intent> addAdminLauncher;

    public GestionFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gestion_superadmin, container, false);

        // Inicializar vistas
        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        chipGroupFiltro = view.findViewById(R.id.chipGroupFiltro);
        searchEditText = view.findViewById(R.id.searchEditText);
        fabAddHotelAdmin = view.findViewById(R.id.fabAddHotelAdmin);

        // Configurar RecyclerView
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar datos (datos hardcodeados)
        initUserData();

        // Configurar adaptador
        userAdapter = new UserAdapter(userList, this);
        recyclerViewUsers.setAdapter(userAdapter);

        // Registrar el launcher para el resultado de la actividad
        addAdminLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        // Extraer los datos del Intent
                        Intent data = result.getData();
                        String name = data.getStringExtra(AddHotelAdminActivity.EXTRA_ADMIN_NAME);
                        String role = data.getStringExtra(AddHotelAdminActivity.EXTRA_ADMIN_ROLE);
                        String roleDesc = data.getStringExtra(AddHotelAdminActivity.EXTRA_ADMIN_ROLE_DESC);
                        boolean enabled = data.getBooleanExtra(AddHotelAdminActivity.EXTRA_ADMIN_ENABLED, true);

                        // Crear y añadir el nuevo usuario
                        User newAdmin = new User(name, role, roleDesc, enabled);
                        addNewUser(newAdmin);

                        Snackbar.make(view, "Administrador agregado con éxito", Snackbar.LENGTH_SHORT).show();
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
            Intent intent = new Intent(getActivity(), AddHotelAdminActivity.class);
            addAdminLauncher.launch(intent);
        });

        // Inicialmente, ver si debemos mostrar el botón
        int checkedChipId = chipGroupFiltro.getCheckedChipId();
        if (checkedChipId == R.id.chipTodos || checkedChipId == R.id.chipAdmins) {
            fabAddHotelAdmin.show();
        } else {
            fabAddHotelAdmin.hide();
        }

        return view;
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

        // Crear instancia del fragmento de detalles con los datos del usuario
        UserDetailFragment detailFragment = UserDetailFragment.newInstance(
                user.getName(),
                user.getRole(),
                user.getRoleDescription(),
                user.isEnabled()
        );

        // Realizar la transacción del fragmento
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)  // Permite volver atrás con el botón back
                .commit();
    }

    @Override
    public void onStatusChanged(int position, boolean isEnabled) {
        User user = userList.get(position);
        user.setEnabled(isEnabled);
        // Aquí podrías implementar la lógica para guardar el estado en una DB
        Toast.makeText(getContext(),
                user.getName() + " " + (isEnabled ? "habilitado" : "deshabilitado"),
                Toast.LENGTH_SHORT).show();
    }
}