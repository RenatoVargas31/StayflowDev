package com.iot.stayflowdev;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DriverMapaActivity extends BaseActivity {

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_driver_mapa;
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_mapa;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_mapa);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
    }
}