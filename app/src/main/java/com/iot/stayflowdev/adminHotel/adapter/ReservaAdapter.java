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
import com.iot.stayflowdev.adminHotel.model.Reserva;

import java.util.List;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder> {

    private List<Reserva> reservas;
    private Context context;

    public ReservaAdapter(List<Reserva> reservas, Context context) {
        this.reservas = reservas;
        this.context = context;
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reserva, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);
        holder.nombre.setText(reserva.getNombre());
        holder.habitacion.setText(reserva.getHabitacion());
        holder.detallePersonas.setText(reserva.getDetallePersonas());
        holder.imagen.setImageResource(reserva.getImagenResId());

        holder.masInfo.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleReservaActivity.class);
            intent.putExtra("nombre", reserva.getNombre());
            intent.putExtra("habitacion", reserva.getHabitacion());
            intent.putExtra("codigo", reserva.getCodigoReserva());
            intent.putExtra("llegada", reserva.getFechaLlegada());
            intent.putExtra("salida", reserva.getFechaSalida());
            intent.putExtra("huespedes", reserva.getDetallePersonas());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView nombre, habitacion, detallePersonas, masInfo;

        ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imgGuest);
            nombre = itemView.findViewById(R.id.tvNombre);
            habitacion = itemView.findViewById(R.id.tvHabitacion);
            detallePersonas = itemView.findViewById(R.id.tvDetallePersonas);
            masInfo = itemView.findViewById(R.id.tvMasInfo);
        }
    }
}
