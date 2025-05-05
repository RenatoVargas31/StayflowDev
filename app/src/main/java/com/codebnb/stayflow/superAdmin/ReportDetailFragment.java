package com.codebnb.stayflow.superAdmin;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.codebnb.stayflow.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReportDetailFragment extends Fragment {

    private static final String TAG = "ReportDetailFragment";
    private static final String ARG_HOTEL_NAME = "hotel_name";
    private static final String ARG_FILTER_TYPE = "filter_type";
    private static final String ARG_START_DATE = "start_date";
    private static final String ARG_END_DATE = "end_date";

    private String hotelName;
    private String filterType;
    private long startDate;
    private long endDate;

    private BarChart barChart;
    private LineChart lineChart;

    public static ReportDetailFragment newInstance(String hotelName, String filterType, long startDate, long endDate) {
        ReportDetailFragment f = new ReportDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HOTEL_NAME, hotelName);
        args.putString(ARG_FILTER_TYPE, filterType);
        args.putLong(ARG_START_DATE, startDate);
        args.putLong(ARG_END_DATE, endDate);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            hotelName = getArguments().getString(ARG_HOTEL_NAME);
            filterType = getArguments().getString(ARG_FILTER_TYPE);
            startDate = getArguments().getLong(ARG_START_DATE);
            endDate = getArguments().getLong(ARG_END_DATE);

            Log.d(TAG, "Hotel: " + hotelName);
            Log.d(TAG, "Filter Type: " + filterType);
            Log.d(TAG, "Start Date: " + new Date(startDate));
            Log.d(TAG, "End Date: " + new Date(endDate));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.superadmin_reporte_vista, container, false);

        ImageButton buttonBack = view.findViewById(R.id.buttonBack);
        TextView titleTextView = view.findViewById(R.id.textViewReportTitle);
        TextView totalResText = view.findViewById(R.id.textViewTotalReservas);
        TextView completadasText = view.findViewById(R.id.textViewCompletadas);
        TextView canceladasText = view.findViewById(R.id.textViewCanceladas);
        TextView montoText = view.findViewById(R.id.textViewMontoGenerado);
        TextView periodText = view.findViewById(R.id.textViewChartPeriod);

        barChart = view.findViewById(R.id.barChart);
        lineChart = view.findViewById(R.id.lineChart);

        Button buttonBarras = view.findViewById(R.id.buttonBarras);
        Button buttonLineal = view.findViewById(R.id.buttonLineal);

        // Títulos y estadísticas simuladas
        titleTextView.setText(hotelName + " - " + getFilterTitle());
        totalResText.setText("145 reservas");
        completadasText.setText("75 reservas");
        canceladasText.setText("21 reservas");
        montoText.setText("$ 10,567");
        periodText.setText(getFilterTitle());

        buttonBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Establece los colores iniciales correctamente
        buttonBarras.setBackgroundColor(getResources().getColor(R.color.purple_700)); // Fondo activo
        buttonBarras.setTextColor(Color.WHITE); // Texto activo
        buttonLineal.setBackgroundColor(Color.TRANSPARENT); // Fondo inactivo
        buttonLineal.setTextColor(getResources().getColor(R.color.purple_700)); // Texto inactivo

        // Toggle de gráficos con configuración completa de estilos
        buttonBarras.setOnClickListener(v -> {
            barChart.setVisibility(View.VISIBLE);
            lineChart.setVisibility(View.GONE);

            // Actualiza apariencia de botones
            buttonBarras.setBackgroundColor(getResources().getColor(R.color.purple_700));
            buttonBarras.setTextColor(Color.WHITE);
            buttonLineal.setBackgroundColor(Color.TRANSPARENT);
            buttonLineal.setTextColor(getResources().getColor(R.color.purple_700));
        });

        buttonLineal.setOnClickListener(v -> {
            lineChart.setVisibility(View.VISIBLE);
            barChart.setVisibility(View.GONE);

            // Actualiza apariencia de botones
            buttonLineal.setBackgroundColor(getResources().getColor(R.color.purple_700));
            buttonLineal.setTextColor(Color.WHITE);
            buttonBarras.setBackgroundColor(Color.TRANSPARENT);
            buttonBarras.setTextColor(getResources().getColor(R.color.purple_700));
        });

        // Mostrar barra por defecto
        barChart.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.GONE);

        loadChartData();
        return view;
    }

    private String getFilterTitle() {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return "Reporte del " + fmt.format(new Date(startDate))
                    + " al " + fmt.format(new Date(endDate));
        } catch (Exception e) {
            Log.e(TAG, "Error formatting dates", e);
            return "Reporte";
        }
    }

    private void loadChartData() {
        // Datos de ejemplo para el gráfico
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<Entry> lineEntries = new ArrayList<>();

        // Generar datos aleatorios para 7 días
        for (int i = 0; i < 7; i++) {
            float value = (float) (Math.random() * 100);
            barEntries.add(new BarEntry(i, value));
            lineEntries.add(new Entry(i, value));
        }

        // Configurar gráfico de barras
        BarDataSet barDataSet = new BarDataSet(barEntries, "Reservas");
        barDataSet.setColor(getResources().getColor(R.color.md_theme_inversePrimary));
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.invalidate();

        // Configurar gráfico lineal
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Reservas");
        lineDataSet.setColor(getResources().getColor(R.color.md_theme_inversePrimary));
        lineDataSet.setCircleColor(getResources().getColor(R.color.md_theme_inversePrimary));
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }

}