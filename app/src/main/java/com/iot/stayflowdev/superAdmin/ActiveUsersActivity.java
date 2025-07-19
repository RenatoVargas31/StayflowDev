package com.iot.stayflowdev.superAdmin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.superAdmin.adapter.ActiveUserAdapter;
import com.iot.stayflowdev.model.User;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class ActiveUsersActivity extends AppCompatActivity {

    private static final String TAG = "ActiveUsersActivity";
    private FirebaseFirestore db;
    private ListenerRegistration connectedUsersListener;
    private RecyclerView recyclerView;
    private ActiveUserAdapter adapter;
    private MaterialButton btnRefresh;
    private TextView tvTotalCount;
    private List<User> connectedUsersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_users);

        // Configurar toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Usuarios Conectados");
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        initializeViews();

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar listener para usuarios conectados
        setupConnectedUsersListener();

        // Configurar botón de actualizar
        btnRefresh.setOnClickListener(v -> refreshUsersList());
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewActiveUsers);
        btnRefresh = findViewById(R.id.btnRefresh);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        connectedUsersList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new ActiveUserAdapter(connectedUsersList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupConnectedUsersListener() {
        // Escuchar usuarios conectados en tiempo real
        connectedUsersListener = db.collection("usuarios")
                .whereEqualTo("conectado", true)
                .whereEqualTo("estado", true)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Error al escuchar usuarios conectados", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        connectedUsersList.clear();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            User user = document.toObject(User.class);
                            if (user != null) {
                                user.setUid(document.getId());
                                connectedUsersList.add(user);
                            }
                        }

                        // Actualizar UI
                        updateUI();

                        Log.d(TAG, "Lista actualizada: " + connectedUsersList.size() + " usuarios conectados");
                    }
                });
    }

    private void updateUI() {
        // Actualizar contador total
        int totalConnected = connectedUsersList.size();
        String countText = "Total: " + totalConnected + " usuario" + (totalConnected != 1 ? "s" : "") + " conectado" + (totalConnected != 1 ? "s" : "");
        tvTotalCount.setText(countText);

        // Actualizar adapter
        adapter.notifyDataSetChanged();

        // Mostrar/ocultar mensaje de "sin usuarios"
        findViewById(R.id.tvEmptyState).setVisibility(
            connectedUsersList.isEmpty() ? View.VISIBLE : View.GONE
        );
    }

    private void refreshUsersList() {
        // El listener automático ya mantiene la lista actualizada,
        // pero podemos mostrar un feedback visual
        btnRefresh.setEnabled(false);
        btnRefresh.setText("Actualizando...");

        // Reactivar botón después de 1 segundo
        btnRefresh.postDelayed(() -> {
            btnRefresh.setEnabled(true);
            btnRefresh.setText("🔄 Actualizar");
        }, 1000);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar el listener
        if (connectedUsersListener != null) {
            connectedUsersListener.remove();
        }
    }
}
