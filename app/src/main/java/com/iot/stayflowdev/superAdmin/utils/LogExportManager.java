package com.iot.stayflowdev.superAdmin.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.iot.stayflowdev.superAdmin.model.LogItem;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utilidad para exportar logs a formatos PDF y CSV
 */
public class LogExportManager {

    private static final String TAG = "LogExportManager";
    private static final String FILE_PROVIDER_AUTHORITY = "com.iot.stayflowdev.fileprovider";
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST = 100;

    /**
     * Exporta una lista de logs a un archivo PDF
     *
     * @param context Contexto de la aplicación
     * @param logItems Lista de logs a exportar
     * @param categoryFilter Filtro de categoría aplicado (o null si no hay filtro)
     */
    public static void exportToPdf(Context context, List<LogItem> logItems, String categoryFilter) {
        try {
            // Verificar si tenemos los permisos necesarios para versiones anteriores a Android 10
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (!checkStoragePermission(context)) {
                    Toast.makeText(context, "Se requieren permisos de almacenamiento para exportar", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Crear nombre de archivo con fecha actual
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "logs_" + timestamp + ".pdf";

            // Directorio para guardar el PDF
            File pdfDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "logs");
            if (!pdfDir.exists()) {
                boolean dirCreated = pdfDir.mkdirs();
                if (!dirCreated) {
                    Log.e(TAG, "No se pudo crear el directorio");
                    Toast.makeText(context, "Error al crear directorio para PDF", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            File pdfFile = new File(pdfDir, fileName);

            Log.d(TAG, "Ruta del archivo PDF: " + pdfFile.getAbsolutePath());

            // Crear el documento PDF
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);

            // Estilos y configuración del documento
            document.setMargins(20, 20, 20, 20);

            // Título del documento
            String title = "Registro de Logs del Sistema";
            if (categoryFilter != null && !categoryFilter.equals(LogItem.CATEGORY_ALL)) {
                switch (categoryFilter) {
                    case LogItem.CATEGORY_HOTELS:
                        title += " - Hoteles";
                        break;
                    case LogItem.CATEGORY_ACCOUNT:
                        title += " - Cuentas";
                        break;
                    case LogItem.CATEGORY_RESERVATION:
                        title += " - Reservaciones";
                        break;
                }
            }
            Paragraph titleParagraph = new Paragraph(title)
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(titleParagraph);

            // Fecha de generación
            String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
            Paragraph dateParagraph = new Paragraph("Generado el: " + currentDate)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(dateParagraph);

            document.add(new Paragraph("\n"));

            // Crear tabla
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 6}))
                    .useAllAvailableWidth();

            // Encabezados de la tabla
            DeviceRgb headerBgColor = new DeviceRgb(22, 87, 136);
            table.addCell(createHeaderCell("Título", headerBgColor));
            table.addCell(createHeaderCell("Fecha/Hora", headerBgColor));
            table.addCell(createHeaderCell("Descripción", headerBgColor));

            // Añadir los logs a la tabla
            for (LogItem log : logItems) {
                table.addCell(createCell(log.title));
                table.addCell(createCell(log.timestamp));
                table.addCell(createCell(log.description));
            }

            // Añadir la tabla al documento
            document.add(table);

            // Pie de página
            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("Total de registros: " + logItems.size())
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(10));

            // Cerrar el documento
            document.close();

            Log.d(TAG, "PDF creado correctamente en: " + pdfFile.getAbsolutePath());

            // Compartir el PDF
            sharePdfFile(context, pdfFile);

        } catch (IOException e) {
            Log.e(TAG, "Error al exportar a PDF", e);
            Toast.makeText(context, "Error al exportar a PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error inesperado al exportar a PDF", e);
            Toast.makeText(context, "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Exporta una lista de logs a un archivo CSV
     *
     * @param context Contexto de la aplicación
     * @param logItems Lista de logs a exportar
     * @param categoryFilter Filtro de categoría aplicado (o null si no hay filtro)
     */
    public static void exportToCsv(Context context, List<LogItem> logItems, String categoryFilter) {
        try {
            // Verificar si tenemos los permisos necesarios para versiones anteriores a Android 10
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (!checkStoragePermission(context)) {
                    Toast.makeText(context, "Se requieren permisos de almacenamiento para exportar", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Crear nombre de archivo con fecha actual
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "logs_" + timestamp + ".csv";

            // Directorio para guardar el CSV
            File csvDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "logs");
            if (!csvDir.exists()) {
                boolean dirCreated = csvDir.mkdirs();
                if (!dirCreated) {
                    Log.e(TAG, "No se pudo crear el directorio");
                    Toast.makeText(context, "Error al crear directorio para CSV", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            File csvFile = new File(csvDir, fileName);

            Log.d(TAG, "Ruta del archivo CSV: " + csvFile.getAbsolutePath());

            // Crear el archivo CSV
            CSVWriter csvWriter = new CSVWriter(new FileWriter(csvFile));

            // Escribir encabezados
            String[] header = {"Título", "Fecha/Hora", "Categoría", "Descripción"};
            csvWriter.writeNext(header);

            // Escribir datos
            for (LogItem log : logItems) {
                String categoryText;
                switch (log.category) {
                    case LogItem.CATEGORY_HOTELS:
                        categoryText = "Hoteles";
                        break;
                    case LogItem.CATEGORY_ACCOUNT:
                        categoryText = "Cuentas";
                        break;
                    case LogItem.CATEGORY_RESERVATION:
                        categoryText = "Reservaciones";
                        break;
                    default:
                        categoryText = "General";
                        break;
                }

                String[] data = {
                        log.title,
                        log.timestamp,
                        categoryText,
                        log.description
                };
                csvWriter.writeNext(data);
            }

            // Cerrar el archivo
            csvWriter.close();

            Log.d(TAG, "CSV creado correctamente en: " + csvFile.getAbsolutePath());

            // Compartir el CSV
            shareCsvFile(context, csvFile);

        } catch (IOException e) {
            Log.e(TAG, "Error al exportar a CSV", e);
            Toast.makeText(context, "Error al exportar a CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error inesperado al exportar a CSV", e);
            Toast.makeText(context, "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Verifica si la app tiene permiso de escritura en el almacenamiento
     */
    private static boolean checkStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true; // En Android 10 y superior, usamos el almacenamiento de la app
        }

        int permission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Solicita permisos de almacenamiento si es necesario
     */
    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST);
        }
    }

    /**
     * Crea una celda de encabezado para la tabla PDF
     */
    private static Cell createHeaderCell(String text, DeviceRgb bgColor) {
        Cell cell = new Cell()
                .add(new Paragraph(text)
                        .setBold()
                        .setFontSize(11))
                .setBackgroundColor(bgColor)
                .setFontColor(ColorConstants.WHITE)
                .setPadding(5);
        return cell;
    }

    /**
     * Crea una celda de datos para la tabla PDF
     */
    private static Cell createCell(String text) {
        Cell cell = new Cell()
                .add(new Paragraph(text != null ? text : "")
                        .setFontSize(10))
                .setPadding(5);
        return cell;
    }

    /**
     * Comparte el archivo PDF generado
     */
    private static void sharePdfFile(Context context, File pdfFile) {
        try {
            Uri uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, pdfFile);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Registro de Logs del Sistema");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Verificar que haya apps que puedan manejar este intent
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(Intent.createChooser(intent, "Compartir PDF"));
            } else {
                Toast.makeText(context, "No hay aplicaciones para compartir PDF", Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(context, "PDF exportado con éxito", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error al compartir PDF", e);
            Toast.makeText(context, "Error al compartir PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Comparte el archivo CSV generado
     */
    private static void shareCsvFile(Context context, File csvFile) {
        try {
            Uri uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, csvFile);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Registro de Logs del Sistema");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Verificar que haya apps que puedan manejar este intent
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(Intent.createChooser(intent, "Compartir CSV"));
            } else {
                Toast.makeText(context, "No hay aplicaciones para compartir CSV", Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(context, "CSV exportado con éxito", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error al compartir CSV", e);
            Toast.makeText(context, "Error al compartir CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
