package com.iot.stayflowdev;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class NotificacionAdapter extends RecyclerView.Adapter<NotificacionAdapter.NotificacionViewHolder> {

    // Constantes para los tipos de notificación
    public static final int TIPO_VIAJE_ACEPTADO = 1;
    public static final int TIPO_VIAJE_CANCELADO = 2;
    public static final int TIPO_RECOGIDA_PROXIMA = 3;

    private List<Notificacion> listaNotificaciones;
    private Context contexto;

    // Modelo de datos para representar una notificación
    public static class Notificacion {
        private int tipo;
        private String titulo;
        private String mensaje;
        private String hora;

        public Notificacion(int tipo, String titulo, String mensaje, String hora) {
            this.tipo = tipo;
            this.titulo = titulo;
            this.mensaje = mensaje;
            this.hora = hora;
        }

        public int getTipo() { return tipo; }
        public String getTitulo() { return titulo; }
        public String getMensaje() { return mensaje; }
        public String getHora() { return hora; }
    }

    // Constructor del adaptador
    public NotificacionAdapter(Context contexto, List<Notificacion> listaNotificaciones) {
        this.contexto = contexto;
        this.listaNotificaciones = listaNotificaciones;
    }

    @NonNull
    @Override
    public NotificacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacion, parent, false);
        return new NotificacionViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacionViewHolder holder, int position) {
        Notificacion notificacion = listaNotificaciones.get(position);

        // Asignar textos
        holder.tvTitulo.setText(notificacion.getTitulo());
        holder.tvMensaje.setText(notificacion.getMensaje());
        holder.tvHora.setText(notificacion.getHora());

        // Configurar apariencia según el tipo de notificación
        MaterialCardView cardView = (MaterialCardView) holder.itemView;

        switch (notificacion.getTipo()) {
            case TIPO_VIAJE_ACEPTADO:
                // Icono de check
                holder.ivIcono.setImageResource(R.drawable.ic_check_circle);
                // Fondo del icono
                holder.ivIcono.setBackgroundTintList(
                        ContextCompat.getColorStateList(contexto, R.color.icon_bg_success));
                // Fondo de la tarjeta (opcional)
                cardView.setCardBackgroundColor(
                        ContextCompat.getColor(contexto, R.color.bg_success));
                break;

            case TIPO_VIAJE_CANCELADO:
                // Icono X
                holder.ivIcono.setImageResource(R.drawable.ic_cancel);
                // Fondo del icono
                holder.ivIcono.setBackgroundTintList(
                        ContextCompat.getColorStateList(contexto, R.color.icon_bg_error));
                // Fondo de la tarjeta (opcional)
                cardView.setCardBackgroundColor(
                        ContextCompat.getColor(contexto, R.color.bg_error));
                break;

            case TIPO_RECOGIDA_PROXIMA:
                // Icono de campana
                holder.ivIcono.setImageResource(R.drawable.ic_notifications);
                // Fondo del icono
                holder.ivIcono.setBackgroundTintList(
                        ContextCompat.getColorStateList(contexto, R.color.icon_bg_info));
                // Fondo de la tarjeta (opcional)
                cardView.setCardBackgroundColor(
                        ContextCompat.getColor(contexto, R.color.bg_info));
                break;
        }

        // Mostrar línea divisoria excepto en el último elemento
        boolean esUltimoItem = position == getItemCount() - 1;
        holder.viewDivider.setVisibility(esUltimoItem ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return listaNotificaciones.size();
    }

    // ViewHolder para mantener referencias a las vistas
    public static class NotificacionViewHolder extends RecyclerView.ViewHolder {
        // Cambiado de ShapeableImageView a ImageView
        ImageView ivIcono;
        TextView tvTitulo, tvMensaje, tvHora;
        View viewDivider;
        LinearLayout llContenido;

        @SuppressLint("WrongViewCast")
        public NotificacionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcono = itemView.findViewById(R.id.ivIcono);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvMensaje = itemView.findViewById(R.id.tvMensaje);
            tvHora = itemView.findViewById(R.id.tvHora);
            viewDivider = itemView.findViewById(R.id.viewDivider);
            llContenido = itemView.findViewById(R.id.llContenido);
        }
    }

}
