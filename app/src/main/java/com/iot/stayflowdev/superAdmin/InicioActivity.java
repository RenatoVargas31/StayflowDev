package com.iot.stayflowdev.superAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.iot.stayflowdev.R;
import com.google.android.material.button.MaterialButton;

public class InicioActivity extends BaseSuperAdminActivity {

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

        // Configurar listeners específicos de esta pantalla
        setupContentListeners();
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
}