package com.iot.stayflowdev.superAdmin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.Reserva;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.image.ImageDataFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportDetailActivity extends AppCompatActivity {

    private static final String TAG = "ReportDetailActivity";
    public static final String EXTRA_HOTEL_NAME = "HOTEL_NAME";
    public static final String EXTRA_HOTEL_ID = "HOTEL_ID";
    public static final String EXTRA_FILTER_TYPE = "filter_type";
    public static final String EXTRA_START_DATE = "start_date";
    public static final String EXTRA_END_DATE = "end_date";

    private String hotelName;
    private String hotelId;
    private String filterType;
    private long startDate;
    private long endDate;

    private BarChart barChart;
    private LineChart lineChart;
    private TextView totalResText, completadasText, canceladasText, montoText;

    // Firebase
    private FirebaseFirestore db;

    // Datos del reporte
    private List<Reserva> reservasDelHotel;
    private int totalReservas = 0;
    private int reservasCompletadas = 0;
    private int reservasCanceladas = 0;
    private double montoTotal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superadmin_reporte_vista);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        reservasDelHotel = new ArrayList<>();

        // Obtener datos del Intent
        hotelName = getIntent().getStringExtra(EXTRA_HOTEL_NAME);
        hotelId = getIntent().getStringExtra(EXTRA_HOTEL_ID);
        filterType = getIntent().getStringExtra(EXTRA_FILTER_TYPE);
        startDate = getIntent().getLongExtra(EXTRA_START_DATE, System.currentTimeMillis());
        endDate = getIntent().getLongExtra(EXTRA_END_DATE, System.currentTimeMillis());

        Log.d(TAG, "Hotel: " + hotelName + " (ID: " + hotelId + ")");
        Log.d(TAG, "Filter Type: " + filterType);
        Log.d(TAG, "Start Date: " + new Date(startDate));
        Log.d(TAG, "End Date: " + new Date(endDate));

        initViews();
        setupClickListeners();
        loadReportData();
    }

    private void initViews() {
        ImageButton buttonBack = findViewById(R.id.buttonBack);
        ImageButton buttonExport = findViewById(R.id.buttonExport);
        TextView titleTextView = findViewById(R.id.textViewReportTitle);
        totalResText = findViewById(R.id.textViewTotalReservas);
        completadasText = findViewById(R.id.textViewCompletadas);
        canceladasText = findViewById(R.id.textViewCanceladas);
        montoText = findViewById(R.id.textViewMontoGenerado);
        TextView periodText = findViewById(R.id.textViewChartPeriod);

        barChart = findViewById(R.id.barChart);
        lineChart = findViewById(R.id.lineChart);

        Button buttonBarras = findViewById(R.id.buttonBarras);
        Button buttonLineal = findViewById(R.id.buttonLineal);

        // Configurar títulos
        titleTextView.setText(hotelName + " - Reporte");
        periodText.setText(getFilterTitle());

        // Mostrar valores iniciales
        updateStatistics();

        buttonBack.setOnClickListener(v -> finish());
        buttonExport.setOnClickListener(v -> exportToPdf());

        // Establece los colores iniciales
        buttonBarras.setBackgroundColor(getResources().getColor(R.color.blue_500));
        buttonBarras.setTextColor(Color.WHITE);
        buttonLineal.setBackgroundColor(Color.TRANSPARENT);
        buttonLineal.setTextColor(getResources().getColor(R.color.blue_500));

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
            buttonBarras.setBackgroundColor(getResources().getColor(R.color.blue_500));
            buttonBarras.setTextColor(Color.WHITE);
            buttonLineal.setBackgroundColor(Color.TRANSPARENT);
            buttonLineal.setTextColor(getResources().getColor(R.color.blue_500));
        });

        buttonLineal.setOnClickListener(v -> {
            lineChart.setVisibility(View.VISIBLE);
            barChart.setVisibility(View.GONE);

            // Actualiza apariencia de botones
            buttonLineal.setBackgroundColor(getResources().getColor(R.color.blue_500));
            buttonLineal.setTextColor(Color.WHITE);
            buttonBarras.setBackgroundColor(Color.TRANSPARENT);
            buttonBarras.setTextColor(getResources().getColor(R.color.blue_500));
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

    private void loadReportData() {
        Log.d(TAG, "Cargando datos del reporte para hotel: " + hotelName);

        if (hotelId == null || hotelId.isEmpty()) {
            Log.e(TAG, "Hotel ID is null or empty");
            Toast.makeText(this, "Error: ID del hotel no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar loading
        showLoading(true);

        // Consultar reservas del hotel en el rango de fechas
        db.collection("reservas")
                .whereEqualTo("idHotel", hotelId)
                .whereGreaterThanOrEqualTo("fechaCreacion", new Date(startDate))
                .whereLessThanOrEqualTo("fechaCreacion", new Date(endDate))
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        reservasDelHotel.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Reserva reserva = document.toObject(Reserva.class);
                                reserva.setId(document.getId());
                                reservasDelHotel.add(reserva);
                            } catch (Exception e) {
                                Log.e(TAG, "Error al convertir documento a Reserva: " + e.getMessage());
                            }
                        }

                        Log.d(TAG, "Reservas cargadas: " + reservasDelHotel.size());
                        processReportData();

                    } else {
                        Log.e(TAG, "Error al cargar reservas", task.getException());
                        Toast.makeText(this, "Error al cargar datos del reporte", Toast.LENGTH_SHORT).show();
                        // Mostrar datos por defecto en caso de error
                        updateStatistics();
                        loadChartDataDefault();
                    }
                });
    }

    private void processReportData() {
        totalReservas = reservasDelHotel.size();
        reservasCompletadas = 0;
        reservasCanceladas = 0;
        montoTotal = 0.0;

        for (Reserva reserva : reservasDelHotel) {
            // Contar estados
            if ("confirmada".equals(reserva.getEstado()) || "completada".equals(reserva.getEstado())) {
                reservasCompletadas++;
            } else if ("cancelada".equals(reserva.getEstado())) {
                reservasCanceladas++;
            }

            // Sumar montos
            if (reserva.getCostoTotal() != null && !reserva.getCostoTotal().isEmpty()) {
                try {
                    double costo = Double.parseDouble(reserva.getCostoTotal().replace(",", ""));
                    montoTotal += costo;
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Error al parsear costo: " + reserva.getCostoTotal());
                }
            }
        }

        // Actualizar UI
        updateStatistics();
        loadChartDataFromReservas();
    }

    private void updateStatistics() {
        totalResText.setText(totalReservas + " reservas");
        completadasText.setText(reservasCompletadas + " reservas");
        canceladasText.setText(reservasCanceladas + " reservas");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
        montoText.setText(currencyFormat.format(montoTotal));
    }

    private void loadChartDataFromReservas() {
        // Agrupar reservas por día
        Map<String, Integer> reservasPorDia = new HashMap<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

        // Crear mapa de días en el rango
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(startDate));

        while (cal.getTimeInMillis() <= endDate) {
            String dayKey = dayFormat.format(cal.getTime());
            reservasPorDia.put(dayKey, 0);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Contar reservas por día
        for (Reserva reserva : reservasDelHotel) {
            if (reserva.getFechaCreacion() != null) {
                String day = dayFormat.format(reserva.getFechaCreacion().toDate());
                reservasPorDia.put(day, reservasPorDia.getOrDefault(day, 0) + 1);
            }
        }

        // Preparar datos para los gráficos
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<Entry> lineEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Integer> entry : reservasPorDia.entrySet()) {
            barEntries.add(new BarEntry(index, entry.getValue()));
            lineEntries.add(new Entry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        // Configurar gráficos
        setupBarChart(barEntries, labels);
        setupLineChart(lineEntries, labels);
    }

    private void setupBarChart(ArrayList<BarEntry> barEntries, ArrayList<String> labels) {
        BarDataSet barDataSet = new BarDataSet(barEntries, "Reservas por día");
        barDataSet.setColor(getResources().getColor(R.color.md_theme_primary));
        barDataSet.setValueTextSize(10f);

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setGranularity(1f);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void setupLineChart(ArrayList<Entry> lineEntries, ArrayList<String> labels) {
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Reservas por día");
        lineDataSet.setColor(getResources().getColor(R.color.md_theme_primary));
        lineDataSet.setCircleColor(getResources().getColor(R.color.md_theme_primary));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(4f);
        lineDataSet.setValueTextSize(10f);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.getXAxis().setGranularity(1f);
        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    private void loadChartDataDefault() {
        // Datos de ejemplo cuando no hay datos reales
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<Entry> lineEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            float value = (float) (Math.random() * 10);
            barEntries.add(new BarEntry(i, value));
            lineEntries.add(new Entry(i, value));
            labels.add("Día " + (i + 1));
        }

        setupBarChart(barEntries, labels);
        setupLineChart(lineEntries, labels);
    }

    private void exportToPdf() {
        try {
            // Crear directorio si no existe
            File directory = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "StayflowReports");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Crear nombre de archivo con fecha
            String fileName = "Reporte_" + hotelName.replace(" ", "_") + "_" + 
                            new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                            .format(new Date()) + ".pdf";
            File file = new File(directory, fileName);

            // Crear PDF
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Título
            Paragraph title = new Paragraph("Reporte de " + hotelName)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20);
            document.add(title);

            // Período
            Paragraph period = new Paragraph(getFilterTitle())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14);
            document.add(period);

            // Tabla de estadísticas
            Table table = new Table(2);
            table.setWidth(UnitValue.createPercentValue(100));
            
            addTableRow(table, "Total Reservas", "145");
            addTableRow(table, "Reservas Completadas", "75");
            addTableRow(table, "Reservas Canceladas", "21");
            addTableRow(table, "Monto Generado", "$ 10,567");
            
            document.add(table);

            // Capturar y agregar el gráfico actual
            try {
                // Obtener el gráfico actual (barra o línea)
                View chartView = barChart.getVisibility() == View.VISIBLE ? barChart : lineChart;
                
                // Crear un bitmap del gráfico
                chartView.setDrawingCacheEnabled(true);
                chartView.buildDrawingCache();
                Bitmap chartBitmap = Bitmap.createBitmap(chartView.getDrawingCache());
                chartView.setDrawingCacheEnabled(false);

                // Convertir el bitmap a bytes
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                chartBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bitmapData = stream.toByteArray();

                // Crear imagen para el PDF
                Image chartImage = new Image(ImageDataFactory.create(bitmapData));
                
                // Ajustar el tamaño de la imagen para que se ajuste al ancho de la página
                float pageWidth = pdf.getDefaultPageSize().getWidth() - 50; // 50 puntos de margen
                float imageWidth = chartImage.getImageWidth();
                float imageHeight = chartImage.getImageHeight();
                float ratio = imageHeight / imageWidth;
                chartImage.setWidth(pageWidth);
                chartImage.setHeight(pageWidth * ratio);

                // Agregar título del gráfico
                Paragraph chartTitle = new Paragraph("Gráfico de Reservas")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(14)
                        .setMarginTop(20);
                document.add(chartTitle);

                // Agregar la imagen al PDF
                document.add(chartImage);
            } catch (Exception e) {
                Log.e(TAG, "Error al agregar el gráfico al PDF", e);
            }

            document.close();

            // Compartir el PDF
            sharePdf(file);

            Toast.makeText(this, "Reporte exportado exitosamente", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error al exportar PDF", e);
            Toast.makeText(this, "Error al exportar el reporte", Toast.LENGTH_SHORT).show();
        }
    }

    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Paragraph(label));
        table.addCell(new Paragraph(value));
    }

    private void sharePdf(File file) {
        Uri uri = FileProvider.getUriForFile(this,
                getApplicationContext().getPackageName() + ".provider",
                file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Compartir Reporte"));
    }

    private void showLoading(boolean isLoading) {
        // Método para mostrar u ocultar un indicador de carga (spinner)
        // Como no existe loadingIndicator en el layout, usamos un enfoque alternativo

        // Opción 1: Deshabilitar botones durante la carga
        Button buttonBarras = findViewById(R.id.buttonBarras);
        Button buttonLineal = findViewById(R.id.buttonLineal);
        ImageButton buttonExport = findViewById(R.id.buttonExport);

        if (buttonBarras != null) buttonBarras.setEnabled(!isLoading);
        if (buttonLineal != null) buttonLineal.setEnabled(!isLoading);
        if (buttonExport != null) buttonExport.setEnabled(!isLoading);

        // Opción 2: Mostrar/ocultar los gráficos como indicador visual
        if (isLoading) {
            if (barChart != null) barChart.setVisibility(View.INVISIBLE);
            if (lineChart != null) lineChart.setVisibility(View.INVISIBLE);
        } else {
            // Restaurar visibilidad del gráfico activo
            Button activeButton = findViewById(R.id.buttonBarras);
            if (activeButton != null && activeButton.getCurrentTextColor() == Color.WHITE) {
                if (barChart != null) barChart.setVisibility(View.VISIBLE);
                if (lineChart != null) lineChart.setVisibility(View.GONE);
            } else {
                if (barChart != null) barChart.setVisibility(View.GONE);
                if (lineChart != null) lineChart.setVisibility(View.VISIBLE);
            }
        }
    }
}
