package com.iot.stayflowdev.adminHotel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.model.Servicio;
import java.util.List;

public class ServicioAdapter extends RecyclerView.Adapter<ServicioAdapter.ServicioViewHolder> {

    private List<Servicio> listaServicios;
    private OnServicioActionListener actionListener;

    public ServicioAdapter(List<Servicio> listaServicios, OnServicioActionListener actionListener) {
        this.listaServicios = listaServicios;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_agregar_servicio, parent, false);
        return new ServicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicioViewHolder holder, int position) {
        Servicio servicio = listaServicios.get(position);
        holder.tvNombre.setText(servicio.getNombre());
        holder.tvDescripcion.setText(servicio.getDescripcion());
        holder.tvPrecio.setText(servicio.isEsGratis() ? "Gratis" : "S/. " + servicio.getPrecio());

        holder.btnEditar.setOnClickListener(v -> actionListener.onEditar(servicio));
        holder.btnEliminar.setOnClickListener(v -> actionListener.onEliminar(servicio));
    }

    @Override
    public int getItemCount() {
        return listaServicios.size();
    }

    public void updateData(List<Servicio> nuevosServicios) {
        this.listaServicios = nuevosServicios;
        notifyDataSetChanged();
    }

    public static class ServicioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDescripcion, tvPrecio;
        ImageButton btnEditar, btnEliminar;

        public ServicioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreServicio);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionServicio);
            tvPrecio = itemView.findViewById(R.id.tvPrecioServicio);
            btnEditar = itemView.findViewById(R.id.btnEditarServicio);
            btnEliminar = itemView.findViewById(R.id.btnEliminarServicio);
        }
    }

    public interface OnServicioActionListener {
        void onEditar(Servicio servicio);
        void onEliminar(Servicio servicio);
    }
}
