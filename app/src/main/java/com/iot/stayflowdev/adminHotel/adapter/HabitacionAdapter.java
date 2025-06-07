package com.iot.stayflowdev.adminHotel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.Habitacion;

import java.util.List;

public class HabitacionAdapter extends RecyclerView.Adapter<HabitacionAdapter.HabitacionViewHolder> {

    private List<Habitacion> habitacionList;
    private OnHabitacionActionListener actionListener;

    public HabitacionAdapter(List<Habitacion> habitacionList, OnHabitacionActionListener listener) {
        this.habitacionList = habitacionList;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public HabitacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habitacion, parent, false);
        return new HabitacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitacionViewHolder holder, int position) {
        Habitacion habitacion = habitacionList.get(position);
        holder.tvTipo.setText(habitacion.getTipo());
        holder.tvCapacidad.setText("Capacidad: " + habitacion.getCapacidad().getAdultos()
                + " adultos, " + habitacion.getCapacidad().getNinos() + " niños");
        holder.tvTamano.setText("Tamaño: " + habitacion.getTamano() + " m²");
        holder.tvPrecio.setText("Precio: S/. " + habitacion.getPrecio());
        holder.tvCantidad.setText("Cantidad de habitaciones: " + habitacion.getCantidad());


        // Habilitar/Deshabilitar botones según estado "Disponible"
        if (Boolean.TRUE.equals(habitacion.getDisponible())) {
            holder.btnEditar.setEnabled(true);
            holder.btnEliminar.setEnabled(true);
        } else {
            holder.btnEditar.setEnabled(false);
            holder.btnEliminar.setEnabled(false);
        }

        // Acciones de botones
        holder.btnEditar.setOnClickListener(v -> actionListener.onEditar(habitacion, position));
        holder.btnEliminar.setOnClickListener(v -> actionListener.onEliminar(habitacion, position));
    }

    @Override
    public int getItemCount() {
        return habitacionList.size();
    }

    public void updateData(List<Habitacion> newList) {
        habitacionList.clear();
        habitacionList.addAll(newList);
        notifyDataSetChanged();
    }

    public interface OnHabitacionActionListener {
        void onEditar(Habitacion habitacion, int position);
        void onEliminar(Habitacion habitacion, int position);
    }

    static class HabitacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipo, tvCapacidad, tvTamano, tvPrecio, tvCantidad;
        MaterialButton btnEditar, btnEliminar;

        HabitacionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvCapacidad = itemView.findViewById(R.id.tvCapacidad);
            tvTamano = itemView.findViewById(R.id.tvTamano);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
    public List<Habitacion> getHabitaciones() {
        return habitacionList;
    }
}
