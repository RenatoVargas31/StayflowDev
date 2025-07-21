package com.iot.stayflowdev.adminHotel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.model.UsuarioResumen;

import java.util.List;

public class UsuarioResumenAdapter extends RecyclerView.Adapter<UsuarioResumenAdapter.ViewHolder> {

    private List<UsuarioResumen> lista;

    public UsuarioResumenAdapter(List<UsuarioResumen> lista) {
        this.lista = lista;
    }

    public void setLista(List<UsuarioResumen> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario_resumen, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsuarioResumen usuario = lista.get(position);
        holder.textNombre.setText(usuario.getNombre());

        if (usuario.getMonto() > 0) {
            holder.textDetalle.setText(String.format("Total gastado: S/. %.2f", usuario.getMonto()));
        } else {
            holder.textDetalle.setText("Reservas: " + usuario.getCantidadReservas());
        }
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNombre, textDetalle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textNombreUsuario);
            textDetalle = itemView.findViewById(R.id.textCantidadReservas);  // mismo ID, cambia el contenido
        }
    }
}
