package com.codebnb.stayflow.driver.mapa;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codebnb.stayflow.R;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;



public class OrigenFragment extends Fragment {
    private TextView tvHotelName;
    private TextView tvHotelDireccion;
    private TextView tvDistanciaPrincipal;
    private TextView tvTiempoLlegada;
    private MaterialButton btnIniciarViaje;
    private MaterialButton btnContactar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_origen, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        tvHotelName = view.findViewById(R.id.tv_hotel_name);
        tvHotelDireccion = view.findViewById(R.id.tv_hotel_direccion);
        tvDistanciaPrincipal = view.findViewById(R.id.tv_distancia_principal);
        tvTiempoLlegada = view.findViewById(R.id.tv_tiempo_llegada);
        btnIniciarViaje = view.findViewById(R.id.btn_iniciar_viaje);
        btnContactar = view.findViewById(R.id.btn_contactar);

        btnContactar.setOnClickListener(v -> {
            // Crear un Intent para abrir la actividad ChatActivity
            Intent intent = new Intent(getActivity(), ChatActivity.class);

            startActivity(intent);
        });
    }
}