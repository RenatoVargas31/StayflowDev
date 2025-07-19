package com.iot.stayflowdev.superAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.util.Log;

import com.iot.stayflowdev.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InicioActivity extends BaseSuperAdminActivity {

    private FirebaseFirestore db;
    private ListenerRegistration activeUsersListener;
    private ListenerRegistration connectedUsersListener;
    private TextView activeUsersCountText;
    private TextView activeUsersDetailText;
    private TextView dateTimeText;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.superadmin_base_superadmin_activity;
    }

    @Override
    protected int getBottomNavigationSelectedItem() {
        return R.id.nav_inicio;
    }

    @Override
    protected String getToolbarTitle() {
        return "Panel de Control";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar el contenido específico de esta activity en el content_frame
        LayoutInflater.from(this).inflate(R.layout.superadmin_inicio_superadmin,
                findViewById(R.id.content_frame), true);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Inicializar referencias a vistas
        initializeViews();

        // Configurar fecha y hora actual
        setupDateTime();

        // Configurar listeners para usuarios activos
        setupActiveUsersListener();

        // Configurar listeners específicos de esta pantalla
        setupContentListeners();
    }

    private void initializeViews() {
        activeUsersCountText = findViewById(R.id.activeUsersCountText);
        activeUsersDetailText = findViewById(R.id.activeUsersDetailText);
        dateTimeText = findViewById(R.id.dateTimeText);
    }

    private void setupDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d 'de' MMMM, yyyy - h:mm a",
            new Locale("es", "ES"));
        String currentDateTime = dateFormat.format(new Date());

        if (dateTimeText != null) {
            dateTimeText.setText(currentDateTime);
        }
    }

    private void setupActiveUsersListener() {
        // Listener para usuarios ACTIVOS (estado = true, cuentas habilitadas)
        activeUsersListener = db.collection("usuarios")
                .whereEqualTo("estado", true)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w("InicioActivity", "Error al escuchar usuarios activos", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        int activeUsersCount = queryDocumentSnapshots.size();
                        updateActiveUsersCountUI(activeUsersCount);
                        Log.d("InicioActivity", "Usuarios activos (cuentas habilitadas): " + activeUsersCount);
                    }
                });

        // Listener para usuarios CONECTADOS (conectado = true, en línea ahora)
        connectedUsersListener = db.collection("usuarios")
                .whereEqualTo("conectado", true)
                .whereEqualTo("estado", true) // Solo usuarios con cuenta activa Y conectados
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w("InicioActivity", "Error al escuchar usuarios conectados", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        int connectedUsersCount = queryDocumentSnapshots.size();
                        updateConnectedUsersUI(connectedUsersCount);
                        Log.d("InicioActivity", "Usuarios conectados actualmente: " + connectedUsersCount);
                    }
                });
    }

    private void updateActiveUsersCountUI(int count) {
        // Actualiza el número grande en la tarjeta superior (usuarios con cuenta activa)
        if (activeUsersCountText != null) {
            activeUsersCountText.setText(String.valueOf(count));
        }
    }

    private void updateConnectedUsersUI(int count) {
        // Actualiza la descripción detallada (usuarios realmente conectados)
        if (activeUsersDetailText != null) {
            String detailText = count + " conectados actualmente";
            activeUsersDetailText.setText(detailText);
        }
    }

    private void setupContentListeners() {
        MaterialButton btnLogs = findViewById(R.id.goToLogsButton);
        MaterialButton btnUsers = findViewById(R.id.goToUsersButton);

        if (btnLogs != null) {
            btnLogs.setOnClickListener(v -> {
                Intent intent = new Intent(InicioActivity.this, LogsActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (btnUsers != null) {
            btnUsers.setOnClickListener(v -> {
                Intent intent = new Intent(InicioActivity.this, PerfilActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar los listeners cuando la activity se destruye
        if (activeUsersListener != null) {
            activeUsersListener.remove();
        }
        if (connectedUsersListener != null) {
            connectedUsersListener.remove();
        }
    }
}