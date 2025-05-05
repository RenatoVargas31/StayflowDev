package com.codebnb.stayflow.cliente;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codebnb.stayflow.R;
import com.codebnb.stayflow.databinding.ClienteFragmentBusquedaHotelBinding;

public class BusquedaHotelFragment extends Fragment {

    ClienteFragmentBusquedaHotelBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ClienteFragmentBusquedaHotelBinding.inflate(inflater, container, false);
        //Configurar fragment de retorno
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_busquedaHotelFragment_to_nav_buscar);
        });
        return binding.getRoot();

    }
}