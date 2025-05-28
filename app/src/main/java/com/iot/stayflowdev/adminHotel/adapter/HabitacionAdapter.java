package com.iot.stayflowdev.adminHotel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.model.Habitacion;

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
        holder.tvCapacidad.setText("Capacidad: " + habitacion.getCapacidad() + " personas");
        holder.tvTamano.setText("Tamaño: " + habitacion.getTamano() + " m²");
        holder.tvPrecio.setText(String.format("Precio: S/. %.2f", habitacion.getPrecio()));

        // Habilitar/Deshabilitar botones según estado "enUso"
        if (habitacion.isEnUso()) {
            holder.btnEditar.setEnabled(false);
            holder.btnEliminar.setEnabled(false);
        } else {
            holder.btnEditar.setEnabled(true);
            holder.btnEliminar.setEnabled(true);
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
        TextView tvTipo, tvCapacidad, tvTamano, tvPrecio;
        MaterialButton btnEditar, btnEliminar;

        HabitacionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvCapacidad = itemView.findViewById(R.id.tvCapacidad);
            tvTamano = itemView.findViewById(R.id.tvTamano);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}
