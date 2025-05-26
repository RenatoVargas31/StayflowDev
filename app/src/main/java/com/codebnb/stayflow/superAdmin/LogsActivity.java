package com.codebnb.stayflow.superAdmin;

import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import com.codebnb.stayflow.R;
import com.codebnb.stayflow.superAdmin.adapter.LogsAdapter;
import com.codebnb.stayflow.superAdmin.model.LogItem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogsActivity extends BaseSuperAdminActivity {

    private LogsAdapter logsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RecyclerView recyclerView = findViewById(R.id.logsRecyclerView);
        ChipGroup chipGroup = findViewById(R.id.chipGroup);

        // Set up filter chips
        Chip chipAll = findViewById(R.id.chipAll);
        Chip chipHotels = findViewById(R.id.chipHotels);
        Chip chipAccount = findViewById(R.id.chipAccount);
        Chip chipReservation = findViewById(R.id.chipReservation);

        // Sample log data with categories
        List<LogItem> logs = createSampleLogData();

        // Initialize the adapter
        logsAdapter = new LogsAdapter(logs);
        recyclerView.setAdapter(logsAdapter);

        // Set up chip listeners
        chipAll.setOnClickListener(v -> logsAdapter.filterByCategory(LogItem.CATEGORY_ALL));
        chipHotels.setOnClickListener(v -> logsAdapter.filterByCategory(LogItem.CATEGORY_HOTELS));
        chipAccount.setOnClickListener(v -> logsAdapter.filterByCategory(LogItem.CATEGORY_ACCOUNT));
        chipReservation.setOnClickListener(v -> logsAdapter.filterByCategory(LogItem.CATEGORY_RESERVATION));

        // Alternatively, you can use ChipGroup's selection listener
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                logsAdapter.filterByCategory(LogItem.CATEGORY_ALL);
            } else if (checkedId == R.id.chipHotels) {
                logsAdapter.filterByCategory(LogItem.CATEGORY_HOTELS);
            } else if (checkedId == R.id.chipAccount) {
                logsAdapter.filterByCategory(LogItem.CATEGORY_ACCOUNT);
            } else if (checkedId == R.id.chipReservation) {
                logsAdapter.filterByCategory(LogItem.CATEGORY_RESERVATION);
            }
        });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.superadmin_logs_fragment;
    }

    @Override
    protected int getBottomNavigationSelectedItem() {
        return R.id.nav_inicio; // O el ítem que corresponda
    }

    @Override
    protected String getToolbarTitle() {
        return "Logs del Sistema";
    }

    private List<LogItem> createSampleLogData() {
        return new ArrayList<>(Arrays.asList(
                new LogItem("Inicio de sesión exitoso", "10:15 AM",
                        "El usuario superadmin inició sesión correctamente.", LogItem.CATEGORY_ACCOUNT,
                        R.drawable.ic_perfil),

                new LogItem("Cambio de contraseña", "09:45 AM",
                        "El usuario cambió su contraseña.", LogItem.CATEGORY_ACCOUNT,
                        R.drawable.ic_perfil),

                new LogItem("Error en base de datos", "11:22 PM",
                        "No se pudo conectar a la base de datos temporalmente.", LogItem.CATEGORY_ALL,
                        R.drawable.ic_error),

                new LogItem("Nuevo hotel registrado", "09:30 PM",
                        "Se ha agregado el hotel 'Grand Plaza' al sistema.", LogItem.CATEGORY_HOTELS,
                        R.drawable.ic_hotel),

                new LogItem("Nuevo usuario creado", "08:30 PM",
                        "Se registró un nuevo administrador.", LogItem.CATEGORY_ACCOUNT,
                        R.drawable.ic_perfil)
        ));
    }
}