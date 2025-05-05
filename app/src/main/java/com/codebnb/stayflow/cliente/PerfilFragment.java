package com.codebnb.stayflow.cliente;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codebnb.stayflow.databinding.ClienteFragmentPerfilBinding;

public class PerfilFragment extends Fragment {

    private ClienteFragmentPerfilBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ClienteFragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}