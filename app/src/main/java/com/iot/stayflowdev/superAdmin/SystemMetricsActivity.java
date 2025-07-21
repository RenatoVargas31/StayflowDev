package com.iot.stayflowdev.superAdmin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.iot.stayflowdev.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SystemMetricsActivity extends BaseSuperAdminActivity {

    private static final String TAG = "SystemMetricsActivity";

    private FirebaseFirestore db;
    private ListenerRegistration systemMetricsListener;
    private ListenerRegistration hotelsListener;
    private ListenerRegistration usersListener;
    private ListenerRegistration logsListener;

    // TextViews para métricas principales
    private TextView firebaseStatusText;
    private TextView dbOperationsText;
    private TextView responseTimeText;
    private TextView dataUsageText;
    private TextView lastUpdateText;

    // TextViews para métricas detalladas
    private TextView totalHotelsText;
    private TextView activeHotelsText;
    private TextView totalUsersText;
    private TextView connectedUsersText;
    private TextView totalLogsText;
    private TextView unreadLogsText;
    private TextView logs24hText;

    // TextViews para estadísticas de rendimiento
    private TextView avgResponseTimeText;
    private TextView maxResponseTimeText;
    private TextView minResponseTimeText;
    private TextView successRateText;
    private TextView errorCountText;

    // Variables para tracking de métricas
    private int dbOperationsCount = 0;
    private long totalResponseTime = 0;
    private int responseTimeCount = 0;
    private long maxResponseTime = 0;
    private long minResponseTime = Long.MAX_VALUE;
    private int successfulOperations = 0;
    private int failedOperations = 0;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.superadmin_base_superadmin_activity;
    }

    @Override
    protected int getBottomNavigationSelectedItem() {
        return R.id.nav_reportes; // Usar reportes como categoría más cercana
    }

    @Override
    protected String getToolbarTitle() {
        return "Métricas del Sistema";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar el contenido específico de esta activity
        LayoutInflater.from(this).inflate(R.layout.superadmin_system_metrics,
                findViewById(R.id.content_frame), true);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Inicializar referencias a vistas
        initializeViews();

        // Configurar listeners para obtener métricas
        setupMetricsListeners();

        // Configurar actualización automática
        setupAutoUpdate();
    }

    private void initializeViews() {
        // Métricas principales
        firebaseStatusText = findViewById(R.id.firebaseStatusText);
        dbOperationsText = findViewById(R.id.dbOperationsText);
        responseTimeText = findViewById(R.id.responseTimeText);
        dataUsageText = findViewById(R.id.dataUsageText);
        lastUpdateText = findViewById(R.id.lastUpdateText);

        // Métricas detalladas
        totalHotelsText = findViewById(R.id.totalHotelsText);
        activeHotelsText = findViewById(R.id.activeHotelsText);
        totalUsersText = findViewById(R.id.totalUsersText);
        connectedUsersText = findViewById(R.id.connectedUsersText);
        totalLogsText = findViewById(R.id.totalLogsText);
        unreadLogsText = findViewById(R.id.unreadLogsText);
        logs24hText = findViewById(R.id.logs24hText);

        // Estadísticas de rendimiento
        avgResponseTimeText = findViewById(R.id.avgResponseTimeText);
        maxResponseTimeText = findViewById(R.id.maxResponseTimeText);
        minResponseTimeText = findViewById(R.id.minResponseTimeText);
        successRateText = findViewById(R.id.successRateText);
        errorCountText = findViewById(R.id.errorCountText);
    }

    private void setupMetricsListeners() {
        // Listener para hoteles
        hotelsListener = db.collection("hoteles")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Error al escuchar hoteles", e);
                        trackFailedOperation();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        int totalHotels = queryDocumentSnapshots.size();
                        int activeHotels = 0;

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Boolean estado = doc.getBoolean("estado");
                            if (estado != null && estado) {
                                activeHotels++;
                            }
                        }

                        updateHotelsMetrics(totalHotels, activeHotels);
                        trackSuccessfulOperation();
                    }
                });

        // Listener para usuarios
        usersListener = db.collection("usuarios")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Error al escuchar usuarios", e);
                        trackFailedOperation();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        int totalUsers = queryDocumentSnapshots.size();
                        int connectedUsers = 0;

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Boolean conectado = doc.getBoolean("conectado");
                            Boolean estado = doc.getBoolean("estado");
                            if (conectado != null && conectado && estado != null && estado) {
                                connectedUsers++;
                            }
                        }

                        updateUsersMetrics(totalUsers, connectedUsers);
                        trackSuccessfulOperation();
                    }
                });

        // Listener para logs
        logsListener = db.collection("system_logs")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Error al escuchar logs", e);
                        trackFailedOperation();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        int totalLogs = queryDocumentSnapshots.size();
                        int unreadLogs = 0;
                        int logs24h = 0;

                        long twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Boolean leido = doc.getBoolean("leido");
                            if (leido != null && !leido) {
                                unreadLogs++;
                            }

                            com.google.firebase.Timestamp timestamp = doc.getTimestamp("timestamp");
                            if (timestamp != null && timestamp.toDate().getTime() > twentyFourHoursAgo) {
                                logs24h++;
                            }
                        }

                        updateLogsMetrics(totalLogs, unreadLogs, logs24h);
                        trackSuccessfulOperation();
                    }
                });
    }

    private void setupAutoUpdate() {
        android.os.Handler handler = new android.os.Handler();
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateLastUpdateTime();
                checkFirebaseConnection();
                updatePerformanceMetrics();
                handler.postDelayed(this, 10000); // Actualizar cada 10 segundos
            }
        };
        handler.post(updateRunnable);
    }

    private void trackSuccessfulOperation() {
        dbOperationsCount++;
        successfulOperations++;

        // Simular tiempo de respuesta
        long responseTime = 50 + (long)(Math.random() * 450);
        totalResponseTime += responseTime;
        responseTimeCount++;

        if (responseTime > maxResponseTime) {
            maxResponseTime = responseTime;
        }
        if (responseTime < minResponseTime) {
            minResponseTime = responseTime;
        }

        updateOperationsMetrics();
    }

    private void trackFailedOperation() {
        dbOperationsCount++;
        failedOperations++;
        updateOperationsMetrics();
    }

    private void updateHotelsMetrics(int total, int active) {
        if (totalHotelsText != null) {
            totalHotelsText.setText(String.valueOf(total));
        }
        if (activeHotelsText != null) {
            activeHotelsText.setText(String.valueOf(active));
        }
    }

    private void updateUsersMetrics(int total, int connected) {
        if (totalUsersText != null) {
            totalUsersText.setText(String.valueOf(total));
        }
        if (connectedUsersText != null) {
            connectedUsersText.setText(String.valueOf(connected));
        }
    }

    private void updateLogsMetrics(int total, int unread, int logs24h) {
        if (totalLogsText != null) {
            totalLogsText.setText(String.valueOf(total));
        }
        if (unreadLogsText != null) {
            unreadLogsText.setText(String.valueOf(unread));
        }
        if (logs24hText != null) {
            logs24hText.setText(String.valueOf(logs24h));
        }
    }

    private void updateOperationsMetrics() {
        if (dbOperationsText != null) {
            dbOperationsText.setText(String.valueOf(dbOperationsCount));
        }

        if (responseTimeCount > 0 && responseTimeText != null) {
            long avgResponseTime = totalResponseTime / responseTimeCount;
            responseTimeText.setText(avgResponseTime + "ms");
        }

        // Estimar uso de datos
        long dataUsage = (dbOperationsCount * 2) / 1024; // MB
        if (dataUsageText != null) {
            if (dataUsage < 1) {
                dataUsageText.setText("< 1MB");
            } else {
                dataUsageText.setText(dataUsage + "MB");
            }
        }
    }

    private void updatePerformanceMetrics() {
        if (responseTimeCount > 0) {
            if (avgResponseTimeText != null) {
                long avgTime = totalResponseTime / responseTimeCount;
                avgResponseTimeText.setText(avgTime + "ms");
            }

            if (maxResponseTimeText != null) {
                maxResponseTimeText.setText(maxResponseTime + "ms");
            }

            if (minResponseTimeText != null && minResponseTime != Long.MAX_VALUE) {
                minResponseTimeText.setText(minResponseTime + "ms");
            }
        }

        if (successRateText != null && dbOperationsCount > 0) {
            double successRate = (double) successfulOperations / dbOperationsCount * 100;
            successRateText.setText(String.format("%.1f%%", successRate));
        }

        if (errorCountText != null) {
            errorCountText.setText(String.valueOf(failedOperations));
        }
    }

    private void updateLastUpdateTime() {
        if (lastUpdateText != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String currentTime = dateFormat.format(new Date());
            lastUpdateText.setText("Última actualización: " + currentTime);
        }
    }

    private void checkFirebaseConnection() {
        db.collection("system_logs")
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                updateFirebaseStatus(true);
                trackSuccessfulOperation();
            })
            .addOnFailureListener(e -> {
                updateFirebaseStatus(false);
                trackFailedOperation();
                Log.e(TAG, "Error de conectividad Firebase", e);
            });
    }

    private void updateFirebaseStatus(boolean connected) {
        if (firebaseStatusText != null) {
            if (connected) {
                firebaseStatusText.setText("🟢 Conectado");
                firebaseStatusText.setTextColor(getResources().getColor(R.color.md_theme_primary, null));
            } else {
                firebaseStatusText.setText("🔴 Desconectado");
                firebaseStatusText.setTextColor(getResources().getColor(R.color.md_theme_error, null));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar listeners
        if (hotelsListener != null) {
            hotelsListener.remove();
        }
        if (usersListener != null) {
            usersListener.remove();
        }
        if (logsListener != null) {
            logsListener.remove();
        }
        if (systemMetricsListener != null) {
            systemMetricsListener.remove();
        }
    }
}

