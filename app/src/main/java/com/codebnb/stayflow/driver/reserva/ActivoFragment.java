package com.codebnb.stayflow.driver.reserva;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.codebnb.stayflow.R;
import com.google.android.material.card.MaterialCardView;


public class ActivoFragment extends Fragment {

    // Vistas para las tarjetas
    private MaterialCardView cardReserva1, cardReserva2, cardReserva3, cardReserva4;

    // Vistas para el texto e imágenes dentro de las tarjetas
    private TextView tvNombre1, tvRuta1, tvFechaHora1;
    private TextView tvNombre2, tvRuta2, tvFechaHora2;
    private TextView tvNombre3, tvRuta3, tvFechaHora3;
    private TextView tvNombre4, tvRuta4, tvFechaHora4;
    private Button btnActivo1;

    // Vista para el estado vacío
    private LinearLayout emptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        return inflater.inflate(R.layout.fragment_activo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar referencias a las tarjetas
        cardReserva1 = view.findViewById(R.id.card_reserva1);
        cardReserva2 = view.findViewById(R.id.card_reserva2);
        cardReserva3 = view.findViewById(R.id.card_reserva3);
        cardReserva4 = view.findViewById(R.id.card_reserva4);

        // Inicializar referencias a los botoness de las tarjetas


        //Inicializar
        ConstraintLayout solicitud1 = view.findViewById(R.id.solicitud1);

        // Inicializar referencias a los textos de la tarjeta 1
        tvNombre1 = view.findViewById(R.id.tv_hotel1_nombre);
        tvRuta1 = view.findViewById(R.id.tv_pasajero1);
        tvFechaHora1 = view.findViewById(R.id.tv_hora1);


        // Vista de estado vacío
        emptyView = view.findViewById(R.id.empty_view);

        // Ver detalles de reserva


        solicitud1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a DetallesReservaActivity
                Intent intent = new Intent(getActivity(), DetallesReservaActivity.class);
                startActivity(intent);
            }
        });

    }
}