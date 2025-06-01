package com.codebnb.stayflow.superAdmin;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

public class ReportDetailActivity extends AppCompatActivity {

    private static final String TAG = "ReportDetailActivity";
    public static final String EXTRA_HOTEL_NAME = "hotel_name";
    public static final String EXTRA_FILTER_TYPE = "filter_type";
    public static final String EXTRA_START_DATE = "start_date";
    public static final String EXTRA_END_DATE = "end_date";

    private String hotelName;
    private String filterType;
    private long startDate;
    private long endDate;

    private BarChart barChart;
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superadmin_reporte_vista);

        // Obtener datos del Intent
        hotelName = getIntent().getStringExtra(EXTRA_HOTEL_NAME);
        filterType = getIntent().getStringExtra(EXTRA_FILTER_TYPE);
        startDate = getIntent().getLongExtra(EXTRA_START_DATE, System.currentTimeMillis());
        endDate = getIntent().getLongExtra(EXTRA_END_DATE, System.currentTimeMillis());

        Log.d(TAG, "Hotel: " + hotelName);
        Log.d(TAG, "Filter Type: " + filterType);
        Log.d(TAG, "Start Date: " + new Date(startDate));
        Log.d(TAG, "End Date: " + new Date(endDate));

        initViews();
        setupClickListeners();
        loadChartData();
    }

    private void initViews() {
        ImageButton buttonBack = findViewById(R.id.buttonBack);
        TextView titleTextView = findViewById(R.id.textViewReportTitle);
        TextView totalResText = findViewById(R.id.textViewTotalReservas);
        TextView completadasText = findViewById(R.id.textViewCompletadas);
        TextView canceladasText = findViewById(R.id.textViewCanceladas);
        TextView montoText = findViewById(R.id.textViewMontoGenerado);
        TextView periodText = findViewById(R.id.textViewChartPeriod);

        barChart = findViewById(R.id.barChart);
        lineChart = findViewById(R.id.lineChart);

        Button buttonBarras = findViewById(R.id.buttonBarras);
        Button buttonLineal = findViewById(R.id.buttonLineal);

        // Títulos y estadísticas simuladas
        titleTextView.setText(hotelName + " - " + getFilterTitle());
        totalResText.setText("145 reservas");
        completadasText.setText("75 reservas");
        canceladasText.setText("21 reservas");
        montoText.setText("$ 10,567");
        periodText.setText(getFilterTitle());

        buttonBack.setOnClickListener(v -> finish());

        // Establece los colores iniciales correctamente
        buttonBarras.setBackgroundColor(getResources().getColor(R.color.purple_700)); // Fondo activo
        buttonBarras.setTextColor(Color.WHITE); // Texto activo
        buttonLineal.setBackgroundColor(Color.TRANSPARENT); // Fondo inactivo
        buttonLineal.setTextColor(getResources().getColor(R.color.purple_700)); // Texto inactivo

        // Mostrar barra por defecto
        barChart.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        Button buttonBarras = findViewById(R.id.buttonBarras);
        Button buttonLineal = findViewById(R.id.buttonLineal);

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