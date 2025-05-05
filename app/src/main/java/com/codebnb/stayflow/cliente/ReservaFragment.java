package com.codebnb.stayflow.cliente;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codebnb.stayflow.R;
import com.codebnb.stayflow.databinding.ClienteFragmentReservaBinding;

public class ReservaFragment extends Fragment {

    private ClienteFragmentReservaBinding binding;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ClienteFragmentReservaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}