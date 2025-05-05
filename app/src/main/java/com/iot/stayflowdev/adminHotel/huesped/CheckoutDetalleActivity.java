package com.iot.stayflowdev.adminHotel.huesped;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iot.stayflowdev.R;

public class CheckoutDetalleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_detalle);

        // Recibir datos del intent
        Intent intent = getIntent();
        String nombre = intent.getStringExtra("nombre");
        String codigoReserva = intent.getStringExtra("codigoReserva");
        int imagenResId = intent.getIntExtra("imagenResId", R.drawable.img_guest_placeholder);
        double montoHospedaje = intent.getDoubleExtra("montoHospedaje", 0);
        String tarjeta = intent.getStringExtra("tarjeta");
        double danios = intent.getDoubleExtra("danios", 0);
        double total = intent.getDoubleExtra("total", 0);

        // Mostrar en las vistas
        ((TextView) findViewById(R.id.tvTituloCheckout)).setText("Checkout de " + nombre);
        ((TextView) findViewById(R.id.tvHospedajeMonto)).setText(String.format("S/. %.2f", montoHospedaje));
        ((TextView) findViewById(R.id.tvTarjetaNumero)).setText(tarjeta);
        ((TextView) findViewById(R.id.tvDaniosMonto)).setText(String.format("S/. %.2f", danios));
        ((TextView) findViewById(R.id.tvResumenHospedajeMonto)).setText(String.format("S/. %.2f", montoHospedaje));
        ((TextView) findViewById(R.id.tvResumenDaniosMonto)).setText(String.format("S/. %.2f", danios));
        ((TextView) findViewById(R.id.tvTotalMonto)).setText(String.format("S/. %.2f", total));
    }
}

