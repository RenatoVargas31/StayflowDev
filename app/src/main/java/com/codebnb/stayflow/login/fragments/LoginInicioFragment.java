package com.codebnb.stayflow.login.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codebnb.stayflow.R;
import com.google.android.material.button.MaterialButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginInicioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginInicioFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LoginInicioFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginInicioFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginInicioFragment newInstance(String param1, String param2) {
        LoginInicioFragment fragment = new LoginInicioFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_inicio, container, false);

        MaterialButton btnIniciarSesion = view.findViewById(R.id.btn_iniciar_sesion);
        MaterialButton btnRegistrar = view.findViewById(R.id.btn_registrar);



        btnIniciarSesion.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.login_fragment_container, new LoginSesionFragment());
            transaction.addToBackStack(null); // Permite regresar con el botón "atrás"
            transaction.commit();
        });

        btnRegistrar.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.login_fragment_container, new LoginRegisterFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });


        return view;
    }
}