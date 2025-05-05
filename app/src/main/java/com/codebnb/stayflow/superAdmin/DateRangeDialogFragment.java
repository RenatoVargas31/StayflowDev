package com.codebnb.stayflow.superAdmin;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.codebnb.stayflow.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateRangeDialogFragment extends DialogFragment {

    private TextInputLayout textInputStartDate, textInputEndDate;
    private TextInputEditText editTextStartDate, editTextEndDate;
    private CalendarView calendarView;
    private Button buttonCancel, buttonContinuar;

    private Date startDate = null;
    private Date endDate = null;
    private boolean isSelectingStartDate = true;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface DateRangeListener {
        void onDateRangeSelected(Date startDate, Date endDate);
    }

    private DateRangeListener listener;

    public void setDateRangeListener(DateRangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_MaterialComponents_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.superadmin_selector_rango_fechas_reporte, container, false);

        initViews(view);
        setupListeners();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(width, height);
            }
        }
    }

    private void initViews(View view) {
        textInputStartDate = view.findViewById(R.id.textInputStartDate);
        textInputEndDate = view.findViewById(R.id.textInputEndDate);
        editTextStartDate = view.findViewById(R.id.editTextStartDate);
        editTextEndDate = view.findViewById(R.id.editTextEndDate);
        calendarView = view.findViewById(R.id.calendarView);
        buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonContinuar = view.findViewById(R.id.buttonContinuar);

        buttonContinuar.setEnabled(false);
    }

    private void setupListeners() {
        editTextStartDate.setOnClickListener(v -> {
            isSelectingStartDate = true;
            highlightSelectedField();
        });

        editTextEndDate.setOnClickListener(v -> {
            // Solo permitir seleccionar la fecha final si ya hay una fecha inicial
            if (startDate != null) {
                isSelectingStartDate = false;
                highlightSelectedField();
            } else {
                Toast.makeText(getContext(), "Primero selecciona la fecha inicial",
                        Toast.LENGTH_SHORT).show();
            }
        });

        textInputStartDate.setEndIconOnClickListener(v -> {
            isSelectingStartDate = true;
            highlightSelectedField();
        });

        textInputEndDate.setEndIconOnClickListener(v -> {
            if (startDate != null) {
                isSelectingStartDate = false;
                highlightSelectedField();
            } else {
                Toast.makeText(getContext(), "Primero selecciona la fecha inicial",
                        Toast.LENGTH_SHORT).show();
            }
        });

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            Date selectedDate = calendar.getTime();

            if (isSelectingStartDate) {
                startDate = selectedDate;
                editTextStartDate.setText(dateFormat.format(startDate));

                // Automáticamente cambia a la selección de fecha final
                isSelectingStartDate = false;
                highlightSelectedField();
            } else {
                // Asegurarse que la fecha final no sea anterior a la fecha inicial
                if (startDate != null && selectedDate.before(startDate)) {
                    Toast.makeText(getContext(), "La fecha final debe ser posterior a la inicial",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                endDate = selectedDate;
                editTextEndDate.setText(dateFormat.format(endDate));
            }

            // Habilitar botón Continuar cuando ambas fechas estén seleccionadas
            buttonContinuar.setEnabled(startDate != null && endDate != null);
        });

        buttonCancel.setOnClickListener(v -> dismiss());

        buttonContinuar.setOnClickListener(v -> {
            if (startDate != null && endDate != null && listener != null) {
                listener.onDateRangeSelected(startDate, endDate);
                dismiss();
            }
        });

        // Resaltar campo inicial
        highlightSelectedField();
    }

    private void highlightSelectedField() {
        if (isSelectingStartDate) {
            textInputStartDate.setBoxStrokeColor(ContextCompat.getColor(getContext(), R.color.purple_700));
            textInputEndDate.setBoxStrokeColor(ContextCompat.getColor(getContext(), R.color.gray));
        } else {
            textInputStartDate.setBoxStrokeColor(ContextCompat.getColor(getContext(), R.color.gray));
            textInputEndDate.setBoxStrokeColor(ContextCompat.getColor(getContext(), R.color.purple_700));
        }
    }

}