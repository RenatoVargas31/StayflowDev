package com.codebnb.stayflow.superAdmin;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.codebnb.stayflow.R;
import com.codebnb.stayflow.superAdmin.adapter.LogsAdapter;
import com.codebnb.stayflow.superAdmin.model.LogItem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogsFragment extends Fragment {

    private LogsAdapter logsAdapter;

    public LogsFragment() {
        super(R.layout.superadmin_logs_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.logsRecyclerView);
        ChipGroup chipGroup = view.findViewById(R.id.chipGroup);

        // Set up filter chips
        Chip chipAll = view.findViewById(R.id.chipAll);
        Chip chipHotels = view.findViewById(R.id.chipHotels);
        Chip chipAccount = view.findViewById(R.id.chipAccount);
        Chip chipReservation = view.findViewById(R.id.chipReservation);

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
                        R.drawable.ic_perfil),

                new LogItem("Intento fallido de inicio de sesión", "07:50 PM",
                        "Se detectó un intento con credenciales inválidas.", LogItem.CATEGORY_ACCOUNT,
                        R.drawable.ic_warning),

                new LogItem("Reserva cancelada", "04:15 PM",
                        "La reserva #1234 ha sido cancelada por el usuario.", LogItem.CATEGORY_RESERVATION,
                        R.drawable.ic_calendar),

                new LogItem("Nueva reserva", "01:22 PM",
                        "Se ha creado la reserva #5678 para Hotel Miramar.", LogItem.CATEGORY_RESERVATION,
                        R.drawable.ic_calendar),

                new LogItem("Actualización de tarifas", "11:05 AM",
                        "Se actualizaron las tarifas para 3 hoteles.", LogItem.CATEGORY_HOTELS,
                        R.drawable.ic_hotel)
        ));
    }
    public String getTitle() {
        return "Logs";
    }
}