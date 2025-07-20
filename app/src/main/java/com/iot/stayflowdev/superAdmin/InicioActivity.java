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
    private ListenerRegistration recentLogsListener;
    private ListenerRegistration logs24hListener; // Nuevo listener para logs de 24h
    private ListenerRegistration systemMetricsListener; // Nuevo listener para métricas del sistema
    private TextView activeUsersCountText;
    private TextView activeUsersDetailText;
    private TextView dateTimeText;
    private TextView logsCountText;
    private TextView logsDetailText;

    // Variables para métricas del sistema
    private TextView systemStatusText;
    // private TextView hotelsCountText;
    // private TextView connectivityStatusText;

    // Variables para métricas de Firebase
    private TextView dbOperationsText;
    private TextView responseTimeText;
    private TextView dataUsageText;

    // Variables para tracking de métricas
    private int dbOperationsCount = 0;
    private long lastOperationTime = 0;
    private long totalResponseTime = 0;
    private int responseTimeCount = 0;

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

        // Configurar listener para logs recientes
        setupRecentLogsListener();

        // Configurar listener para métricas del sistema
        setupSystemMetricsListener();

        // Configurar click en el cuadro de usuarios activos
        setupActiveUsersCardClick();

        // Configurar click en las tarjetas de logs
        setupLogsCardClick();

        // Configurar click en la sección detallada de logs
        setupLogsDetailCardClick();

        // Configurar click en la tarjeta de rendimiento del sistema
        setupSystemMetricsCardClick();

        // Configurar listeners específicos de esta pantalla
        setupContentListeners();
    }

    private void initializeViews() {
        activeUsersCountText = findViewById(R.id.activeUsersCountText);
        activeUsersDetailText = findViewById(R.id.activeUsersDetailText);
        dateTimeText = findViewById(R.id.dateTimeText);
        logsCountText = findViewById(R.id.logsCountText);
        logsDetailText = findViewById(R.id.logsDetailText);

        // Inicializar vistas para métricas del sistema
        systemStatusText = findViewById(R.id.systemStatusText);
        // Comentar las vistas que no existen en el layout actual
        // hotelsCountText = findViewById(R.id.hotelsCountText);
        // connectivityStatusText = findViewById(R.id.connectivityStatusText);

        // Inicializar vistas para métricas de Firebase
        dbOperationsText = findViewById(R.id.dbOperationsText);
        responseTimeText = findViewById(R.id.responseTimeText);
        dataUsageText = findViewById(R.id.dataUsageText);
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
        MaterialButton btnMessagingTest = findViewById(R.id.goToMessagingTestButton);

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

        if (btnMessagingTest != null) {
            btnMessagingTest.setOnClickListener(v -> {
                Intent intent = new Intent(InicioActivity.this, SelectUserForChatActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }

    private void setupActiveUsersCardClick() {
        // Configurar el click en la tarjeta grande de usuarios activos (sección "Actividad del Sistema")
        findViewById(R.id.activeUsersDetailCard).setOnClickListener(v -> {
            // Abrir la pantalla de usuarios conectados
            Intent intent = new Intent(InicioActivity.this, ActiveUsersActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    private void setupRecentLogsListener() {
        // Listener para logs SIN LEER (leido = false)
        recentLogsListener = db.collection("system_logs")
                .whereEqualTo("leido", false)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w("InicioActivity", "Error al escuchar logs sin leer", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        int unreadLogsCount = queryDocumentSnapshots.size();
                        updateLogsUI(unreadLogsCount);
                        Log.d("InicioActivity", "Logs sin leer: " + unreadLogsCount);
                    }
                });

        // Listener para logs en las ÚLTIMAS 24 HORAS (todos los logs, leídos y no leídos)
        // Calcular timestamp de hace 24 horas
        long twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        com.google.firebase.Timestamp timestamp24hAgo = new com.google.firebase.Timestamp(twentyFourHoursAgo / 1000, 0);

        logs24hListener = db.collection("system_logs")
                .whereGreaterThan("timestamp", timestamp24hAgo)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w("InicioActivity", "Error al escuchar logs de las últimas 24 horas", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        int logs24hCount = queryDocumentSnapshots.size();
                        updateLogs24hCountUI(logs24hCount);
                        Log.d("InicioActivity", "Logs en las últimas 24 horas: " + logs24hCount);
                    }
                });
    }

    private void updateLogsUI(int count) {
        // Actualiza la tarjeta pequeña de logs (arriba) con logs sin leer
        if (logsCountText != null) {
            logsCountText.setText(String.valueOf(count));
        }
    }

    private void updateLogs24hCountUI(int count) {
        // Actualiza el conteo de logs en las últimas 24 horas en la sección "Logs del sistema"
        if (logsDetailText != null) {
            String detailText = count + " entradas en las últimas 24h";
            logsDetailText.setText(detailText);
        }
    }

    private void setupLogsCardClick() {
        // Configurar el click en la tarjeta pequeña de logs recientes
        findViewById(R.id.logsCard).setOnClickListener(v -> {
            // Abrir la pantalla de logs
            Intent intent = new Intent(InicioActivity.this, LogsActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    private void setupLogsDetailCardClick() {
        // Configurar el click en la tarjeta grande de logs en la sección "Actividad del Sistema"
        findViewById(R.id.logsDetailCard).setOnClickListener(v -> {
            // Abrir la pantalla de logs (detalle completo)
            Intent intent = new Intent(InicioActivity.this, LogsActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    private void setupSystemMetricsCardClick() {
        // Configurar el click en la tarjeta de "Rendimiento del sistema"
        findViewById(R.id.systemMetricsCard).setOnClickListener(v -> {
            // Abrir la pantalla de métricas detalladas del sistema
            Intent intent = new Intent(InicioActivity.this, SystemMetricsActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    private void setupSystemMetricsListener() {
        // Inicializar estado de Firebase como conectado
        updateFirebaseStatusUI(true);

        // Configurar tracking de métricas de Firebase en tiempo real
        setupFirebaseMetricsTracking();

        // Simular métricas iniciales
        updateDbOperationsUI(0);
        updateResponseTimeUI(0);
        updateDataUsageUI(0);
    }

    private void setupFirebaseMetricsTracking() {
        // Listener para trackear operaciones de base de datos
        // Usamos los listeners existentes para contar operaciones
        trackDatabaseOperation();

        // Configurar timer para actualizar métricas cada 30 segundos
        android.os.Handler handler = new android.os.Handler();
        Runnable metricsUpdater = new Runnable() {
            @Override
            public void run() {
                updateFirebaseMetrics();
                handler.postDelayed(this, 30000); // Actualizar cada 30 segundos
            }
        };
        handler.post(metricsUpdater);
    }

    private void trackDatabaseOperation() {
        // Incrementar contador de operaciones de DB
        dbOperationsCount++;

        // Medir tiempo de respuesta simulado (en un app real medirías el tiempo real)
        long startTime = System.currentTimeMillis();

        // Simular tiempo de respuesta entre 50-500ms
        long responseTime = 50 + (long)(Math.random() * 450);
        totalResponseTime += responseTime;
        responseTimeCount++;

        // Actualizar UI
        updateDbOperationsUI(dbOperationsCount);
        updateResponseTimeUI(totalResponseTime / responseTimeCount);
    }

    private void updateFirebaseMetrics() {
        // Simular uso de datos (en un app real obtendrías esto de Firebase Analytics)
        long dataUsedToday = calculateDataUsageToday();
        updateDataUsageUI(dataUsedToday);

        // Verificar estado de conexión a Firebase
        checkFirebaseConnection();
    }

    private long calculateDataUsageToday() {
        // Estimar uso de datos basado en operaciones realizadas
        // Cada operación promedio = ~2KB de datos
        return (dbOperationsCount * 2) / 1024; // Convertir a MB
    }

    private void checkFirebaseConnection() {
        // Test de conectividad simple realizando una consulta rápida
        db.collection("system_logs")
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                updateFirebaseStatusUI(true);
                trackDatabaseOperation(); // Contar esta operación
            })
            .addOnFailureListener(e -> {
                updateFirebaseStatusUI(false);
                Log.e("InicioActivity", "Error de conectividad Firebase", e);
            });
    }

    private void updateFirebaseStatusUI(boolean connected) {
        if (systemStatusText != null) {
            if (connected) {
                systemStatusText.setText("Firebase: Conectado");
                systemStatusText.setTextColor(getResources().getColor(R.color.md_theme_primary, null));
            } else {
                systemStatusText.setText("Firebase: Desconectado");
                systemStatusText.setTextColor(getResources().getColor(R.color.md_theme_error, null));
            }
        }
    }

    private void updateDbOperationsUI(int operations) {
        if (dbOperationsText != null) {
            dbOperationsText.setText(String.valueOf(operations));
        }
    }

    private void updateResponseTimeUI(long avgResponseTime) {
        if (responseTimeText != null) {
            responseTimeText.setText(avgResponseTime + "ms");
        }
    }

    private void updateDataUsageUI(long dataInMB) {
        if (dataUsageText != null) {
            if (dataInMB < 1) {
                dataUsageText.setText("< 1MB");
            } else {
                dataUsageText.setText(dataInMB + "MB");
            }
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
        if (recentLogsListener != null) {
            recentLogsListener.remove();
        }
        if (logs24hListener != null) {
            logs24hListener.remove();
        }
        if (systemMetricsListener != null) {
            systemMetricsListener.remove();
        }
    }
}
