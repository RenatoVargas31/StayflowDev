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
import com.iot.stayflowdev.model.Reserva;

import java.util.List;
import java.util.ArrayList;

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
        holder.tvCodigo.setText("Código: " + checkout.getCodigoReserva());
        holder.imgGuest.setImageResource(checkout.getImagenResId());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CheckoutDetalleActivity.class);
            intent.putExtra("nombre", checkout.getNombre());
            intent.putExtra("codigoReserva", checkout.getCodigoReserva());
            intent.putExtra("imagenResId", checkout.getImagenResId());
            intent.putExtra("tarjeta", checkout.getTarjeta());

            // Pasar la reserva completa como JSON string (usando Gson si está disponible)
            // O mejor aún, pasar los datos individuales
            if (checkout.getReservaCompleta() != null) {
                Reserva reserva = checkout.getReservaCompleta();
                intent.putExtra("costoTotal", reserva.getCostoTotal());

                // Pasar habitaciones como arrays
                if (reserva.getHabitaciones() != null) {
                    String[] tiposHab = new String[reserva.getHabitaciones().size()];
                    int[] cantidadesHab = new int[reserva.getHabitaciones().size()];
                    String[] preciosHab = new String[reserva.getHabitaciones().size()];

                    for (int i = 0; i < reserva.getHabitaciones().size(); i++) {
                        tiposHab[i] = reserva.getHabitaciones().get(i).getTipo();
                        cantidadesHab[i] = reserva.getHabitaciones().get(i).getCantidad();
                        preciosHab[i] = reserva.getHabitaciones().get(i).getPrecio();
                    }

                    intent.putExtra("tiposHabitaciones", tiposHab);
                    intent.putExtra("cantidadesHabitaciones", cantidadesHab);
                    intent.putExtra("preciosHabitaciones", preciosHab);
                }

                // Pasar servicios como arrays
                if (reserva.getServicios() != null) {
                    String[] nombresServ = new String[reserva.getServicios().size()];
                    String[] preciosServ = new String[reserva.getServicios().size()];
                    String[] descripcionesServ = new String[reserva.getServicios().size()];

                    for (int i = 0; i < reserva.getServicios().size(); i++) {
                        nombresServ[i] = reserva.getServicios().get(i).getNombre();
                        preciosServ[i] = reserva.getServicios().get(i).getPrecio();
                        descripcionesServ[i] = reserva.getServicios().get(i).getDescripcion();
                    }

                    intent.putExtra("nombresServicios", nombresServ);
                    intent.putExtra("preciosServicios", preciosServ);
                    intent.putExtra("descripcionesServicios", descripcionesServ);
                }

                // Pasar daños existentes
                if (reserva.getDanios() != null) {
                    String[] descripcionesDanios = new String[reserva.getDanios().size()];
                    String[] preciosDanios = new String[reserva.getDanios().size()];

                    for (int i = 0; i < reserva.getDanios().size(); i++) {
                        descripcionesDanios[i] = reserva.getDanios().get(i).getDescripcion();
                        preciosDanios[i] = reserva.getDanios().get(i).getPrecio();
                    }

                    intent.putExtra("descripcionesDaniosExistentes", descripcionesDanios);
                    intent.putExtra("preciosDaniosExistentes", preciosDanios);
                }
            }

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

    public void clearItems() {
        checkoutList.clear();
        notifyDataSetChanged();
    }

    public void addItem(Checkout item) {
        checkoutList.add(item);
        notifyItemInserted(checkoutList.size() - 1);
    }

    // ===== NUEVO MÉTODO PARA FILTROS =====
    public void updateItems(List<Checkout> nuevosCheckouts) {
        this.checkoutList.clear();
        this.checkoutList.addAll(nuevosCheckouts);
        notifyDataSetChanged();
    }
}