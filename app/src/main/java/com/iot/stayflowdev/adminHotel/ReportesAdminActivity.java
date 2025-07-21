package com.iot.stayflowdev.adminHotel;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.adapter.UsuarioResumenAdapter;
import com.iot.stayflowdev.adminHotel.model.UsuarioResumen;
import com.iot.stayflowdev.adminHotel.repository.AdminHotelViewModel;
import com.iot.stayflowdev.adminHotel.service.NotificacionService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ReportesAdminActivity extends AppCompatActivity {
    private ImageView notificationIcon;
    private TextView badgeText;
    private RecyclerView recyclerServicios;
    private RecyclerView recyclerUsuarios;
    private AdminHotelViewModel viewModel;
    private NotificacionService notificacionService;
    private String hotelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes_admin);

        inicializarVistas();
        configurarViewModel();
        configurarToolbar();
        configurarBotonesDescarga();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.menu_reportes);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_inicio) {
                startActivity(new Intent(this, AdminInicioActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_reportes) {
                return true;
            } else if (id == R.id.menu_huesped) {
                startActivity(new Intent(this, HuespedAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_mensajeria) {
                startActivity(new Intent(this, MensajeriaAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_perfil) {
                startActivity(new Intent(this, PerfilAdminActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        // Inicializar adapters y layout managers
        recyclerUsuarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerServicios.setLayoutManager(new LinearLayoutManager(this));

        // Configurar observer para hotelId
        configurarObserverHotelId();
    }

    private void inicializarVistas() {
        notificationIcon = findViewById(R.id.notification_icon);
        badgeText = findViewById(R.id.badge_text);
        recyclerServicios = findViewById(R.id.recyclerServicios);
        recyclerUsuarios = findViewById(R.id.recyclerUsuarios);
        notificacionService = new NotificacionService(this);
    }

    private void configurarViewModel() {
        viewModel = new ViewModelProvider(this).get(AdminHotelViewModel.class);
        viewModel.getContadorNotificaciones().observe(this, this::actualizarBadgeNotificaciones);
        viewModel.cargarNotificacionesCheckout();
        viewModel.iniciarActualizacionAutomatica();
    }

    private void configurarToolbar() {
        notificationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificacionesAdminActivity.class);
            startActivity(intent);
        });
    }

    private void configurarBotonesDescarga() {
        MaterialButton btnDescargarVentas = findViewById(R.id.btnDescargarVentas);
        MaterialButton btnDescargarReservas = findViewById(R.id.btnDescargarReservas);

        btnDescargarVentas.setOnClickListener(v -> {
            if (hotelId == null) {
                Toast.makeText(this, "Error: No se pudo obtener el ID del hotel", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Generando reporte de ventas...", Toast.LENGTH_SHORT).show();

            viewModel.obtenerVentasTotalesPorUsuario(hotelId).observe(this, mapa -> {
                if (mapa == null || mapa.isEmpty()) {
                    Toast.makeText(this, "No hay datos de ventas disponibles", Toast.LENGTH_LONG).show();
                    return;
                }
                generarPDFVentas(mapa);
            });
        });

        btnDescargarReservas.setOnClickListener(v -> {
            if (hotelId == null) {
                Toast.makeText(this, "Error: No se pudo obtener el ID del hotel", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Generando reporte de reservas...", Toast.LENGTH_SHORT).show();

            viewModel.obtenerReservasPorUsuario(hotelId).observe(this, mapa -> {
                if (mapa == null || mapa.isEmpty()) {
                    Toast.makeText(this, "No hay datos de reservas disponibles", Toast.LENGTH_LONG).show();
                    return;
                }
                generarPDFReservas(mapa);
            });
        });
    }

    private void configurarObserverHotelId() {
        viewModel.obtenerHotelIdAdministrador().observe(this, id -> {
            if (id != null) {
                hotelId = id;
                cargarReportes(hotelId);
            }
        });
    }

    private void cargarReportes(String hotelId) {
        // VENTAS TOTALES POR USUARIO
        viewModel.obtenerVentasTotalesPorUsuario(hotelId).observe(this, mapa -> {
            if (mapa == null || mapa.isEmpty()) {
                UsuarioResumenAdapter adapterVentas = new UsuarioResumenAdapter(new ArrayList<>());
                recyclerServicios.setAdapter(adapterVentas);
                return;
            }

            List<UsuarioResumen> lista = new ArrayList<>();
            for (Map.Entry<String, Double> entry : mapa.entrySet()) {
                lista.add(new UsuarioResumen(entry.getKey(), entry.getValue()));
            }

            lista.sort((a, b) -> Double.compare(b.getMonto(), a.getMonto()));
            viewModel.obtenerNombresUsuarios(lista, listaConNombres -> runOnUiThread(() -> {
                UsuarioResumenAdapter adapterVentas = new UsuarioResumenAdapter(new ArrayList<>());
                recyclerServicios.setAdapter(adapterVentas);
                adapterVentas.setLista(listaConNombres);
            }));
        });

        // CANTIDAD DE RESERVAS POR USUARIO
        viewModel.obtenerReservasPorUsuario(hotelId).observe(this, mapa -> {
            if (mapa == null || mapa.isEmpty()) {
                UsuarioResumenAdapter adapterReservas = new UsuarioResumenAdapter(new ArrayList<>());
                recyclerUsuarios.setAdapter(adapterReservas);
                return;
            }

            List<UsuarioResumen> lista = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : mapa.entrySet()) {
                lista.add(new UsuarioResumen(entry.getKey(), entry.getValue()));
            }

            lista.sort((a, b) -> Integer.compare(b.getCantidadReservas(), a.getCantidadReservas()));
            viewModel.obtenerNombresUsuarios(lista, listaConNombres -> runOnUiThread(() -> {
                UsuarioResumenAdapter adapterCantidad = new UsuarioResumenAdapter(new ArrayList<>());
                recyclerUsuarios.setAdapter(adapterCantidad);
                adapterCantidad.setLista(listaConNombres);
            }));
        });
    }

    private void actualizarBadgeNotificaciones(Integer contador) {
        if (contador != null && contador > 0) {
            badgeText.setVisibility(View.VISIBLE);
            badgeText.setText(contador > 99 ? "99+" : String.valueOf(contador));
        } else {
            badgeText.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.cargarNotificacionesCheckout();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewModel != null) {
            viewModel.detenerActualizacionAutomatica();
        }
    }

    private void generarPDFVentas(Map<String, Double> ventasPorUsuario) {
        if (ventasPorUsuario == null || ventasPorUsuario.isEmpty()) {
            Toast.makeText(this, "No hay datos de ventas para generar el reporte", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
        String fileName = "Reporte_Ventas_" + timeStamp + ".pdf";

        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Generando reporte PDF...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        PdfDocument document = new PdfDocument();
        int pageWidth = 595, pageHeight = 842;

        Paint titlePaint = new Paint();
        titlePaint.setTextSize(24);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setColor(Color.rgb(33, 150, 243));

        Paint subtitlePaint = new Paint();
        subtitlePaint.setTextSize(16);
        subtitlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        subtitlePaint.setColor(Color.rgb(68, 68, 68));

        Paint headerPaint = new Paint();
        headerPaint.setTextSize(14);
        headerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        headerPaint.setColor(Color.rgb(33, 33, 33));

        Paint normalPaint = new Paint();
        normalPaint.setTextSize(12);
        normalPaint.setColor(Color.rgb(66, 66, 66));

        Paint detailPaint = new Paint();
        detailPaint.setTextSize(10);
        detailPaint.setColor(Color.rgb(117, 117, 117));

        Paint linePaint = new Paint();
        linePaint.setColor(Color.rgb(224, 224, 224));
        linePaint.setStrokeWidth(1);

        Map<String, List<String>> detallesPorUsuario = new HashMap<>();
        Map<String, String> nombresPorUsuario = new HashMap<>();
        AtomicInteger pendientes = new AtomicInteger(ventasPorUsuario.size());
        AtomicInteger errores = new AtomicInteger(0);

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                if (pendientes.get() > 0) {
                    Toast.makeText(this, "Timeout: Generando PDF con datos disponibles...", Toast.LENGTH_SHORT).show();
                    generarPDFConDatosDisponibles(document, ventasPorUsuario, nombresPorUsuario, detallesPorUsuario, fileName, titlePaint, subtitlePaint, headerPaint, normalPaint, detailPaint, linePaint);
                }
            }
        }, 15000);

        for (Map.Entry<String, Double> entry : ventasPorUsuario.entrySet()) {
            String idUsuario = entry.getKey();
            double total = entry.getValue();

            db.collection("usuarios").document(idUsuario).get()
                    .addOnSuccessListener(userSnap -> {
                        try {
                            String nombre = userSnap.getString("nombres");
                            String apellidos = userSnap.getString("apellidos");
                            String nombreCompleto = ((nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : "")).trim();
                            if (nombreCompleto.isEmpty()) nombreCompleto = "Usuario " + idUsuario.substring(0, 8);
                            nombresPorUsuario.put(idUsuario, nombreCompleto);

                            db.collection("reservas")
                                    .whereEqualTo("idUsuario", idUsuario)
                                    .whereEqualTo("idHotel", hotelId)
                                    .get()
                                    .addOnSuccessListener(reservasSnap -> {
                                        try {
                                            List<String> detallesLista = new ArrayList<>();
                                            for (DocumentSnapshot doc : reservasSnap.getDocuments()) {
                                                try {
                                                    Timestamp fecha = doc.getTimestamp("fechaCreacion");
                                                    String fechaStr = fecha != null ?
                                                            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fecha.toDate()) :
                                                            "sin fecha";

                                                    double monto = 0.0;

                                                    try {
                                                        String costoTotalStr = doc.getString("costoTotal");
                                                        if (costoTotalStr != null && !costoTotalStr.isEmpty()) {
                                                            monto = Double.parseDouble(costoTotalStr);
                                                        } else {
                                                            Double costoTotalNum = doc.getDouble("costoTotal");
                                                            if (costoTotalNum != null) {
                                                                monto = costoTotalNum;
                                                            }
                                                        }
                                                    } catch (NumberFormatException e) {
                                                        monto = 0.0;
                                                    }

                                                    detallesLista.add("Reserva: " + doc.getId().substring(0, 8) + "... | " +
                                                            fechaStr + " | S/. " + String.format("%.2f", monto));

                                                } catch (Exception e) {
                                                    android.util.Log.w("PDF_Generation", "Error procesando reserva: " + e.getMessage());
                                                }
                                            }
                                            detallesPorUsuario.put(idUsuario, detallesLista);

                                            if (pendientes.decrementAndGet() == 0) {
                                                progressDialog.dismiss();
                                                generarPDFConDatosDisponibles(document, ventasPorUsuario, nombresPorUsuario, detallesPorUsuario, fileName, titlePaint, subtitlePaint, headerPaint, normalPaint, detailPaint, linePaint);
                                            }
                                        } catch (Exception e) {
                                            manejarErrorConsulta(pendientes, errores, progressDialog, document, ventasPorUsuario, nombresPorUsuario, detallesPorUsuario, fileName, titlePaint, subtitlePaint, headerPaint, normalPaint, detailPaint, linePaint, idUsuario);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        manejarErrorConsulta(pendientes, errores, progressDialog, document, ventasPorUsuario, nombresPorUsuario, detallesPorUsuario, fileName, titlePaint, subtitlePaint, headerPaint, normalPaint, detailPaint, linePaint, idUsuario);
                                    });
                        } catch (Exception e) {
                            manejarErrorConsulta(pendientes, errores, progressDialog, document, ventasPorUsuario, nombresPorUsuario, detallesPorUsuario, fileName, titlePaint, subtitlePaint, headerPaint, normalPaint, detailPaint, linePaint, idUsuario);
                        }
                    })
                    .addOnFailureListener(e -> {
                        nombresPorUsuario.put(idUsuario, "Usuario " + idUsuario.substring(0, 8));
                        manejarErrorConsulta(pendientes, errores, progressDialog, document, ventasPorUsuario, nombresPorUsuario, detallesPorUsuario, fileName, titlePaint, subtitlePaint, headerPaint, normalPaint, detailPaint, linePaint, idUsuario);
                    });
        }
    }

    private void manejarErrorConsulta(AtomicInteger pendientes, AtomicInteger errores, android.app.ProgressDialog progressDialog,
                                      PdfDocument document, Map<String, Double> ventasPorUsuario, Map<String, String> nombresPorUsuario,
                                      Map<String, List<String>> detallesPorUsuario, String fileName, Paint titlePaint, Paint subtitlePaint, Paint headerPaint,
                                      Paint normalPaint, Paint detailPaint, Paint linePaint, String idUsuario) {

        errores.incrementAndGet();
        detallesPorUsuario.put(idUsuario, new ArrayList<>());

        if (pendientes.decrementAndGet() == 0) {
            progressDialog.dismiss();
            if (errores.get() > 0) {
                Toast.makeText(this, "Algunas consultas fallaron. Generando PDF con datos disponibles...", Toast.LENGTH_SHORT).show();
            }
            generarPDFConDatosDisponibles(document, ventasPorUsuario, nombresPorUsuario, detallesPorUsuario, fileName, titlePaint, subtitlePaint, headerPaint, normalPaint, detailPaint, linePaint);
        }
    }

    private void generarPDFConDatosDisponibles(PdfDocument document, Map<String, Double> ventasPorUsuario,
                                               Map<String, String> nombresPorUsuario, Map<String, List<String>> detallesPorUsuario,
                                               String fileName, Paint titlePaint, Paint subtitlePaint, Paint headerPaint, Paint normalPaint,
                                               Paint detailPaint, Paint linePaint) {

        try {
            int pageWidth = 595, pageHeight = 842;
            PdfDocument.Page page = document.startPage(new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create());
            Canvas canvas = page.getCanvas();
            int y = 50;
            int leftMargin = 40;
            int rightMargin = pageWidth - 40;

            Paint headerBgPaint = new Paint();
            headerBgPaint.setColor(Color.rgb(245, 245, 245));
            canvas.drawRect(0, 0, pageWidth, 80, headerBgPaint);

            canvas.drawText("REPORTE DE VENTAS TOTALES", leftMargin, y + 25, titlePaint);

            String fechaReporte = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy - HH:mm",
                    new Locale("es", "ES")).format(new Date());
            canvas.drawText("Generado el: " + fechaReporte, leftMargin, y + 50, detailPaint);

            y = 110;

            double totalGeneral = ventasPorUsuario.values().stream().mapToDouble(Double::doubleValue).sum();
            Paint summaryBgPaint = new Paint();
            summaryBgPaint.setColor(Color.rgb(232, 245, 233));
            canvas.drawRect(leftMargin - 10, y - 5, rightMargin + 10, y + 35, summaryBgPaint);

            canvas.drawText("TOTAL GENERAL DE VENTAS: S/. " + String.format("%.2f", totalGeneral),
                    leftMargin, y + 20, headerPaint);
            y += 50;

            canvas.drawLine(leftMargin, y, rightMargin, y, linePaint);
            y += 20;

            int contador = 1;
            List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(ventasPorUsuario.entrySet());
            sortedEntries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

            for (Map.Entry<String, Double> sortedEntry : sortedEntries) {
                String uid = sortedEntry.getKey();
                String nombreUsr = nombresPorUsuario.getOrDefault(uid, "Usuario " + uid.substring(0, 8));
                double monto = sortedEntry.getValue();

                if (contador % 2 == 0) {
                    Paint userBgPaint = new Paint();
                    userBgPaint.setColor(Color.rgb(250, 250, 250));
                    canvas.drawRect(leftMargin - 10, y - 5, rightMargin + 10, y + 25, userBgPaint);
                }

                canvas.drawText(contador + ". " + nombreUsr, leftMargin, y + 15, headerPaint);
                String montoText = "S/. " + String.format("%.2f", monto);
                float montoWidth = headerPaint.measureText(montoText);
                canvas.drawText(montoText, rightMargin - montoWidth, y + 15, headerPaint);

                y += 30;

                List<String> detalles = detallesPorUsuario.getOrDefault(uid, new ArrayList<>());
                if (!detalles.isEmpty()) {
                    canvas.drawText("Reservas (" + detalles.size() + "):", leftMargin + 20, y, normalPaint);
                    y += 15;

                    for (String detalle : detalles) {
                        canvas.drawText("• " + detalle, leftMargin + 40, y, detailPaint);
                        y += 12;
                    }
                } else {
                    canvas.drawText("No se pudieron cargar los detalles", leftMargin + 20, y, detailPaint);
                    y += 15;
                }

                y += 10;
                canvas.drawLine(leftMargin, y, rightMargin, y, linePaint);
                y += 15;
                contador++;

                if (y > pageHeight - 100) {
                    document.finishPage(page);
                    page = document.startPage(new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, contador).create());
                    canvas = page.getCanvas();
                    y = 50;
                }
            }

            Paint footerPaint = new Paint();
            footerPaint.setTextSize(10);
            footerPaint.setColor(Color.rgb(158, 158, 158));
            canvas.drawText("Reporte generado automáticamente por StayFlow Admin",
                    leftMargin, pageHeight - 30, footerPaint);

            document.finishPage(page);
            guardarYAbrirPDF(document, fileName);

        } catch (Exception e) {
            Toast.makeText(this, "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (document != null) {
                document.close();
            }
        }
    }

    private void generarPDFReservas(Map<String, Integer> reservasPorUsuario) {
        if (reservasPorUsuario == null || reservasPorUsuario.isEmpty()) {
            Toast.makeText(this, "No hay datos de reservas para generar el reporte", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
        String fileName = "Reporte_Reservas_" + timeStamp + ".pdf";

        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Generando reporte PDF...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        PdfDocument document = new PdfDocument();
        int pageWidth = 595, pageHeight = 842;

        Paint titlePaint = new Paint();
        titlePaint.setTextSize(24);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setColor(Color.rgb(76, 175, 80));

        Paint subtitlePaint = new Paint();
        subtitlePaint.setTextSize(16);
        subtitlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        subtitlePaint.setColor(Color.rgb(68, 68, 68));

        Paint headerPaint = new Paint();
        headerPaint.setTextSize(14);
        headerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        headerPaint.setColor(Color.rgb(33, 33, 33));

        Paint normalPaint = new Paint();
        normalPaint.setTextSize(12);
        normalPaint.setColor(Color.rgb(66, 66, 66));

        Paint detailPaint = new Paint();
        detailPaint.setTextSize(10);
        detailPaint.setColor(Color.rgb(117, 117, 117));

        Paint linePaint = new Paint();
        linePaint.setColor(Color.rgb(224, 224, 224));
        linePaint.setStrokeWidth(1);

        Map<String, List<String>> detallesPorUsuario = new HashMap<>();
        Map<String, String> nombresPorUsuario = new HashMap<>();
        AtomicInteger pendientes = new AtomicInteger(reservasPorUsuario.size());

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                if (pendientes.get() > 0) {
                    Toast.makeText(this, "Timeout: Generando PDF con datos disponibles...", Toast.LENGTH_SHORT).show();
                    generarPDFReservasConDatos(document, reservasPorUsuario, nombresPorUsuario, detallesPorUsuario, fileName, titlePaint, subtitlePaint, headerPaint, normalPaint, detailPaint, linePaint);
                }
            }
        }, 15000);

        for (Map.Entry<String, Integer> entry : reservasPorUsuario.entrySet()) {
            String idUsuario = entry.getKey();
            int cantidadReservas = entry.getValue();

            db.collection("usuarios").document(idUsuario).get().addOnSuccessListener(userSnap -> {
                        String nombre = userSnap.getString("nombres");
                        String apellidos = userSnap.getString("apellidos");
                        String nombreCompleto = ((nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : "")).trim();
                        if (nombreCompleto.isEmpty()) nombreCompleto = "Usuario " + idUsuario.substring(0, 8);
                        nombresPorUsuario.put(idUsuario, nombreCompleto);

                        db.collection("reservas")
                                .whereEqualTo("idUsuario", idUsuario)
                                .whereEqualTo("idHotel", hotelId)
                                .get()
                                .addOnSuccessListener(reservasSnap -> {
                                    List<String> detallesLista = new ArrayList<>();
                                    for (DocumentSnapshot doc : reservasSnap.getDocuments()) {
                                        try {
                                            Timestamp fecha = doc.getTimestamp("fechaCreacion");
                                            String fechaStr = fecha != null ?
                                                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fecha.toDate()) :
                                                    "sin fecha";
                                            String estado = doc.getString("estado") != null ? doc.getString("estado") : "N/A";
                                            detallesLista.add("ID: " + doc.getId().substring(0, 8) + "... | " +
                                                    fechaStr + " | Estado: " + estado);
                                        } catch (Exception e) {
                                            android.util.Log.w("PDF_Generation", "Error procesando reserva: " + e.getMessage());
                                        }
                                    }
                                    detallesPorUsuario.put(idUsuario, detallesLista);

                                    if (pendientes.decrementAndGet() == 0) {
                                        progressDialog.dismiss();
                                        generarPDFReservasConDatos(document, reservasPorUsuario, nombresPorUsuario, detallesPorUsuario, fileName, titlePaint, subtitlePaint, headerPaint, normalPaint, detailPaint, linePaint);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    detallesPorUsuario.put(idUsuario, new ArrayList<>());
                                    if (pendientes.decrementAndGet() == 0) {
                                        progressDialog.dismiss();
                                        generarPDFReservasConDatos(document, reservasPorUsuario, nombresPorUsuario, detallesPorUsuario, fileName, titlePaint, subtitlePaint, headerPaint, normalPaint, detailPaint, linePaint);
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        nombresPorUsuario.put(idUsuario, "Usuario " + idUsuario.substring(0, 8));
                        detallesPorUsuario.put(idUsuario, new ArrayList<>());
                        if (pendientes.decrementAndGet() == 0) {
                            progressDialog.dismiss();
                            generarPDFReservasConDatos(document, reservasPorUsuario, nombresPorUsuario, detallesPorUsuario, fileName, titlePaint, subtitlePaint, headerPaint, normalPaint, detailPaint, linePaint);
                        }
                    });
        }
    }

    private void generarPDFReservasConDatos(PdfDocument document, Map<String, Integer> reservasPorUsuario,
                                            Map<String, String> nombresPorUsuario, Map<String, List<String>> detallesPorUsuario,
                                            String fileName, Paint titlePaint, Paint subtitlePaint, Paint headerPaint, Paint normalPaint,
                                            Paint detailPaint, Paint linePaint) {
        try {
            int pageWidth = 595, pageHeight = 842;
            PdfDocument.Page page = document.startPage(new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create());
            Canvas canvas = page.getCanvas();
            int y = 50;
            int leftMargin = 40;
            int rightMargin = pageWidth - 40;

            Paint headerBgPaint = new Paint();
            headerBgPaint.setColor(Color.rgb(245, 245, 245));
            canvas.drawRect(0, 0, pageWidth, 80, headerBgPaint);

            canvas.drawText("REPORTE DE RESERVAS POR USUARIO", leftMargin, y + 25, titlePaint);

            String fechaReporte = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy - HH:mm",
                    new Locale("es", "ES")).format(new Date());
            canvas.drawText("Generado el: " + fechaReporte, leftMargin, y + 50, detailPaint);

            y = 110;

            int totalReservas = reservasPorUsuario.values().stream().mapToInt(Integer::intValue).sum();
            Paint summaryBgPaint = new Paint();
            summaryBgPaint.setColor(Color.rgb(232, 245, 233));
            canvas.drawRect(leftMargin - 10, y - 5, rightMargin + 10, y + 35, summaryBgPaint);

            canvas.drawText("TOTAL DE RESERVAS: " + totalReservas + " reservas",
                    leftMargin, y + 20, headerPaint);
            y += 50;

            canvas.drawLine(leftMargin, y, rightMargin, y, linePaint);
            y += 20;

            int contador = 1;
            List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(reservasPorUsuario.entrySet());
            sortedEntries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

            for (Map.Entry<String, Integer> sortedEntry : sortedEntries) {
                String uid = sortedEntry.getKey();
                String nombreUsr = nombresPorUsuario.getOrDefault(uid, "Usuario " + uid.substring(0, 8));
                int cantidadUsuario = sortedEntry.getValue();

                if (contador % 2 == 0) {
                    Paint userBgPaint = new Paint();
                    userBgPaint.setColor(Color.rgb(250, 250, 250));
                    canvas.drawRect(leftMargin - 10, y - 5, rightMargin + 10, y + 25, userBgPaint);
                }

                canvas.drawText(contador + ". " + nombreUsr, leftMargin, y + 15, headerPaint);
                String cantidadText = cantidadUsuario + " reservas";
                float cantidadWidth = headerPaint.measureText(cantidadText);
                canvas.drawText(cantidadText, rightMargin - cantidadWidth, y + 15, headerPaint);

                y += 30;

                List<String> detalles = detallesPorUsuario.getOrDefault(uid, new ArrayList<>());
                if (!detalles.isEmpty()) {
                    canvas.drawText("Detalle de reservas:", leftMargin + 20, y, normalPaint);
                    y += 15;

                    for (String detalle : detalles) {
                        canvas.drawText("• " + detalle, leftMargin + 40, y, detailPaint);
                        y += 12;
                    }
                } else {
                    canvas.drawText("No se pudieron cargar los detalles", leftMargin + 20, y, detailPaint);
                    y += 15;
                }

                y += 10;
                canvas.drawLine(leftMargin, y, rightMargin, y, linePaint);
                y += 15;
                contador++;

                if (y > pageHeight - 100) {
                    document.finishPage(page);
                    page = document.startPage(new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, contador).create());
                    canvas = page.getCanvas();
                    y = 50;
                }
            }

            Paint footerPaint = new Paint();
            footerPaint.setTextSize(10);
            footerPaint.setColor(Color.rgb(158, 158, 158));
            canvas.drawText("Reporte generado automáticamente por StayFlow Admin",
                    leftMargin, pageHeight - 30, footerPaint);

            document.finishPage(page);
            guardarYAbrirPDF(document, fileName);

        } catch (Exception e) {
            Toast.makeText(this, "Error al generar PDF de reservas: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (document != null) {
                document.close();
            }
        }
    }

    private void guardarYAbrirPDF(PdfDocument document, String fileName) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }

        File file = new File(downloadsDir, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();

            String rutaCompleta = file.getAbsolutePath();
            Toast.makeText(this,
                    "✅ PDF generado exitosamente\n\n" +
                            "📁 Ubicación: Descargas/" + fileName + "\n\n" +
                            "Puedes encontrarlo en tu gestor de archivos o aplicación de Descargas",
                    Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "❌ Error al guardar el PDF: " + e.getMessage() + "\n\n" +
                            "Verifica que tengas espacio suficiente en el dispositivo",
                    Toast.LENGTH_LONG).show();
        }
    }
}