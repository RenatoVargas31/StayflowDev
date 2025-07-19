package com.iot.stayflowdev.superAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.superAdmin.adapter.UsersListAdapter;
import com.iot.stayflowdev.superAdmin.model.UserItem;

import java.util.ArrayList;
import java.util.List;

public class SelectUserForChatActivity extends BaseSuperAdminActivity {

    private static final String TAG = "SelectUserForChat";

    private FirebaseFirestore db;
    private RecyclerView usersRecyclerView;
    private UsersListAdapter usersAdapter;
    private List<UserItem> usersList = new ArrayList<>();

    @Override
    protected int getLayoutResourceId() {
        return R.layout.superadmin_base_superadmin_activity;
    }

    @Override
    protected int getBottomNavigationSelectedItem() {
        return R.id.nav_reportes;
    }

    @Override
    protected String getToolbarTitle() {
        return "Seleccionar Usuario para Chat";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ocultar el bottom navigation en la lista de usuarios para chat
        if (bottomNavigation != null) {
            bottomNavigation.setVisibility(android.view.View.GONE);
        }

        // Inflar el contenido específico
        LayoutInflater.from(this).inflate(R.layout.superadmin_select_user_chat,
                findViewById(R.id.content_frame), true);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        initializeViews();

        // Cargar usuarios
        loadUsers();
    }

    private void initializeViews() {
        usersRecyclerView = findViewById(R.id.usersRecyclerView);

        // Configurar RecyclerView
        usersAdapter = new UsersListAdapter(usersList, this::onUserSelected);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(usersAdapter);
    }

    private void loadUsers() {
        // Cargar todos los usuarios excepto el SuperAdmin actual
        db.collection("usuarios")
                .whereEqualTo("estado", true) // Solo usuarios activos
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    usersList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getId();
                        String name = document.getString("nombres") + " " + document.getString("apellidos");
                        String email = document.getString("email");
                        String role = document.getString("rol");
                        Boolean connected = document.getBoolean("conectado");

                        // Filtrar el SuperAdmin actual si es necesario
                        if (role != null && !role.equals("superadmin")) {
                            UserItem userItem = new UserItem(userId, name, email, role,
                                                           connected != null && connected);
                            usersList.add(userItem);
                        }
                    }

                    usersAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Usuarios cargados: " + usersList.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar usuarios", e);
                    Toast.makeText(this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
                });
    }

    private void onUserSelected(UserItem user) {
        // Iniciar chat con el usuario seleccionado
        Intent intent = new Intent(this, MessagingTestActivity.class);
        intent.putExtra("USER_ID", user.getId());
        intent.putExtra("USER_NAME", user.getName());
        intent.putExtra("USER_EMAIL", user.getEmail());
        intent.putExtra("USER_ROLE", user.getRole());
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
