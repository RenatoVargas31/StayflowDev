package com.codebnb.stayflow.superAdmin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.codebnb.stayflow.R;
import com.codebnb.stayflow.SuperAdminActivity;
import com.google.android.material.button.MaterialButton;

public class InicioFragment extends Fragment {
    public InicioFragment() {
        super(R.layout.fragment_inicio_superadmin);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MaterialButton btnLogs = view.findViewById(R.id.goToLogsButton);
        btnLogs.setOnClickListener(v -> {
            // Usamos el método de SuperAdminActivity para abrir un fragmento secundario
            ((SuperAdminActivity) requireActivity()).openSecondaryFragment(new LogsFragment(), "Logs");
        });
    }
}