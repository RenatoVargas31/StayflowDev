package com.iot.stayflowdev.Driver.Activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.iot.stayflowdev.Driver.Repository.TarjetaCreditoRepository;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.TarjetaCredito;

public class AgregarTarjetaDialogFragment extends DialogFragment {

    private static final String TAG = "AgregarTarjetaDialog";
    private static final String ARG_USER_ID = "user_id";

    // Views
    private EditText etNumeroTarjeta;
    private EditText etTitular;
    private EditText etFechaExpiracion;
    private EditText etCvv;
    private TextView tvTipoTarjeta;
    private ImageView ivTipoTarjeta;

    private String userId;
    private TarjetaCreditoRepository tarjetaRepository;
    private OnTarjetaAgregadaListener listener;

    // Interface para callback
    public interface OnTarjetaAgregadaListener {
        void onTarjetaAgregada(TarjetaCredito tarjeta);
    }

    public static AgregarTarjetaDialogFragment newInstance(String userId) {
        AgregarTarjetaDialogFragment fragment = new AgregarTarjetaDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        }
        tarjetaRepository = new TarjetaCreditoRepository();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = crearVistaFormulario();
        configurarEventos();

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .setTitle("Agregar Tarjeta")
                .setPositiveButton("Guardar", null) // Se configurará después
                .setNegativeButton("Cancelar", (dialog, which) -> dismiss())
                .create();
    }

    private View crearVistaFormulario() {
        // Crear vista programáticamente
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        // Tipo de tarjeta
        LinearLayout tipoLayout = new LinearLayout(requireContext());
        tipoLayout.setOrientation(LinearLayout.HORIZONTAL);
        tipoLayout.setGravity(Gravity.CENTER_VERTICAL);

        ivTipoTarjeta = new ImageView(requireContext());
        ivTipoTarjeta.setImageResource(R.drawable.ic_credit_card);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(120, 80);
        imgParams.rightMargin = 24;
        ivTipoTarjeta.setLayoutParams(imgParams);

        tvTipoTarjeta = new TextView(requireContext());
        tvTipoTarjeta.setText("Tarjeta");
        tvTipoTarjeta.setTextSize(16);
        tvTipoTarjeta.setTypeface(null, Typeface.BOLD);

        tipoLayout.addView(ivTipoTarjeta);
        tipoLayout.addView(tvTipoTarjeta);
        layout.addView(tipoLayout);

        // Número de tarjeta
        TextInputLayout tilNumero = new TextInputLayout(requireContext());
        tilNumero.setHint("Número de tarjeta");
        tilNumero.setHelperText("16 dígitos");

        etNumeroTarjeta = new TextInputEditText(requireContext());
        etNumeroTarjeta.setInputType(InputType.TYPE_CLASS_NUMBER);
        etNumeroTarjeta.setTypeface(Typeface.MONOSPACE);
        etNumeroTarjeta.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        tilNumero.addView(etNumeroTarjeta);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 24;
        params.bottomMargin = 16;
        tilNumero.setLayoutParams(params);

        layout.addView(tilNumero);

        // Titular
        TextInputLayout tilTitular = new TextInputLayout(requireContext());
        tilTitular.setHint("Nombre del titular");
        tilTitular.setHelperText("Como aparece en la tarjeta");

        etTitular = new TextInputEditText(requireContext());
        etTitular.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        etTitular.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        tilTitular.addView(etTitular);
        tilTitular.setLayoutParams(params);
        layout.addView(tilTitular);

        // Layout horizontal para fecha y CVV
        LinearLayout fechaCvvLayout = new LinearLayout(requireContext());
        fechaCvvLayout.setOrientation(LinearLayout.HORIZONTAL);
        fechaCvvLayout.setLayoutParams(params);

        // Fecha de expiración
        TextInputLayout tilFecha = new TextInputLayout(requireContext());
        tilFecha.setHint("MM/AA");
        tilFecha.setHelperText("Expiración");

        etFechaExpiracion = new TextInputEditText(requireContext());
        etFechaExpiracion.setInputType(InputType.TYPE_CLASS_NUMBER);
        etFechaExpiracion.setTypeface(Typeface.MONOSPACE);
        etFechaExpiracion.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        tilFecha.addView(etFechaExpiracion);

        LinearLayout.LayoutParams fechaParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        fechaParams.weight = 1;
        fechaParams.rightMargin = 16;
        tilFecha.setLayoutParams(fechaParams);

        // CVV
        TextInputLayout tilCvv = new TextInputLayout(requireContext());
        tilCvv.setHint("CVV");
        tilCvv.setHelperText("3-4 dígitos");

        etCvv = new TextInputEditText(requireContext());
        etCvv.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        etCvv.setTypeface(Typeface.MONOSPACE);
        etCvv.setImeOptions(EditorInfo.IME_ACTION_DONE);

        tilCvv.addView(etCvv);

        LinearLayout.LayoutParams cvvParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        cvvParams.weight = 1;
        tilCvv.setLayoutParams(cvvParams);

        fechaCvvLayout.addView(tilFecha);
        fechaCvvLayout.addView(tilCvv);
        layout.addView(fechaCvvLayout);

        // Mensaje de seguridad
        TextView tvSeguridad = new TextView(requireContext());
        tvSeguridad.setText("Tu información está protegida con encriptación");
        tvSeguridad.setTextSize(12);
        tvSeguridad.setTextColor(Color.parseColor("#757575"));
        tvSeguridad.setPadding(0, 24, 0, 0);
        layout.addView(tvSeguridad);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Configurar el botón positivo después de que se cree el diálogo
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> validarYGuardarTarjeta());
        }
    }

    private void configurarEventos() {
        // Máscara para número de tarjeta
        etNumeroTarjeta.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;

                isUpdating = true;
                String text = s.toString().replaceAll("\\s+", "");
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < text.length() && i < 16; i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(text.charAt(i));
                }

                etNumeroTarjeta.setText(formatted.toString());
                etNumeroTarjeta.setSelection(formatted.length());

                // Detectar tipo de tarjeta
                detectarTipoTarjeta(text);

                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Máscara para fecha
        etFechaExpiracion.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;

                isUpdating = true;
                String text = s.toString().replaceAll("/", "");

                if (text.length() >= 2) {
                    text = text.substring(0, 2) + "/" + text.substring(2, Math.min(4, text.length()));
                }

                etFechaExpiracion.setText(text);
                etFechaExpiracion.setSelection(text.length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Limitar CVV a 4 dígitos
        etCvv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 4) {
                    etCvv.setText(s.toString().substring(0, 4));
                    etCvv.setSelection(4);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void detectarTipoTarjeta(String numero) {
        if (numero.startsWith("4")) {
            ivTipoTarjeta.setImageResource(R.drawable.ic_visa_logo);
            tvTipoTarjeta.setText("Visa");
        } else if (numero.startsWith("5") || numero.startsWith("2")) {
            ivTipoTarjeta.setImageResource(R.drawable.ic_mastercard_logo);
            tvTipoTarjeta.setText("Mastercard");
        } else {
            ivTipoTarjeta.setImageResource(R.drawable.ic_credit_card);
            tvTipoTarjeta.setText("Tarjeta");
        }
    }

    private void validarYGuardarTarjeta() {
        String numero = etNumeroTarjeta.getText().toString().replaceAll("\\s+", "");
        String titular = etTitular.getText().toString().trim();
        String fecha = etFechaExpiracion.getText().toString();
        String cvv = etCvv.getText().toString();

        // Validaciones básicas
        if (numero.length() < 13) {
            Toast.makeText(getContext(), "Número de tarjeta inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (titular.isEmpty()) {
            Toast.makeText(getContext(), "El nombre del titular es requerido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!fecha.matches("\\d{2}/\\d{2}")) {
            Toast.makeText(getContext(), "Formato de fecha inválido (MM/AA)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cvv.length() < 3) {
            Toast.makeText(getContext(), "CVV inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear objeto tarjeta
        String[] fechaParts = fecha.split("/");
        String mes = fechaParts[0];
        String anio = "20" + fechaParts[1];

        TarjetaCredito tarjeta = new TarjetaCredito(numero, titular, mes, anio, cvv);

        if (!tarjeta.esValida()) {
            Toast.makeText(getContext(), "Los datos de la tarjeta no son válidos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar tarjeta
        guardarTarjeta(tarjeta);
    }

    private void guardarTarjeta(TarjetaCredito tarjeta) {
        // Deshabilitar botón mientras guarda
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setEnabled(false);
            positiveButton.setText("Guardando...");
        }

        tarjetaRepository.agregarTarjeta(tarjeta,
                documentId -> {
                    Log.d(TAG, "Tarjeta guardada exitosamente");

                    if (listener != null) {
                        listener.onTarjetaAgregada(tarjeta);
                    }

                    dismiss();
                },
                error -> {
                    Log.e(TAG, "Error al guardar tarjeta: " + error.getMessage());

                    // Rehabilitar botón
                    if (dialog != null) {
                        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        positiveButton.setEnabled(true);
                        positiveButton.setText("Guardar");
                    }

                    Toast.makeText(getContext(), "Error al guardar la tarjeta", Toast.LENGTH_SHORT).show();
                }
        );
    }

    public void setOnTarjetaAgregadaListener(OnTarjetaAgregadaListener listener) {
        this.listener = listener;
    }
}