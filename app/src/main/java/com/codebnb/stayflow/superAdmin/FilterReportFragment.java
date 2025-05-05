package com.codebnb.stayflow.superAdmin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.codebnb.stayflow.R;
import com.codebnb.stayflow.SuperAdminActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FilterReportFragment extends Fragment {

    private static final String ARG_HOTEL_NAME = "hotel_name";
    private String hotelName;

    public static FilterReportFragment newInstance(String hotelName) {
        FilterReportFragment fragment = new FilterReportFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HOTEL_NAME, hotelName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            hotelName = getArguments().getString(ARG_HOTEL_NAME);
        }

        if (hotelName == null) {
            // Si no se ha pasado un hotel válido, volver atrás
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.superadmin_filtrado_reporte_hotel, container, false);

        // Configurar los clicks para cada opción de filtro
        ImageButton buttonBack = view.findViewById(R.id.buttonBack);
        CardView cardRangoFechas = view.findViewById(R.id.cardRangoFechas);
        CardView cardUltimaSemana = view.findViewById(R.id.cardUltimaSemana);
        CardView cardUltimoMes = view.findViewById(R.id.cardUltimoMes);
        CardView cardUltimosSeisMeses = view.findViewById(R.id.cardUltimosSeisMeses);
        CardView cardUltimoAnio = view.findViewById(R.id.cardUltimoAnio);

        buttonBack.setOnClickListener(v -> goBack());

        cardRangoFechas.setOnClickListener(v -> showDateRangePicker());

        cardUltimaSemana.setOnClickListener(v -> {
            // Calcular rango de última semana
            Calendar calendar = Calendar.getInstance();
            Date endDate = calendar.getTime(); // Hoy
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            Date startDate = calendar.getTime();

            navigateToReport("Última semana", startDate, endDate);
        });

        cardUltimoMes.setOnClickListener(v -> {
            // Calcular rango del último mes
            Calendar calendar = Calendar.getInstance();
            Date endDate = calendar.getTime(); // Hoy
            calendar.add(Calendar.MONTH, -1);
            Date startDate = calendar.getTime();

            navigateToReport("Último mes", startDate, endDate);
        });

        cardUltimosSeisMeses.setOnClickListener(v -> {
            // Calcular rango de últimos 6 meses
            Calendar calendar = Calendar.getInstance();
            Date endDate = calendar.getTime(); // Hoy
            calendar.add(Calendar.MONTH, -6);
            Date startDate = calendar.getTime();

            navigateToReport("Últimos seis meses", startDate, endDate);
        });

        cardUltimoAnio.setOnClickListener(v -> {
            // Calcular rango del último año
            Calendar calendar = Calendar.getInstance();
            Date endDate = calendar.getTime(); // Hoy
            calendar.add(Calendar.YEAR, -1);
            Date startDate = calendar.getTime();

            navigateToReport("Último año", startDate, endDate);
        });

        return view;
    }

    private void goBack() {
        // Mostrar el BottomNavigationView al volver
        if (getActivity() instanceof SuperAdminActivity) {
            ((SuperAdminActivity) getActivity()).showBottomNavigation();
        }
        getActivity().getSupportFragmentManager().popBackStack();
    }

    private void showDateRangePicker() {
        DateRangeDialogFragment dateRangeDialog = new DateRangeDialogFragment();
        dateRangeDialog.setDateRangeListener(new DateRangeDialogFragment.DateRangeListener() {
            @Override
            public void onDateRangeSelected(Date startDate, Date endDate) {
                // Formatear fechas para mostrar
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String periodName = sdf.format(startDate) + " - " + sdf.format(endDate);

                navigateToReport(periodName, startDate, endDate);
            }
        });
        dateRangeDialog.show(getChildFragmentManager(), "dateRangeDialog");
    }

    private void navigateToReport(String periodName, Date startDate, Date endDate) {
        // Crear una instancia del fragmento de reporte con los parámetros adecuados
        ReportDetailFragment reportFragment = ReportDetailFragment.newInstance(
                hotelName, periodName, startDate.getTime(), endDate.getTime());

        // Reemplazar el fragmento actual con el fragmento de reporte
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, reportFragment)
                .addToBackStack(null)
                .commit();
    }

    public String getTitle() {
        if (getArguments() != null) {
            return getArguments().getString("HOTEL_NAME", "Filtro de Reportes");
        }
        return "Filtro de Reportes";
    }
}