package com.codebnb.stayflow.login.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codebnb.stayflow.R;
import com.codebnb.stayflow.driver.home.DriverInicioFragment;
import com.google.android.material.button.MaterialButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginCargarFotoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginCargarFotoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LoginCargarFotoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginCargarFotoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginCargarFotoFragment newInstance(String param1, String param2) {
        LoginCargarFotoFragment fragment = new LoginCargarFotoFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_cargar_foto, container, false);

        MaterialButton btnOmitirFinalizar = view.findViewById(R.id.btn_omitir_finalizar);

        btnOmitirFinalizar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DriverInicioFragment.class);
            startActivity(intent);
            requireActivity().finish(); // Para cerrar la pantalla de login si no deseas volver atrás
        });

        return view;
    }
}