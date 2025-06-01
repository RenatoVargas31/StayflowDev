package com.codebnb.stayflow.superAdmin;

import androidx.core.util.Pair;
import androidx.fragment.app.FragmentManager;

import com.codebnb.stayflow.R;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// DateRangePickerUtil.java - Clase útil para mostrar el selector de fechas
public class DateRangePickerUtil {

    public interface DateRangeSelectedListener {
        void onDateRangeSelected(long startDate, long endDate, String startDateString, String endDateString);
    }

    public static void showDateRangePicker(FragmentManager fragmentManager, DateRangeSelectedListener listener) {
        MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Selecciona el rango que deseas visualizar");
        //builder.setTheme(R.style.ThemeMaterialCalendar); // Asegúrate de definir este estilo

        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();

        picker.addOnPositiveButtonClickListener(selection -> {
            Long startDate = selection.first;
            Long endDate = selection.second;

            // Formatear fechas para mostrar
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            String startDateString = dateFormat.format(new Date(startDate));
            String endDateString = dateFormat.format(new Date(endDate));

            listener.onDateRangeSelected(startDate, endDate, startDateString, endDateString);
        });

        picker.show(fragmentManager, "DATE_RANGE_PICKER");
    }
}