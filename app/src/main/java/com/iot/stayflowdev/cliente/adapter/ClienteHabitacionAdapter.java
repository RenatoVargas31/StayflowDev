package com.iot.stayflowdev.cliente.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.Habitacion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClienteHabitacionAdapter extends RecyclerView.Adapter<ClienteHabitacionAdapter.HabitacionViewHolder> {

    private static final String TAG = "ClienteHabitacionAdapter";
    private List<Habitacion> habitaciones = new ArrayList<>();
    private Map<String, Integer> selecciones = new HashMap<>(); // ID -> cantidad seleccionada
    private OnHabitacionSeleccionadaListener listener;

    public interface OnHabitacionSeleccionadaListener {
        void onSeleccionCambiada(String habitacionId, int cantidad, double subtotal);
        void onTotalCambiado(double total, Map<String, Integer> selecciones);
    }

    public ClienteHabitacionAdapter(OnHabitacionSeleccionadaListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HabitacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cliente_habitacion, parent, false);
        return new HabitacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitacionViewHolder holder, int position) {
        Habitacion habitacion = habitaciones.get(position);
        holder.bind(habitacion);
    }

    @Override
    public int getItemCount() {
        return habitaciones.size();
    }

    /**
     * Actualiza la lista de habitaciones
     */
    public void setHabitaciones(List<Habitacion> nuevasHabitaciones) {
        this.habitaciones.clear();
        this.selecciones.clear();

        if (nuevasHabitaciones != null) {
            this.habitaciones.addAll(nuevasHabitaciones);
        }

        notifyDataSetChanged();
        notificarCambioTotal();
    }

    /**
     * Obtiene las selecciones actuales
     */
    public Map<String, Integer> getSelecciones() {
        return new HashMap<>(selecciones);
    }

    /**
     * Calcula el total actual
     */
    public double calcularTotal() {
        double total = 0.0;

        for (Map.Entry<String, Integer> entry : selecciones.entrySet()) {
            String habitacionId = entry.getKey();
            int cantidad = entry.getValue();

            Habitacion habitacion = encontrarHabitacionPorId(habitacionId);
            if (habitacion != null) {
                try {
                    double precio = Double.parseDouble(habitacion.getPrecio());
                    total += precio * cantidad;
                } catch (NumberFormatException e) {
                    // Ignorar precios inválidos
                }
            }
        }

        return total;
    }

    /**
     * Busca una habitación por ID
     */
    private Habitacion encontrarHabitacionPorId(String id) {
        for (Habitacion hab : habitaciones) {
            if (id.equals(hab.getId())) {
                return hab;
            }
        }
        return null;
    }

    /**
     * Notifica cambio en el total
     */
    private void notificarCambioTotal() {
        if (listener != null) {
            double total = calcularTotal();
            listener.onTotalCambiado(total, getSelecciones());
        }
    }

    class HabitacionViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivHabitacion;
        private TextView tvTipoHabitacion;
        private TextView tvPrecio;
        private TextView tvCapacidad;
        private TextView tvTamano;
        private TextView tvDisponibles;
        private TextInputEditText etCantidad;
        private TextView tvSubtotal;

        private Habitacion habitacionActual;

        public HabitacionViewHolder(@NonNull View itemView) {
            super(itemView);

            ivHabitacion = itemView.findViewById(R.id.ivHabitacion);
            tvTipoHabitacion = itemView.findViewById(R.id.tvTipoHabitacion);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvCapacidad = itemView.findViewById(R.id.tvCapacidad);
            tvTamano = itemView.findViewById(R.id.tvTamano);
            tvDisponibles = itemView.findViewById(R.id.tvDisponibles);
            etCantidad = itemView.findViewById(R.id.etCantidad);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
        }

        public void bind(Habitacion habitacion) {
            this.habitacionActual = habitacion;

            // Información básica
            tvTipoHabitacion.setText(habitacion.getTipo());
            tvPrecio.setText("S/. " + habitacion.getPrecio());

            // Capacidad
            if (habitacion.getCapacidad() != null) {
                String capacidadTexto = habitacion.getCapacidad().getAdultos() + " adulto";
                if (Integer.parseInt(habitacion.getCapacidad().getAdultos()) > 1) {
                    capacidadTexto += "s";
                }

                int ninos = Integer.parseInt(habitacion.getCapacidad().getNinos());
                if (ninos > 0) {
                    capacidadTexto += ", " + ninos + " niño" + (ninos > 1 ? "s" : "");
                }

                tvCapacidad.setText(capacidadTexto);
            } else {
                tvCapacidad.setText("Capacidad no especificada");
            }

            // Tamaño
            tvTamano.setText(habitacion.getTamano() + " m²");

            // Disponibles
            tvDisponibles.setText(habitacion.getCantidad() + " disponibles");

            // Imagen
            if (habitacion.getFoto() != null && !habitacion.getFoto().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(habitacion.getFoto())
                        .placeholder(R.drawable.ic_hotel)
                        .error(R.drawable.ic_hotel)
                        .centerCrop()
                        .into(ivHabitacion);
            } else {
                ivHabitacion.setImageResource(R.drawable.ic_hotel);
            }

            // Configurar cantidad
            configurarCampoGiCantidad();

            // Restaurar selección previa si existe
            Integer cantidadPrevia = selecciones.get(habitacion.getId());
            if (cantidadPrevia != null && cantidadPrevia > 0) {
                etCantidad.setText(String.valueOf(cantidadPrevia));
                actualizarSubtotal(cantidadPrevia);
            } else {
                etCantidad.setText("0");
                actualizarSubtotal(0);
            }
        }

        private void configurarCampoGiCantidad() {
            // Remover listener previo para evitar bucles
            etCantidad.setOnFocusChangeListener(null);

            etCantidad.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    procesarCambioCantidad(s.toString());
                }
            });
        }

        private void procesarCambioCantidad(String cantidadStr) {
            try {
                int cantidad = 0;

                if (!cantidadStr.trim().isEmpty()) {
                    cantidad = Integer.parseInt(cantidadStr.trim());
                }

                // Validar límites
                int maxDisponible = Integer.parseInt(habitacionActual.getCantidad());
                if (cantidad > maxDisponible) {
                    cantidad = maxDisponible;
                    etCantidad.setText(String.valueOf(cantidad));
                    etCantidad.setSelection(etCantidad.getText().length());
                }

                if (cantidad < 0) {
                    cantidad = 0;
                    etCantidad.setText("0");
                }

                // Actualizar selección
                if (cantidad > 0) {
                    selecciones.put(habitacionActual.getId(), cantidad);
                } else {
                    selecciones.remove(habitacionActual.getId());
                }

                // Actualizar subtotal
                actualizarSubtotal(cantidad);

                // Notificar cambio
                if (listener != null) {
                    double precio = Double.parseDouble(habitacionActual.getPrecio());
                    double subtotal = precio * cantidad;
                    listener.onSeleccionCambiada(habitacionActual.getId(), cantidad, subtotal);
                }

                // Notificar cambio total
                notificarCambioTotal();

            } catch (NumberFormatException e) {
                // Si no es un número válido, mantener 0
                etCantidad.setText("0");
                selecciones.remove(habitacionActual.getId());
                actualizarSubtotal(0);
                notificarCambioTotal();
            }
        }

        private void actualizarSubtotal(int cantidad) {
            if (cantidad > 0) {
                try {
                    double precio = Double.parseDouble(habitacionActual.getPrecio());
                    double subtotal = precio * cantidad;
                    tvSubtotal.setText(String.format("Subtotal: S/. %.2f", subtotal));
                    tvSubtotal.setVisibility(View.VISIBLE);
                } catch (NumberFormatException e) {
                    tvSubtotal.setVisibility(View.GONE);
                }
            } else {
                tvSubtotal.setVisibility(View.GONE);
            }
        }
    }
}
