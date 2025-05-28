/*  package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iot.stayflowdev.R;

public class BaseActivity extends AppCompatActivity {

    private boolean isUpdatingNavigation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (isUpdatingNavigation) return false;

            int itemId = item.getItemId();
            Intent intent = null;

            if (itemId == R.id.menu_inicio && !(this instanceof AdminInicioActivity)) {
                intent = new Intent(this, AdminInicioActivity.class);
            } else if (itemId == R.id.menu_reportes && !(this instanceof ReportesAdminActivity)) {
                intent = new Intent(this, ReportesAdminActivity.class);
            } else if (itemId == R.id.menu_huesped && !(this instanceof HuespedAdminActivity)) {
                intent = new Intent(this, HuespedAdminActivity.class);
            } else if (itemId == R.id.menu_mensajeria && !(this instanceof MensajeriaAdminActivity)) {
                intent = new Intent(this, MensajeriaAdminActivity.class);
            } else if (itemId == R.id.menu_perfil && !(this instanceof PerfilAdminActivity)) {
                intent = new Intent(this, PerfilAdminActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(getSelectedMenuItemId());
        }
    }

    protected int getSelectedMenuItemId() {
        return R.id.menu_inicio;  // Por defecto Inicio
    }

    @Override
    public void setContentView(int layoutResID) {
        FrameLayout container = findViewById(R.id.container);
        if (container != null) {
            LayoutInflater.from(this).inflate(layoutResID, container, true);
        } else {
            super.setContentView(layoutResID);
        }
    }
} */
