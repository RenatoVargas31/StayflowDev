package com.iot.stayflowdev;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class SolicitudesAdapter extends RecyclerView.Adapter<SolicitudesAdapter.SolicitudViewHolder> {
    private List<SolicitudModel> listaSolicitudes;
    private Context context;

    public void setListaSolicitudes(List<SolicitudModel> listaSolicitudes) {
        this.listaSolicitudes = listaSolicitudes;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public class SolicitudViewHolder extends RecyclerView.ViewHolder {
        SolicitudModel solicitud;
        TextView tvDistance, tvPickupLocation, tvDestinationLocation, tvTiempo, tvEstimatedTime;
        MaterialCardView cardSolicitud;

        public SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvPickupLocation = itemView.findViewById(R.id.tvPickupLocation);
            tvDestinationLocation = itemView.findViewById(R.id.tvDestinationLocation);
            tvTiempo = itemView.findViewById(R.id.tvTiempo);
            tvEstimatedTime = itemView.findViewById(R.id.tvEstimatedTime);
            cardSolicitud = itemView.findViewById(R.id.cardSolicitud);

            cardSolicitud.setOnClickListener(view -> {
                // Iniciar la actividad de detalles y pasar los datos
                Intent intent = new Intent(context, DriverInfoSolicitudActivity.class);

                // Pasar todos los datos de la solicitud a la actividad de detalles
                intent.putExtra("EXTRA_PICKUP_LOCATION", solicitud.getPickupLocation());
                intent.putExtra("EXTRA_DESTINATION_LOCATION", solicitud.getDestinationLocation());
                intent.putExtra("EXTRA_DISTANCE", solicitud.getDistance());
                intent.putExtra("EXTRA_TIME", solicitud.getTiempo());
                intent.putExtra("EXTRA_ESTIMATED_TIME", solicitud.getEstimatedTime());
                intent.putExtra("EXTRA_PASSENGER_NAME", solicitud.getPassengerName());
                intent.putExtra("EXTRA_PASSENGER_PHONE", solicitud.getPassengerPhone());
                intent.putExtra("EXTRA_PASSENGERS_COUNT", solicitud.getPassengersCount());
                intent.putExtra("EXTRA_VEHICLE_TYPE", solicitud.getVehicleType());
                intent.putExtra("EXTRA_NOTES", solicitud.getNotes());
                intent.putExtra("EXTRA_STATUS", solicitud.getStatus());

                context.startActivity(intent);
            });
        }
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_solicitud_cercana, parent, false);
        return new SolicitudViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        SolicitudModel s = listaSolicitudes.get(position);
        holder.solicitud = s;
        holder.tvDistance.setText(s.getDistance());
        holder.tvPickupLocation.setText(s.getPickupLocation());
        holder.tvDestinationLocation.setText(s.getDestinationLocation());
        holder.tvTiempo.setText(s.getTiempo());
        holder.tvEstimatedTime.setText(s.getEstimatedTime());
    }

    @Override
    public int getItemCount() {
        return listaSolicitudes != null ? listaSolicitudes.size() : 0;
    }
}