package com.iot.stayflowdev.adminHotel.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iot.stayflowdev.adminHotel.model.Usuario;
import com.iot.stayflowdev.databinding.ItemUsuarioBinding;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private List<Usuario> listaUsuarios;

    public UsuarioAdapter(List<Usuario> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemUsuarioBinding binding = ItemUsuarioBinding.inflate(inflater, parent, false);
        return new UsuarioViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        holder.bind(listaUsuarios.get(position));
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public void updateData(List<Usuario> nuevosUsuarios) {
        this.listaUsuarios = nuevosUsuarios;
        notifyDataSetChanged();
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        private final ItemUsuarioBinding binding;

        public UsuarioViewHolder(ItemUsuarioBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Usuario usuario) {
            binding.textNombreUsuario.setText(usuario.getNombre());
            binding.progressUsuario.setProgress(usuario.getPorcentaje());
            binding.textMontoUsuario.setText(usuario.getMonto());
        }
    }
}
