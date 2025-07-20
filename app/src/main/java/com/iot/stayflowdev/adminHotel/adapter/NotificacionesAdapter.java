package com.iot.stayflowdev.adminHotel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.model.NotificacionCheckout;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificacionesAdapter extends RecyclerView.Adapter<NotificacionesAdapter.ViewHolder> {

    private List<NotificacionCheckout> notificaciones;
    private OnNotificacionClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

    public interface OnNotificacionClickListener {
        void onNotificacionClick(NotificacionCheckout notificacion);
    }

    public NotificacionesAdapter(List<NotificacionCheckout> notificaciones,
                                 OnNotificacionClickListener listener) {
        this.notificaciones = notificaciones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacion_checkout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificacionCheckout notificacion = notificaciones.get(position);

        holder.tvTitulo.setText(notificacion.getTituloNotificacion());
        holder.tvMensaje.setText(notificacion.getMensajeNotificacion());
        holder.tvFecha.setText(dateFormat.format(notificacion.getFechaCheckoutAsDate()));
        holder.tvMonto.setText("S/. " + notificacion.getCostoTotal());

        // Mostrar estado de la reserva (solo si existe el TextView)
        if (holder.tvEstado != null) {
            holder.tvEstado.setText("SIN CHECKOUT");

            // Color rojo para indicar que necesita checkout
            holder.tvEstado.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_light)
            );
        }

        // Configurar color según prioridad
        int colorResId = notificacion.getColorPrioridad();
        holder.viewIndicador.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), colorResId)
        );

        // Configurar apariencia según si está leída
        if (notificacion.isLeida()) {
            holder.cardView.setAlpha(0.7f);
            holder.tvTitulo.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray)
            );
        } else {
            holder.cardView.setAlpha(1.0f);
            holder.tvTitulo.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black)
            );
        }

        // Configurar click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificacionClick(notificacion);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificaciones != null ? notificaciones.size() : 0;
    }

    public void actualizarNotificaciones(List<NotificacionCheckout> nuevasNotificaciones) {
        this.notificaciones = nuevasNotificaciones;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        View viewIndicador;
        TextView tvTitulo;
        TextView tvMensaje;
        TextView tvFecha;
        TextView tvMonto;
        TextView tvEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            viewIndicador = itemView.findViewById(R.id.view_indicador);
            tvTitulo = itemView.findViewById(R.id.tv_titulo);
            tvMensaje = itemView.findViewById(R.id.tv_mensaje);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            tvMonto = itemView.findViewById(R.id.tv_monto);
            tvEstado = itemView.findViewById(R.id.tv_estado);
        }
    }
}