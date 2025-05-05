package com.iot.stayflowdev.adminHotel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.iot.stayflowdev.databinding.DialogAgregarServicioBinding;

public class AgregarServicioBottomSheet extends BottomSheetDialogFragment {

    private DialogAgregarServicioBinding binding;

    public interface OnServicioAgregadoListener {
        void onServicioAgregado(String nombre, String descripcion, String precio);
    }

    private OnServicioAgregadoListener listener;

    public static AgregarServicioBottomSheet newInstance(OnServicioAgregadoListener listener) {
        AgregarServicioBottomSheet fragment = new AgregarServicioBottomSheet();
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAgregarServicioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cerrar el diálogo con el ícono de la toolbar

        // Botón guardar servicio
        binding.btnSaveService.setOnClickListener(v -> {
            String nombre = binding.inputServiceName.getText().toString().trim();
            String descripcion = binding.inputDescription.getText().toString().trim();
            String precio = binding.inputPrice.getText().toString().trim();

            if (nombre.isEmpty() || descripcion.isEmpty() || precio.isEmpty()) {
                Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                // Devolver datos al activity
                if (listener != null) {
                    listener.onServicioAgregado(nombre, descripcion, precio);
                }
                dismiss();
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
