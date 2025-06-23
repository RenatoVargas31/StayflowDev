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
import com.iot.stayflowdev.model.User;
import com.iot.stayflowdev.superAdmin.utils.LocalStorageManager;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

    // Referencia a Firestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // El layout ya se configura en BaseSuperAdminActivity

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

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

        // Inicializar lista de usuarios
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this);
        recyclerViewUsers.setAdapter(userAdapter);

        // Ya no cargamos todos los usuarios aquí
        // loadUsersFromFirestore();

        // Registrar el launcher para el resultado de la actividad
        addAdminLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Cuando se crea un nuevo administrador, actualizamos la lista completa desde Firestore
                        // para asegurar que se muestre el usuario recién creado con todos sus datos
                        Toast.makeText(this, "Administrador creado exitosamente", Toast.LENGTH_SHORT).show();
                        // Aplicar el filtro guardado después de crear un nuevo administrador
                        applySavedFilter();
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
                filterType = "adminhotel";
                fabAddHotelAdmin.show();
                subFilterScrollView.setVisibility(View.GONE);
            } else if (selectedId == R.id.chipTaxistas) {
                filterType = "driver";
                fabAddHotelAdmin.hide();
                subFilterScrollView.setVisibility(View.VISIBLE);
                // Seleccionar "Todos los Taxistas" por defecto
                chipGroupTaxistaFiltro.check(R.id.chipTaxistasTodos);
            } else if (selectedId == R.id.chipClientes) {
                filterType = "usuario";
                fabAddHotelAdmin.hide();
                subFilterScrollView.setVisibility(View.GONE);
            }

            // Guardar el filtro seleccionado
            LocalStorageManager.FilterSettings filters = localStorageManager.getFilters();
            filters.setSelectedUserType(filterType);
            localStorageManager.saveFilters(filters);

            // Filtrar usuarios por tipo
            filterUsersByType(filterType);
        });

        // Configurar listener para el subfiltro de taxistas
        chipGroupTaxistaFiltro.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int selectedId = checkedIds.get(0);
            String subFilterType = "Todos";

            if (selectedId == R.id.chipTaxistasPendientes) {
                subFilterType = "pendiente";
                filterTaxistasByStatus(subFilterType);
            } else if (selectedId == R.id.chipTaxistasHabilitados) {
                subFilterType = "activo";
                filterTaxistasByStatus(subFilterType);
            } else {
                // Si es "Todos", cargar todos los taxistas
                filterUsersByType("driver");
            }
        });

        // Configurar listener para la búsqueda
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterUsersByText(s.toString());
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

    private void loadUsersFromFirestore() {
        // Mostrar un indicador de carga
        showLoading(true);

        db.collection("usuarios")
            .get()
            .addOnCompleteListener(task -> {
                showLoading(false);
                if (task.isSuccessful()) {
                    userList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
                        // Asegurar que el usuario tenga el UID correcto (el del documento)
                        user.setUid(document.getId());
                        userList.add(user);
                    }
                    // Actualizamos tanto la lista original como la lista del adaptador
                    userAdapter.updateFullList(userList);

                    // Verificar si la lista está vacía y mostrar mensaje
                    if (userList.isEmpty()) {
                        showEmptyMessage(true);
                    } else {
                        showEmptyMessage(false);
                    }
                } else {
                    showErrorMessage("Error al cargar usuarios: " + task.getException().getMessage());
                }
            });
    }

    private void showEmptyMessage(boolean show) {
        // Opcionalmente implementa un mensaje de lista vacía
        // ...
    }

    private void filterUsersByType(String rol) {
        if ("Todos".equals(rol)) {
            loadUsersFromFirestore();
            return;
        }

        showLoading(true);
        db.collection("usuarios")
            .whereEqualTo("rol", rol)
            .get()
            .addOnCompleteListener(task -> {
                showLoading(false);
                if (task.isSuccessful()) {
                    userList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
                        // Asegurar que el usuario tenga el UID correcto
                        user.setUid(document.getId());
                        userList.add(user);
                    }
                    // Actualizamos tanto la lista original como la lista del adaptador
                    userAdapter.updateFullList(userList);

                    // Verificar si la lista está vacía y mostrar mensaje
                    if (userList.isEmpty()) {
                        showEmptyMessage(true);
                    } else {
                        showEmptyMessage(false);
                    }
                } else {
                    showErrorMessage("Error al filtrar usuarios: " + task.getException().getMessage());
                }
            });
    }

    private void filterTaxistasByStatus(String statusType) {
        showLoading(true);

        // Convertir el tipo de estado (string) a un valor booleano
        boolean estadoValue = "activo".equals(statusType);

        db.collection("usuarios")
            .whereEqualTo("rol", "driver")
            .whereEqualTo("estado", estadoValue)
            .get()
            .addOnCompleteListener(task -> {
                showLoading(false);
                if (task.isSuccessful()) {
                    userList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
                        // Asegurar que el usuario tenga el UID correcto
                        user.setUid(document.getId());
                        userList.add(user);
                    }
                    // Actualizamos tanto la lista original como la lista del adaptador
                    userAdapter.updateFullList(userList);

                    // Verificar si la lista está vacía y mostrar mensaje
                    if (userList.isEmpty()) {
                        showEmptyMessage(true);
                    } else {
                        showEmptyMessage(false);
                    }
                } else {
                    showErrorMessage("Error al filtrar taxistas: " + task.getException().getMessage());
                }
            });
    }

    private void filterUsersByText(String searchText) {
        if (searchText.isEmpty()) {
            // Si la búsqueda está vacía, volver a cargar datos según el filtro actual
            String currentFilter = localStorageManager.getFilters().getSelectedUserType();
            filterUsersByType(currentFilter);
            return;
        }

        // Convertir a minúsculas para búsqueda sin distinción entre mayúsculas y minúsculas
        String searchLowerCase = searchText.toLowerCase();

        // Filtrar en memoria para búsqueda por texto
        List<User> filteredList = new ArrayList<>();
        for (User user : userList) {
            if ((user.getNombres() != null && user.getNombres().toLowerCase().contains(searchLowerCase)) ||
                (user.getApellidos() != null && user.getApellidos().toLowerCase().contains(searchLowerCase)) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchLowerCase))) {
                filteredList.add(user);
            }
        }

        // Actualizar el adaptador con resultados filtrados
        userAdapter.updateUserList(filteredList);
    }

    private void createNewAdminUser(String nombres, String apellidos, String email) {
        // Este método ya no se utiliza para crear usuarios directamente
        // La creación se realiza en AddHotelAdminActivity con Firebase Auth + Firestore
        // Solo abrimos la actividad para que el usuario ingrese los datos
        Intent intent = new Intent(this, AddHotelAdminActivity.class);
        addAdminLauncher.launch(intent);
    }

    private void updateUserStatus(User user, boolean estado, String reason) {
        if (user.getUid() == null || user.getUid().isEmpty()) {
            showErrorMessage("ID de usuario no válido");
            return;
        }

        showLoading(true);

        // Actualizar en Firestore
        db.collection("usuarios")
            .document(user.getUid())
            .update("estado", estado)
            .addOnSuccessListener(aVoid -> {
                showLoading(false);
                // Actualizar el objeto local
                user.setEstado(estado);
                userAdapter.notifyDataSetChanged();

                // Obtener el nombre del usuario o usar un valor predeterminado si es null
                String userName = user.getName() != null ? user.getName() : "Usuario sin nombre";

                // Mostrar mensaje de confirmación
                String message = userName + " ha sido " + (estado ? "habilitado" : "deshabilitado");
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();

                // TODO: Guardar la razón del cambio en un registro de logs
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                showErrorMessage("Error al actualizar estado: " + e.getMessage());
            });
    }

    private void showLoading(boolean show) {
        // Comentamos o modificamos el código que muestra el overlay para que no oscurezca la pantalla
        View loadingOverlay = findViewById(R.id.loadingOverlay);
        if (loadingOverlay != null) {
            // Establecer la visibilidad en INVISIBLE en lugar de GONE para mantener el espacio
            // pero que no sea visible (o alternativamente, podemos eliminar completamente su visibilidad)
            // loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);

            // O simplemente no hacer nada para que nunca se muestre el overlay
            // También podemos ocultar completamente este overlay si ya no lo necesitamos
            loadingOverlay.setVisibility(View.GONE);
        }
    }

    private void showErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void applySavedFilter() {
        LocalStorageManager.FilterSettings filters = localStorageManager.getFilters();
        String savedFilter = filters.getSelectedUserType();

        // Seleccionar el chip correspondiente
        if (savedFilter != null && !savedFilter.equals("Todos")) {
            switch (savedFilter) {
                case "adminhotel":
                    chipGroupFiltro.check(R.id.chipAdmins);
                    fabAddHotelAdmin.show();
                    break;
                case "driver":
                    chipGroupFiltro.check(R.id.chipTaxistas);
                    fabAddHotelAdmin.hide();
                    break;
                case "usuario":
                    chipGroupFiltro.check(R.id.chipClientes);
                    fabAddHotelAdmin.hide();
                    break;
                default:
                    chipGroupFiltro.check(R.id.chipTodos);
                    fabAddHotelAdmin.show();
                    break;
            }
            filterUsersByType(savedFilter);
        } else {
            // Si no hay filtro guardado o es "Todos", seleccionar "Todos" por defecto
            chipGroupFiltro.check(R.id.chipTodos);
            fabAddHotelAdmin.show();
            loadUsersFromFirestore();
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

    @Override
    public void onDetailsClick(User user) {
        // Crear intent para UserDetailActivity
        Intent intent = new Intent(GestionActivity.this, UserDetailActivity.class);
        intent.putExtra("USER_ID", user.getUid());
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
                // Revertir el switch
                user.setEnabled(!isEnabled);
                userAdapter.notifyDataSetChanged();
                return;
            }

            // Actualizar el estado del usuario en Firestore
            boolean newEstado = isEnabled;
            updateUserStatus(user, newEstado, reasonText);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            // Revertir el switch
            user.setEnabled(!isEnabled);
            userAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        builder.setCancelable(false);
        builder.show();
    }
}



