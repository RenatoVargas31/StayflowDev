package com.codebnb.stayflow.cliente;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codebnb.stayflow.R;
import com.codebnb.stayflow.databinding.ClienteFragmentBuscarBinding;

public class BuscarFragment extends Fragment {

    private ClienteFragmentBuscarBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ClienteFragmentBuscarBinding.inflate(inflater, container, false);
        //Configurar fragment de destino

        binding.button.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_buscar_to_busquedaHotelFragment);
        });
        return binding.getRoot();
    }
}