package com.iot.stayflowdev.adminHotel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.model.Habitacion;

import java.util.List;

public class HabitacionAdapter extends RecyclerView.Adapter<HabitacionAdapter.HabitacionViewHolder> {

    private List<Habitacion> habitacionList;

    public HabitacionAdapter(List<Habitacion> habitacionList) {
        this.habitacionList = habitacionList;
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

    public void addHabitacion(Habitacion habitacion) {
        habitacionList.add(habitacion);
        notifyItemInserted(habitacionList.size() - 1);
    }

    static class HabitacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipo, tvCapacidad, tvTamano;

        HabitacionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvCapacidad = itemView.findViewById(R.id.tvCapacidad);
            tvTamano = itemView.findViewById(R.id.tvTamano);
        }
    }
}
