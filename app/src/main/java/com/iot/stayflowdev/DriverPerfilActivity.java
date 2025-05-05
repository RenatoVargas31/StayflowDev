package com.iot.stayflowdev;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DriverPerfilActivity extends BaseActivity {

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_driver_perfil;
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_perfil;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        // Encuentra la sección de tarjeta de crédito en el layout
        ConstraintLayout layoutTarjetaCredit = findViewById(R.id.layout_tarjetaCredit);
        if (layoutTarjetaCredit != null) {
            layoutTarjetaCredit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Crear un intent para iniciar DriverTarjetaCreditoActivity
                    Intent intent = new Intent(DriverPerfilActivity.this, DriverTarjetaCreditoActivity.class);
                    startActivity(intent);
                }
            });
        }

        ConstraintLayout layoutVehiculo = findViewById(R.id.layout_vehicle_model);
        if (layoutVehiculo != null) {
            layoutVehiculo.setOnClickListener(v -> {
                Intent intent = new Intent(DriverPerfilActivity.this, DriverVehiculoActivity.class);
                startActivity(intent);
            });
        }

        ConstraintLayout layoutCorreo = findViewById(R.id.layout_correo);
        if (layoutCorreo != null) {
            layoutCorreo.setOnClickListener(v -> {
                Intent intent = new Intent(DriverPerfilActivity.this, DriverCorreoActivity.class);
                startActivity(intent);
            });
        }







    }

}