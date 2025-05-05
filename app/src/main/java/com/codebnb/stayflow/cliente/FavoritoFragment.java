package com.codebnb.stayflow.cliente;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codebnb.stayflow.databinding.ClienteFragmentFavoritoBinding;


public class FavoritoFragment extends Fragment {

    ClienteFragmentFavoritoBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ClienteFragmentFavoritoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}