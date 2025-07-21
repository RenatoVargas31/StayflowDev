package com.iot.stayflowdev.Driver.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iot.stayflowdev.Driver.Dtos.SolicitudReserva;
import com.iot.stayflowdev.Driver.Model.ReservaModel;
import com.iot.stayflowdev.R;

import java.util.ArrayList;
import java.util.List;

public class ReservasAdapter  extends RecyclerView.Adapter<ReservasAdapter.ReservaViewHolder> {
    private List<SolicitudReserva> listaReservas;
    private OnReservaClickListener listener;
    private Context context;

    // Constructor
    public ReservasAdapter(Context context, List<SolicitudReserva> listaReservas, OnReservaClickListener listener) {
        this.context = context;
        this.listaReservas = listaReservas != null ? listaReservas : new ArrayList<>();
        this.listener = listener;
    }

    // Método para actualizar los datos
    public void actualizarDatos(List<SolicitudReserva> nuevasReservas) {
        this.listaReservas.clear();
        if (nuevasReservas != null) {
            this.listaReservas.addAll(nuevasReservas);
        }
        notifyDataSetChanged();
    }

    // Método para agregar una nueva reserva
    public void agregarReserva(SolicitudReserva reserva) {
        if (reserva != null) {
            this.listaReservas.add(0, reserva);
            notifyItemInserted(0);
        }
    }

    // Método para actualizar el estado de una reserva
    public void actualizarEstadoReserva(String reservaId, String nuevoEstado) {
        for (int i = 0; i < listaReservas.size(); i++) {
            if (listaReservas.get(i).getReservaId().equals(reservaId)) {
                listaReservas.get(i).setEstado(nuevoEstado);
                notifyItemChanged(i);
                break;
            }
        }
    }

    // Método para remover una reserva
    public void removerReserva(String reservaId) {
        for (int i = 0; i < listaReservas.size(); i++) {
            if (listaReservas.get(i).getReservaId().equals(reservaId)) {
                listaReservas.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_driver_reserva, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        SolicitudReserva reserva = listaReservas.get(position);

        // Configurar los datos
        holder.nombre.setText(reserva.getNombrePasajero() != null ?
                reserva.getNombrePasajero() : "Pasajero sin nombre");

        holder.origen.setText("De: " + (reserva.getOrigen() != null ?
                reserva.getOrigen() : "Origen no especificado"));

        holder.destino.setText("A: " + (reserva.getDestino() != null ?
                reserva.getDestino() : "Destino no especificado"));

        holder.distancia.setText(reserva.getDistanciaTexto());

        holder.fechaHora.setText(reserva.getFechaHora());

        // Configurar el clic
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

    // ViewHolder
    public static class ReservaViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, origen, destino, distancia, fechaHora;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.tv_nombre);
            origen = itemView.findViewById(R.id.tv_origen);
            destino = itemView.findViewById(R.id.tv_destino);
            distancia = itemView.findViewById(R.id.tv_distancia);
            fechaHora = itemView.findViewById(R.id.tv_fecha_hora);
        }
    }

    // Interface para clics
    public interface OnReservaClickListener {
        void onReservaClick(SolicitudReserva reserva);
    }

}
