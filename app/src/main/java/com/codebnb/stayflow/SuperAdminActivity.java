package com.codebnb.stayflow;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.codebnb.stayflow.superAdmin.*;

public class SuperAdminActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private MaterialToolbar toolbar;
    private boolean isRootFragment = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superadmin_activity);

        toolbar = findViewById(R.id.topAppBar);
        setupToolbar();

        bottomNav = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();

        // Configurar el callback para el botón de retroceso
        setupBackPressCallback();

        // Configurar listener para cambios en el stack de fragmentos
        getSupportFragmentManager().addOnBackStackChangedListener(this::onBackStackChanged);

        // Cargar fragmento inicial y establecer el título
        toolbar.setTitle("Inicio");
        bottomNav.setSelectedItemId(R.id.nav_inicio);
    }

    private void setupToolbar() {
        // Configurar el listener para el botón de navegación (back)
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        // Ocultar inicialmente el icono de navegación en la pantalla principal
        toolbar.setNavigationIcon(null);

        // Configurar listener para los items del menú
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logs) {
                // Cargar fragmento de logs
                loadFragment(new LogsFragment(), true); // Añadimos al backstack
                return true;
            }
            return false;
        });
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String sectionTitle = "";

            int itemId = item.getItemId();
            if (itemId == R.id.nav_inicio) {
                selectedFragment = new InicioFragment();
                sectionTitle = "Inicio";
            } else if (itemId == R.id.nav_gestion) {
                selectedFragment = new GestionFragment();
                sectionTitle = "Gestión";
            } else if (itemId == R.id.nav_reportes) {
                selectedFragment = new ReportesFragment();
                sectionTitle = "Reportes";
            } else if (itemId == R.id.nav_perfil) {
                selectedFragment = new PerfilFragment();
                sectionTitle = "Perfil";
            }

            // Actualizar el título de la barra superior
            toolbar.setTitle(sectionTitle);

            // Para fragmentos de navegación principal, no añadimos al backstack
            return loadFragment(selectedFragment, false);
        });
    }

    private void setupBackPressCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    // Si hay fragmentos en el backstack, volvemos atrás
                    getSupportFragmentManager().popBackStack();
                } else {
                    // Si estamos en la raíz, dejamos que el sistema maneje el botón de retroceso
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });
    }

    private void onBackStackChanged() {
        // Verifica si estamos en un fragmento raíz o en un fragmento secundario
        isRootFragment = getSupportFragmentManager().getBackStackEntryCount() == 0;

        // Actualizar la visibilidad del icono de navegación
        if (isRootFragment) {
            toolbar.setNavigationIcon(null); // Ocultar icono de retroceso en fragmentos raíz

            // Cuando volvemos a un fragmento raíz, restauramos el título según el ítem seleccionado
            int selectedItemId = bottomNav.getSelectedItemId();
            if (selectedItemId == R.id.nav_inicio) {
                toolbar.setTitle("Inicio");
            } else if (selectedItemId == R.id.nav_gestion) {
                toolbar.setTitle("Gestión");
            } else if (selectedItemId == R.id.nav_reportes) {
                toolbar.setTitle("Reportes");
            } else if (selectedItemId == R.id.nav_perfil) {
                toolbar.setTitle("Perfil");
            }
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back); // Mostrar icono de retroceso

            // Buscar el título del fragmento actual del backstack
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                // Obtener el fragmento actual
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                // Determinar el título basado en el tipo de fragmento
                if (currentFragment instanceof LogsFragment) {
                    toolbar.setTitle("Logs");
                } else if (currentFragment instanceof UserDetailFragment) {
                    toolbar.setTitle("Detalle de Usuario");
                } else if (currentFragment instanceof FilterReportFragment) {
                    // Extraer el nombre del hotel si está disponible
                    if (currentFragment.getArguments() != null) {
                        String hotelName = currentFragment.getArguments().getString("HOTEL_NAME", "Filtro de Reportes");
                        toolbar.setTitle(hotelName);
                    } else {
                        toolbar.setTitle("Filtro de Reportes");
                    }
                }
                // Añadir más casos según los fragmentos secundarios que tengas
            }
        }

        // Gestionar visibilidad del bottom navigation según el fragmento
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            hideBottomNavigation();
        } else {
            showBottomNavigation();
        }
    }

    public boolean loadFragment(Fragment fragment, boolean addToBackStack) {
        if (fragment != null) {
            androidx.fragment.app.FragmentTransaction transaction =
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment);

            if (addToBackStack) {
                transaction.addToBackStack(null);
            }

            transaction.commit();
            return true;
        }
        return false;
    }

    public void hideBottomNavigation() {
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
    }

    public void showBottomNavigation() {
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }

    // Método para abrir un fragmento secundario
    public void openSecondaryFragment(Fragment fragment, String title) {
        // Establecer el título de la pantalla secundaria
        toolbar.setTitle(title);
        loadFragment(fragment, true); // Añadimos al backstack
    }

    // Sobrecarga para usar el título del fragmento si implementa la interfaz TitledFragment
    public void openSecondaryFragment(Fragment fragment) {
        String title = "Detalle";

        // Si el fragmento tiene un método getTitle, úsalo
        if (fragment instanceof LogsFragment) {
            title = "Logs";
        } else if (fragment instanceof UserDetailFragment) {
            title = "Detalle de Usuario";
        } else if (fragment instanceof FilterReportFragment) {
            if (fragment.getArguments() != null) {
                title = fragment.getArguments().getString("HOTEL_NAME", "Filtro de Reportes");
            } else {
                title = "Filtro de Reportes";
            }
        }

        toolbar.setTitle(title);
        loadFragment(fragment, true); // Añadimos al backstack
    }
}