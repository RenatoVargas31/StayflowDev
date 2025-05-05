package com.codebnb.stayflow.driver.mapa;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codebnb.stayflow.R;


public class DriverMapaFragment extends Fragment {
    // Fragmentos que se mostrarán en el contenedor
    private OrigenFragment origenFragment;
    private DestinoFragment escanerFragment;
    private ResumenFragment resumenFragment;
    // Estado actual del viaje (podría ser un enum en una implementación real)
    private int estadoViaje = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_driver_mapa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Inicializar fragmentos
        origenFragment = new OrigenFragment();
        escanerFragment = new DestinoFragment();
        resumenFragment = new ResumenFragment();

        mostrarFragmentoActual();
    }

    private void mostrarFragmentoActual() {
        Fragment fragmentToShow;

        switch (estadoViaje) {
            case 0:
                fragmentToShow = origenFragment;
                break;
            case 1:
                fragmentToShow = escanerFragment;
                break;
            case 2:
                fragmentToShow = resumenFragment;
                break;
            default:
                fragmentToShow = origenFragment;
                break;
        }

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragmentToShow)
                .commit();
    }
}