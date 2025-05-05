package com.codebnb.stayflow;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.NavController;

import androidx.navigation.fragment.NavHostFragment;

import androidx.navigation.ui.NavigationUI;

import com.codebnb.stayflow.databinding.ClienteActivityBinding;

public class ClienteActivity extends AppCompatActivity {
    ClienteActivityBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ClienteActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        NavController navController = navHostFragment.getNavController();
        //Ocultar la BottomBar para fragments específicos
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                    if (destination.getId() == R.id.busquedaHotelFragment) {
                        binding.bottomNavigation.setVisibility(View.GONE);
                    } else {
                        binding.bottomNavigation.setVisibility(View.VISIBLE);
                    }
                });
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
    }
}