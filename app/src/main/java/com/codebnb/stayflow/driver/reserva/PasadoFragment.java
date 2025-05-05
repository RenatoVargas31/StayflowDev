package com.codebnb.stayflow.driver.reserva;

import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codebnb.stayflow.R;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;



public class PasadoFragment extends Fragment {
    // Vistas para las tarjetas
    private MaterialCardView cardReserva1;

    // Vistas para el texto dentro de la tarjeta
    private TextView tvNombre1, tvRuta1;
    private Button btnCompletado1;

    // Vista para el estado vacío
    private LinearLayout emptyView;
    private View scrollView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pasado, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar referencias a las vistas
        scrollView = view.findViewById(R.id.scroll_view);
        emptyView = view.findViewById(R.id.empty_view);

        // Inicializar referencias a la tarjeta
        cardReserva1 = view.findViewById(R.id.card_reserva1);

        // Inicializar referencias a los textos de la tarjeta
        tvNombre1 = view.findViewById(R.id.tv_hotel1_nombre);
        tvRuta1 = view.findViewById(R.id.tv_pasajero1);

        ConstraintLayout pasado1 = view.findViewById(R.id.pasado1);

        pasado1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a DetallesReservaPasadoActivity
                Intent intent = new Intent(getActivity(), DetallesReservaPasadoActivity.class);
                startActivity(intent);
            }
        });
    }
}