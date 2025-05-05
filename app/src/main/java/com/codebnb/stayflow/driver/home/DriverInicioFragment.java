package com.codebnb.stayflow.driver.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codebnb.stayflow.R;
import com.google.android.material.card.MaterialCardView;

public class DriverInicioFragment extends Fragment {

    private MaterialCardView cardHotel1;
    private MaterialCardView cardHotel2;
    private MaterialCardView cardHotel3;
    private MaterialCardView cardHotel4;
    private MaterialCardView cardHotel5;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_driver_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cardHotel1 = view.findViewById(R.id.card_hotel_1);
        cardHotel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a DetallesHotelActivity
                Intent intent = new Intent(getActivity(), HotelActivity.class);
                startActivity(intent);
            }
        });

    }

}