package com.iot.stayflowdev.Driver.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.iot.stayflowdev.Driver.Dtos.SolicitudTaxi;
import com.iot.stayflowdev.Driver.Model.SolicitudModel;
import com.iot.stayflowdev.Driver.Activity.DriverInfoSolicitudActivity;
import com.iot.stayflowdev.R;

import java.util.ArrayList;
import java.util.List;

public class SolicitudesAdapter extends RecyclerView.Adapter<SolicitudesAdapter.SolicitudViewHolder> {
    private List<SolicitudTaxi> listaSolicitudes = new ArrayList<>();
    private Context context;

    public void setListaSolicitudes(List<SolicitudTaxi> listaSolicitudes) {
        this.listaSolicitudes = listaSolicitudes;
        notifyDataSetChanged();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public class SolicitudViewHolder extends RecyclerView.ViewHolder {
        TextView tvPickupLocationName;
        TextView tvDistance, tvEstimatedTime, tvEstado, tvTiempo;
        MaterialCardView cardSolicitud;
        LinearLayout estadoContainer;

        public SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPickupLocationName = itemView.findViewById(R.id.tvPickupLocationName);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvEstimatedTime = itemView.findViewById(R.id.tvEstimatedTime);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvTiempo = itemView.findViewById(R.id.tvTiempo);
            cardSolicitud = itemView.findViewById(R.id.cardSolicitud);

            // Obtener el contenedor del estado para cambiar colores
            estadoContainer = (LinearLayout) tvEstado.getParent();
        }

        public void bind(SolicitudTaxi solicitud) {
            // Configurar ubicación de origen
            tvPickupLocationName.setText(solicitud.getOrigen());

            // Configurar distancia calculada
            if (solicitud.getDistanciaKm() > 0) {
                if (solicitud.getDistanciaKm() < 1) {
                    tvDistance.setText(String.format("%.0f m", solicitud.getDistanciaKm() * 1000));
                } else {
                    tvDistance.setText(String.format("%.1f km", solicitud.getDistanciaKm()));
                }
            } else {
                tvDistance.setText("-- km");
            }

            // Configurar tiempo estimado calculado
            if (solicitud.getTiempoEstimadoMin() > 0) {
                tvEstimatedTime.setText(formatearTiempo(solicitud.getTiempoEstimadoMin()));
            } else {
                tvEstimatedTime.setText("-- min");
            }

            // Configurar estado
            configurarEstado(solicitud.getEstado());

            // Configurar tiempo transcurrido (dinámico)
            tvTiempo.setText(calcularTiempoTranscurrido(solicitud.getFechaCreacion()));

            // Click al card
            // En el adapter, en el onClick:
            cardSolicitud.setOnClickListener(v -> {
                try {
                    Log.d("DEBUG", "Click detectado en solicitud: " + solicitud.getSolicitudId());
                    Intent intent = new Intent(context, DriverInfoSolicitudActivity.class);

                    // SOLO PASAR EL ID, NO EL OBJETO COMPLETO
                    intent.putExtra("solicitudId", solicitud.getSolicitudId());

                    Log.d("DEBUG", "Iniciando activity solo con ID");
                    context.startActivity(intent);
                } catch (Exception e) {
                    Log.e("ERROR", "Error al abrir activity: ", e);
                    Toast.makeText(context, "Error al abrir detalles", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void configurarEstado(String estado) {
            if (estado == null) estado = "pendiente";

            switch (estado.toLowerCase()) {
                case "pendiente":
                    tvEstado.setText("Nueva");
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.green_500));
                    estadoContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.icon_bg_success));
                    break;

                case "aceptada":
                    tvEstado.setText("Aceptada");
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.blue_500));
                    estadoContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_500));
                    break;

                case "en_camino":
                    tvEstado.setText("En camino");
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.orange_500));
                    estadoContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.orange_500));
                    break;

                case "rechazada":
                    tvEstado.setText("Rechazada");
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.red_500));
                    estadoContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.red_500));
                    break;

                default:
                    tvEstado.setText("Desconocida");
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant));
                    estadoContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant));
                    break;
            }
        }

        private String formatearTiempo(int minutos) {
            if (minutos < 60) {
                return minutos + " min";
            } else {
                int horas = minutos / 60;
                int minutosRestantes = minutos % 60;
                if (minutosRestantes == 0) {
                    return horas + " h";
                } else {
                    return horas + "h " + minutosRestantes + "m";
                }
            }
        }

        private String calcularTiempoTranscurrido(Timestamp fechaCreacion) {
            if (fechaCreacion == null) {
                return "Tiempo desconocido";
            }

            long tiempoActual = System.currentTimeMillis();
            long tiempoCreacion = fechaCreacion.toDate().getTime();
            long diferencia = tiempoActual - tiempoCreacion;

            long segundos = diferencia / 1000;
            long minutos = segundos / 60;
            long horas = minutos / 60;
            long dias = horas / 24;

            if (dias > 0) {
                return "Hace " + dias + " día" + (dias != 1 ? "s" : "");
            } else if (horas > 0) {
                return "Hace " + horas + " hora" + (horas != 1 ? "s" : "");
            } else if (minutos > 0) {
                return "Hace " + minutos + " min";
            } else {
                return "Hace un momento";
            }
        }
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solicitud_cercana, parent, false);
        return new SolicitudViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        SolicitudTaxi solicitud = listaSolicitudes.get(position);
        holder.bind(solicitud);
    }

    @Override
    public int getItemCount() {
        return listaSolicitudes != null ? listaSolicitudes.size() : 0;
    }
}
