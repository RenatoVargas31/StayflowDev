package com.codebnb.stayflow.driver.reserva;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.codebnb.stayflow.R;

public class CanceladoFragment extends Fragment {

    private ImageView emptyImage;
    private TextView emptyText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cancelado, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        emptyImage = view.findViewById(R.id.imageView2);
        emptyText = view.findViewById(R.id.textView);

        // Aquí podrías comprobar si hay reservas canceladas
        // Si hay reservas, ocultar estas vistas y mostrar un RecyclerView
        // Por ahora, solo mostramos el mensaje de "No hay reservas canceladas"

    }
}