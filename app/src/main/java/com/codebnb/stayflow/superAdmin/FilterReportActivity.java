package com.codebnb.stayflow.superAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.codebnb.stayflow.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FilterReportActivity extends AppCompatActivity implements DateRangeDialogFragment.DateRangeListener {

    private String hotelName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superadmin_filtrado_reporte_hotel);

        // Obtener el nombre del hotel del Intent
        hotelName = getIntent().getStringExtra("HOTEL_NAME");

        if (hotelName == null) {
            // Si no se ha pasado un hotel válido, volver atrás
            finish();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        // No necesitamos inicializar vistas específicas aquí
        // ya que se referencian directamente en setupListeners()
    }

    private void setupListeners() {
        ImageButton buttonBack = findViewById(R.id.buttonBack);
        CardView cardRangoFechas = findViewById(R.id.cardRangoFechas);
        CardView cardUltimaSemana = findViewById(R.id.cardUltimaSemana);
        CardView cardUltimoMes = findViewById(R.id.cardUltimoMes);
        CardView cardUltimosSeisMeses = findViewById(R.id.cardUltimosSeisMeses);
        CardView cardUltimoAnio = findViewById(R.id.cardUltimoAnio);

        buttonBack.setOnClickListener(v -> finish());

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
    }

    private void showDateRangePicker() {
        DateRangeDialogFragment dateRangeDialog = new DateRangeDialogFragment();
        dateRangeDialog.setDateRangeListener(this);
        dateRangeDialog.show(getSupportFragmentManager(), "dateRangeDialog");
    }

    @Override
    public void onDateRangeSelected(Date startDate, Date endDate) {
        // Formatear fechas para mostrar
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String periodName = sdf.format(startDate) + " - " + sdf.format(endDate);

        navigateToReport(periodName, startDate, endDate);
    }

    private void navigateToReport(String periodName, Date startDate, Date endDate) {
        Intent intent = new Intent(this, ReportDetailActivity.class);
        intent.putExtra("HOTEL_NAME", hotelName);
        intent.putExtra("PERIOD_NAME", periodName);
        intent.putExtra("START_DATE", startDate.getTime());
        intent.putExtra("END_DATE", endDate.getTime());
        startActivity(intent);
    }

    public static void start(AppCompatActivity activity, String hotelName) {
        Intent intent = new Intent(activity, FilterReportActivity.class);
        intent.putExtra("HOTEL_NAME", hotelName);
        activity.startActivity(intent);
    }
}