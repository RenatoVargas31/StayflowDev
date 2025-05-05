package com.codebnb.stayflow.superAdmin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.codebnb.stayflow.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserDetailFragment extends Fragment {
    private String userName;
    private String userRole;
    private String userRoleDescription;
    private boolean userEnabled;

    // Crear un factory method para instanciar el fragmento con los datos del usuario
    public static UserDetailFragment newInstance(String name, String role, String roleDescription, boolean enabled) {
        UserDetailFragment fragment = new UserDetailFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("role", role);
        args.putString("roleDescription", roleDescription);
        args.putBoolean("enabled", enabled);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userName = getArguments().getString("name", "");
            userRole = getArguments().getString("role", "");
            userRoleDescription = getArguments().getString("roleDescription", "");
            userEnabled = getArguments().getBoolean("enabled", false);
        }

        // Ocultar la barra de navegación inferior cuando se muestra este fragmento
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_detail, container, false);

        // Inicializar vistas
        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        TextView textViewUserName = view.findViewById(R.id.textViewUserName);
        TextView textViewUserRole = view.findViewById(R.id.textViewUserRole);
        TextView textViewDNI = view.findViewById(R.id.textViewDNI);
        TextView textViewEmail = view.findViewById(R.id.textViewEmail);
        TextView textViewPhone = view.findViewById(R.id.textViewPhone);
        TextView textViewAccountStatus = view.findViewById(R.id.textViewAccountStatus);
        SwitchCompat switchAccountStatus = view.findViewById(R.id.switchAccountStatus);
        ImageView imageViewProfile = view.findViewById(R.id.imageViewProfile);

        // Configurar navegación
        toolbar.setNavigationOnClickListener(v -> {
            // Volver al fragmento anterior
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();

                // Mostrar la barra de navegación inferior al regresar
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                if (bottomNav != null) {
                    bottomNav.setVisibility(View.VISIBLE);
                }
            }
        });

        // Configurar título según el rol
        toolbar.setTitle("Usuario - " + userRoleDescription);

        // Configurar datos del usuario
        textViewUserName.setText(userName);
        textViewUserRole.setText(userRoleDescription);
        textViewAccountStatus.setText(userEnabled ? "Habilitado" : "Deshabilitado");
        switchAccountStatus.setChecked(userEnabled);

        // Configurar datos dummy
        // En una aplicación real, estos datos vendrían de la base de datos
        textViewDNI.setText("78965432");
        textViewEmail.setText(userName.toLowerCase().replace(" ", "") + "@gmail.com");
        textViewPhone.setText("987654321");

        // Listener para el switch de estado
        switchAccountStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            textViewAccountStatus.setText(isChecked ? "Habilitado" : "Deshabilitado");
            // Aquí actualizarías el estado en la base de datos
            Toast.makeText(getContext(),
                    "Estado de " + userName + " actualizado: " + (isChecked ? "Habilitado" : "Deshabilitado"),
                    Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Asegurarte de mostrar la barra de navegación inferior al destruir la vista
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }

    public String getTitle() {
        return "Detalle de Usuario";
    }
}