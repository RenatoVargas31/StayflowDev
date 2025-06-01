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
import com.iot.stayflowdev.adminHotel.MensajeDetalleActivity;
import com.iot.stayflowdev.adminHotel.model.Mensaje;

import java.util.List;

public class MensajeAdapter extends RecyclerView.Adapter<MensajeAdapter.MensajeViewHolder> {

    private List<Mensaje> mensajes;
    private Context context;

    public MensajeAdapter(List<Mensaje> mensajes, Context context) {
        this.mensajes = mensajes;
        this.context = context;
    }

    @NonNull
    @Override
    public MensajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mensaje, parent, false);
        return new MensajeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MensajeViewHolder holder, int position) {
        Mensaje mensaje = mensajes.get(position);
        holder.tvNombre.setText(mensaje.getNombre());
        holder.tvUltimoMensaje.setText(mensaje.getUltimoMensaje());
        holder.imgGuest.setImageResource(mensaje.getImagenResId());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MensajeDetalleActivity.class);
            intent.putExtra("nombre", mensaje.getNombre());
            intent.putExtra("ultimoMensaje", mensaje.getUltimoMensaje());
            intent.putExtra("imagenResId", mensaje.getImagenResId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    public static class MensajeViewHolder extends RecyclerView.ViewHolder {
        ImageView imgGuest;
        TextView tvNombre, tvUltimoMensaje;

        public MensajeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgGuest = itemView.findViewById(R.id.imgGuest);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvUltimoMensaje = itemView.findViewById(R.id.tvUltimoMensaje);
        }
    }
}
