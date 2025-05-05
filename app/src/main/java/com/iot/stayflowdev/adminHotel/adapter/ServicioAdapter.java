package com.iot.stayflowdev.adminHotel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iot.stayflowdev.adminHotel.model.Servicio;
import com.iot.stayflowdev.databinding.ItemAgregarServicioBinding;

import java.util.List;

public class ServicioAdapter extends RecyclerView.Adapter<ServicioAdapter.ServicioViewHolder> {

    private List<Servicio> listaServicios;

    public ServicioAdapter(List<Servicio> listaServicios) {
        this.listaServicios = listaServicios;
    }

    @NonNull
    @Override
    public ServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAgregarServicioBinding binding = ItemAgregarServicioBinding.inflate(inflater, parent, false);
        return new ServicioViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicioViewHolder holder, int position) {
        holder.bind(listaServicios.get(position));
    }

    @Override
    public int getItemCount() {
        return listaServicios.size();
    }

    public void updateData(List<Servicio> nuevosServicios) {
        this.listaServicios = nuevosServicios;
        notifyDataSetChanged();
    }

    static class ServicioViewHolder extends RecyclerView.ViewHolder {
        private final ItemAgregarServicioBinding binding;

        public ServicioViewHolder(ItemAgregarServicioBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Servicio servicio) {
            binding.textNombreServicio.setText(servicio.getNombre());

            if (servicio.getPorcentaje() > 0) {
                binding.progressServicio.setVisibility(View.VISIBLE);
                binding.progressServicio.setProgress(servicio.getPorcentaje());
                binding.textMontoServicio.setText(servicio.getMonto());
                binding.textDescripcionServicio.setVisibility(View.GONE);
                binding.textPrecioServicio.setVisibility(View.GONE);
            } else {
                binding.progressServicio.setVisibility(View.GONE);
                binding.textDescripcionServicio.setText(servicio.getDescripcion());
                binding.textPrecioServicio.setText(servicio.getPrecio());
                binding.textDescripcionServicio.setVisibility(View.VISIBLE);
                binding.textPrecioServicio.setVisibility(View.VISIBLE);
            }
        }
    }
}
