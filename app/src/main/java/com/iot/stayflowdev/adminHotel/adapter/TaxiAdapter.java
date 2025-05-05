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
        holder.nombre.setText(taxi.getNombre());
        holder.codigo.setText("Código de reserva: " + taxi.getCodigoReserva());
        holder.estadoTaxi.setText("Taxi: " + taxi.getEstadoTaxi());
        holder.detalleViaje.setText("Llegada: " + taxi.getDetalleViaje());
        holder.ruta.setText("Ruta: " + taxi.getRuta());
        holder.imagen.setImageResource(taxi.getImagenResId());

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
    }

    @Override
    public int getItemCount() {
        return taxis.size();
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
