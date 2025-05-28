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
import com.iot.stayflowdev.adminHotel.huesped.CheckoutDetalleActivity;
import com.iot.stayflowdev.adminHotel.model.Checkout;

import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.ViewHolder> {

    private List<Checkout> checkoutList;
    private Context context;

    public CheckoutAdapter(List<Checkout> checkoutList, Context context) {
        this.checkoutList = checkoutList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Checkout checkout = checkoutList.get(position);
        holder.tvNombre.setText(checkout.getNombre());
        holder.tvCodigo.setText("Código de reserva: " + checkout.getCodigoReserva());
        holder.imgGuest.setImageResource(checkout.getImagenResId());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CheckoutDetalleActivity.class);
            intent.putExtra("nombre", checkout.getNombre());
            intent.putExtra("codigoReserva", checkout.getCodigoReserva());
            intent.putExtra("imagenResId", checkout.getImagenResId());
            intent.putExtra("montoHospedaje", checkout.getMontoHospedaje());
            intent.putExtra("tarjeta", checkout.getTarjeta());
            intent.putExtra("danios", checkout.getDanios());
            intent.putExtra("total", checkout.getTotal());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return checkoutList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgGuest;
        TextView tvNombre, tvCodigo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgGuest = itemView.findViewById(R.id.imgGuest);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvCodigo = itemView.findViewById(R.id.tvCodigo);
        }
    }
}
