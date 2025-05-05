package com.iot.stayflowdev;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

public class DriverReservaActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_driver_reserva;
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_reservas;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
}