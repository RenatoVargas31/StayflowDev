package com.codebnb.stayflow.driver.perfil;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import com.codebnb.stayflow.R;


public class DriverPerfilFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_driver_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //Buscamos el contenedor del vehiculo
        ConstraintLayout layoutVehicleModel = view.findViewById(R.id.layout_vehicle_model);

        //Buscamos el contenedor del correo
        ConstraintLayout layoutEmail = view.findViewById(R.id.layout_correo);

        //Buscamos el contenedor de la tarjeta de credito
        ConstraintLayout layoutCreditCard = view.findViewById(R.id.layout_tarjetaCredit);



        layoutVehicleModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), VehiculoActivity.class);
                startActivity(intent);
            }
        });

        layoutCreditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TarjetaCreditoActivity.class);
                startActivity(intent);
            }
        });

        layoutEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CorreoActivity.class);
                startActivity(intent);
            }
        });
    }
}