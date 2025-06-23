package com.iot.stayflowdev.adminHotel.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.huesped.DetalleReservaActivity;
import com.iot.stayflowdev.model.Reserva;
import com.iot.stayflowdev.model.Reserva.CantHuespedes;
import com.iot.stayflowdev.model.Reserva.Habitacion;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder> {

    private final List<Reserva> reservas;
    private final Context context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

    public ReservaAdapter(List<Reserva> reservas, Context context) {
        this.reservas = reservas;
        this.context = context;
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reserva, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);

        // Imagen fija por ahora
        holder.imagen.setImageResource(R.drawable.img_guest_placeholder);

        // Código de la reserva - Mostrar el ID completo de la reserva como código
        if (reserva.getId() != null && !reserva.getId().isEmpty()) {
            holder.nombre.setText("Reserva " + reserva.getId());
        } else {
            holder.nombre.setText("Reserva sin código");
        }

        // Tipo de habitación
        List<Habitacion> habitaciones = reserva.getHabitaciones();
        if (habitaciones != null && !habitaciones.isEmpty()) {
            Habitacion primeraHabitacion = habitaciones.get(0);
            String tipoHabitacion = primeraHabitacion.getTipo();

            // Si hay múltiples habitaciones, indicarlo
            if (habitaciones.size() > 1) {
                holder.habitacion.setText(tipoHabitacion + " (+" + (habitaciones.size() - 1) + " más)");
            } else {
                holder.habitacion.setText("Habitación " + tipoHabitacion);
            }
        } else {
            holder.habitacion.setText("Habitación -");
        }

        // Detalle de personas
        CantHuespedes ch = reserva.getCantHuespedes();
        if (ch != null) {
            // Convertir String a int con manejo de errores
            int adultos = 0;
            int ninos = 0;

            try {
                adultos = Integer.parseInt(ch.getAdultos());
            } catch (NumberFormatException e) {
                adultos = 0; // valor por defecto si no se puede convertir
            }

            try {
                ninos = Integer.parseInt(ch.getNinos());
            } catch (NumberFormatException e) {
                ninos = 0; // valor por defecto si no se puede convertir
            }

            String detalle = adultos + " adulto" + (adultos > 1 ? "s" : "");
            if (ninos > 0) detalle += " + " + ninos + " niño" + (ninos > 1 ? "s" : "");
            holder.detallePersonas.setText(detalle);
        } else {
            holder.detallePersonas.setText("-");
        }

        // Fechas
        String fechaInicio = reserva.getFechaInicio() != null ? dateFormat.format(reserva.getFechaInicio().toDate()) : "-";
        String fechaFin = reserva.getFechaFin() != null ? dateFormat.format(reserva.getFechaFin().toDate()) : "-";
        holder.fechas.setText(" • " + fechaInicio + " - " + fechaFin);

        // Botón de más información - ACTUALIZADO PARA PASAR SOLO EL ID
        holder.masInfo.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleReservaActivity.class);
            intent.putExtra("idReserva", reserva.getId()); // Solo pasamos el ID
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView nombre, habitacion, detallePersonas, masInfo, fechas;

        ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imgGuest);
            nombre = itemView.findViewById(R.id.tvNombre);
            habitacion = itemView.findViewById(R.id.tvHabitacion);
            detallePersonas = itemView.findViewById(R.id.tvDetallePersonas);
            masInfo = itemView.findViewById(R.id.tvMasInfo);
            fechas = itemView.findViewById(R.id.tvFechas);
        }
    }
}