package com.iot.stayflowdev.Driver.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iot.stayflowdev.R;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;

public class DriverPruebaMapa extends AppCompatActivity {

    private MapView mapView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtener referencia al MapView
        mapView = findViewById(R.id.mapView);

        // Configurar la cámara inicial (Lima, Perú)
        CameraOptions cameraOptions = new CameraOptions.Builder()
                .center(Point.fromLngLat(-77.0428, -12.0464))
                .pitch(0.0)
                .zoom(10.0)
                .bearing(0.0)
                .build();

        mapView.getMapboxMap().setCamera(cameraOptions);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}