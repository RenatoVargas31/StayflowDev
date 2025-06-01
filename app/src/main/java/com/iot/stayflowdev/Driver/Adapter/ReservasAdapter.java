package com.iot.stayflowdev.Driver.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iot.stayflowdev.Driver.Model.ReservaModel;
import com.iot.stayflowdev.R;

import java.util.List;

public class ReservasAdapter  extends RecyclerView.Adapter<ReservasAdapter.ReservaViewHolder> {

    private List<ReservaModel> listaReservas;
    private OnReservaClickListener listener;
    private Context context;
    // Constructor
    public ReservasAdapter(Context context, List<ReservaModel> listaReservas, OnReservaClickListener listener) {
        this.context = context;
        this.listaReservas = listaReservas;
        this.listener = listener;
    }

    // Método para actualizar los datos cuando cambia la pestaña
    public void actualizarDatos(List<ReservaModel> nuevasReservas) {
        this.listaReservas = nuevasReservas;
        notifyDataSetChanged();
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
        ReservaModel reserva = listaReservas.get(position);

        // Configurar los datos en las vistas
        holder.nombre.setText(reserva.getNombrePasajero());
        holder.origen.setText("De: " + reserva.getOrigen());
        holder.destino.setText("A: " + reserva.getDestino());
        holder.distancia.setText(reserva.getDistancia());
        holder.fechaHora.setText(reserva.getFechaHora());
        holder.icono.setImageResource(reserva.getTipoIcono());

        // Configurar el clic en la tarjeta
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReservaClick(reserva);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaReservas != null ? listaReservas.size() : 0;
    }

    // ViewHolder para mantener las referencias a las vistas
    public static class ReservaViewHolder extends RecyclerView.ViewHolder {
        ImageView icono;
        TextView nombre, origen, destino, distancia, fechaHora;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
           // icono = itemView.findViewById(R.id.iv_icon);
           // nombre = itemView.findViewById(R.id.tv_nombre);
            origen = itemView.findViewById(R.id.tv_origen);
            destino = itemView.findViewById(R.id.tv_destino);
          //  distancia = itemView.findViewById(R.id.tv_distancia);
            fechaHora = itemView.findViewById(R.id.tv_fecha_hora);
        }
    }

    // Interface para manejar clics
    public interface OnReservaClickListener {
        void onReservaClick(ReservaModel reserva);
    }

}
