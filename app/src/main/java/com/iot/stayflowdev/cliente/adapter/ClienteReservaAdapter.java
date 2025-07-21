package com.iot.stayflowdev.cliente.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.Hotel;
import com.iot.stayflowdev.model.Reserva;
import com.iot.stayflowdev.viewmodels.ReservaViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class ClienteReservaAdapter extends RecyclerView.Adapter<ClienteReservaAdapter.ReservaViewHolder> {
    private final List<Reserva> reservas;
    private final Context context;
    private final ReservaViewModel viewModel;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public ClienteReservaAdapter(Context context, ReservaViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
        this.reservas = new ArrayList<>();
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cliente_reserva, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);

        // Configurar código de reserva usando el ID en lugar del código
        holder.codigoReserva.setText("Reserva #" + reserva.getId());

        // Configurar fechas de la reserva
        String fechasReserva = formatFechaReserva(reserva.getFechaInicio()) +
                               " - " +
                               formatFechaReserva(reserva.getFechaFin());
        holder.fechasReserva.setText(fechasReserva);

        // Configurar estado
        holder.estado.setText(reserva.getEstado());

        // Configurar visibilidad de taxi
        if (reserva.isQuieroTaxi()) {
            holder.iconoTaxi.setVisibility(View.VISIBLE);
            holder.textoTaxi.setVisibility(View.VISIBLE);
        } else {
            holder.iconoTaxi.setVisibility(View.GONE);
            holder.textoTaxi.setVisibility(View.GONE);
        }

        // Obtener y configurar nombre del hotel
        obtenerNombreHotel(reserva, hotel -> {
            if (hotel != null) {
                holder.nombreHotel.setText(hotel.getNombre());
            } else {
                holder.nombreHotel.setText("Hotel no disponible");
            }
        });
    }

    /**
     * Obtiene el nombre del hotel asociado a una reserva
     */
    private void obtenerNombreHotel(Reserva reserva, OnHotelLoadedListener listener) {
        CompletableFuture<Hotel> hotelFuture = viewModel.obtenerHotelParaReserva(reserva);
        hotelFuture.thenAccept(listener::onHotelLoaded)
                  .exceptionally(e -> {
                      listener.onHotelLoaded(null);
                      return null;
                  });
    }

    /**
     * Actualiza la lista de reservas y notifica los cambios
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setReservas(List<Reserva> nuevasReservas) {
        this.reservas.clear();
        if (nuevasReservas != null) {
            this.reservas.addAll(nuevasReservas);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    /**
     * Formatea un timestamp de Firestore a un formato legible
     */
    private String formatFechaReserva(Timestamp timestamp) {
        if (timestamp == null) return "Fecha no disponible";
        Date date = timestamp.toDate();
        return sdf.format(date);
    }

    /**
     * ViewHolder para los items de reserva
     */
    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        final TextView codigoReserva;
        final TextView nombreHotel;
        final TextView fechasReserva;
        final TextView estado;
        final ImageView iconoTaxi;
        final TextView textoTaxi;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            codigoReserva = itemView.findViewById(R.id.textViewCodigoReserva);
            nombreHotel = itemView.findViewById(R.id.textViewNombreHotel);
            fechasReserva = itemView.findViewById(R.id.textViewFechasReserva);
            estado = itemView.findViewById(R.id.textViewEstado);
            iconoTaxi = itemView.findViewById(R.id.imageViewTaxi);
            textoTaxi = itemView.findViewById(R.id.textViewTaxi);
        }
    }

    /**
     * Interfaz para manejar la carga asíncrona de hoteles
     */
    interface OnHotelLoadedListener {
        void onHotelLoaded(Hotel hotel);
    }
}
