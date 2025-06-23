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
import com.iot.stayflowdev.adminHotel.huesped.InfoTaxiActivity;
import com.iot.stayflowdev.adminHotel.model.Taxi;

import java.util.List;

public class TaxiAdapter extends RecyclerView.Adapter<TaxiAdapter.TaxiViewHolder> {

    private List<Taxi> taxis;
    private Context context;

    public TaxiAdapter(List<Taxi> taxis, Context context) {
        this.taxis = taxis;
        this.context = context;
    }

    @NonNull
    @Override
    public TaxiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_taxi, parent, false);
        return new TaxiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxiViewHolder holder, int position) {
        Taxi taxi = taxis.get(position);

        // Mostrar nombre del pasajero
        holder.nombre.setText(taxi.getNombre() != null ? taxi.getNombre() : "Sin nombre");

        // Mostrar código de reserva correctamente
        String codigo = taxi.getCodigoReserva();
        if (codigo != null && !codigo.isEmpty()) {
            holder.codigo.setText("Código: " + codigo);
        } else {
            holder.codigo.setText("Sin código");
        }

        // Mostrar el estado con color personalizado y formato mejorado
        String estado = taxi.getEstadoTaxi();
        if (estado != null && !estado.isEmpty()) {
            holder.estadoTaxi.setText("Estado: " + capitalizeFirst(estado));
            int color = getColorForEstado(estado);
            holder.estadoTaxi.setTextColor(color);
        } else {
            holder.estadoTaxi.setText("Estado: Sin definir");
            holder.estadoTaxi.setTextColor(context.getResources().getColor(android.R.color.black, null));
        }

        // Mostrar detalles del viaje
        String detalleViaje = taxi.getDetalleViaje();
        if (detalleViaje != null && !detalleViaje.isEmpty()) {
            holder.detalleViaje.setText("Llegada: " + detalleViaje);
        } else {
            holder.detalleViaje.setText("Llegada: No especificada");
        }

        // Mostrar ruta
        String ruta = taxi.getRuta();
        if (ruta != null && !ruta.isEmpty()) {
            holder.ruta.setText("Ruta: " + ruta);
        } else {
            holder.ruta.setText("Ruta: No especificada");
        }

        // Mostrar imagen
        holder.imagen.setImageResource(taxi.getImagenResId());

        // Configurar click listener para ver información completa
        holder.infoViaje.setOnClickListener(v -> {
            Intent intent = new Intent(context, InfoTaxiActivity.class);
            intent.putExtra("nombre", taxi.getNombre());
            intent.putExtra("codigo", taxi.getCodigoReserva());
            intent.putExtra("estadoTaxi", taxi.getEstadoTaxi());
            intent.putExtra("detalleViaje", taxi.getDetalleViaje());
            intent.putExtra("ruta", taxi.getRuta());
            intent.putExtra("imagenResId", taxi.getImagenResId());
            context.startActivity(intent);
        });

        // También permitir click en todo el item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, InfoTaxiActivity.class);
            intent.putExtra("nombre", taxi.getNombre());
            intent.putExtra("codigo", taxi.getCodigoReserva());
            intent.putExtra("estadoTaxi", taxi.getEstadoTaxi());
            intent.putExtra("detalleViaje", taxi.getDetalleViaje());
            intent.putExtra("ruta", taxi.getRuta());
            intent.putExtra("imagenResId", taxi.getImagenResId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return taxis != null ? taxis.size() : 0;
    }

    /**
     * Obtiene el color apropiado según el estado del taxi
     */
    private int getColorForEstado(String estado) {
        if (estado == null) return context.getResources().getColor(android.R.color.black, null);

        switch (estado.toLowerCase().trim()) {
            case "aceptada":
                return context.getResources().getColor(android.R.color.holo_orange_dark, null);
            case "en camino":
                return context.getResources().getColor(android.R.color.holo_blue_dark, null);
            case "llegado":
                return context.getResources().getColor(android.R.color.holo_green_dark, null);
            case "finalizada":
                return context.getResources().getColor(android.R.color.darker_gray, null);
            case "cancelada":
                return context.getResources().getColor(android.R.color.holo_red_dark, null);
            default:
                return context.getResources().getColor(android.R.color.black, null);
        }
    }

    /**
     * Capitaliza la primera letra de un texto
     */
    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    /**
     * Actualiza la lista de taxis
     */
    public void updateTaxis(List<Taxi> newTaxis) {
        this.taxis = newTaxis;
        notifyDataSetChanged();
    }

    static class TaxiViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView nombre, codigo, estadoTaxi, infoViaje, detalleViaje, ruta;

        TaxiViewHolder(@NonNull View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imgGuest);
            nombre = itemView.findViewById(R.id.tvNombre);
            codigo = itemView.findViewById(R.id.tvCodigo);
            estadoTaxi = itemView.findViewById(R.id.tvEstadoTaxi);
            infoViaje = itemView.findViewById(R.id.tvInfoViaje);
            detalleViaje = itemView.findViewById(R.id.tvDetalleViaje);
            ruta = itemView.findViewById(R.id.tvRuta);
        }
    }
}