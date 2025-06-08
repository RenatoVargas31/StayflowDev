package com.iot.stayflowdev.Driver.Adapter;

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
        notifyDataSetChanged(); // Actualiza el RecyclerView cuando se recibe la nueva lista
    }
    public void setContext(Context context) {
        this.context = context;
    }
    public class SolicitudViewHolder extends RecyclerView.ViewHolder {
        TextView tvPickupLocationName, tvPickupLocation;
        TextView tvDestinationLocationName, tvDestinationLocation;
        TextView tvDistance, tvTiempo, tvEstimatedTime;
        MaterialCardView cardSolicitud;
        public SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);

            // Pickup
            tvPickupLocationName = itemView.findViewById(R.id.tvPickupLocationName);
            tvPickupLocation = itemView.findViewById(R.id.tvPickupLocation);

            // Destination
            tvDestinationLocationName = itemView.findViewById(R.id.tvDestinationLocationName);
            tvDestinationLocation = itemView.findViewById(R.id.tvDestinationLocation);

            // Extras (puedes usarlos más adelante)
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvTiempo = itemView.findViewById(R.id.tvTiempo);
            tvEstimatedTime = itemView.findViewById(R.id.tvEstimatedTime);

            // Card
            cardSolicitud = itemView.findViewById(R.id.cardSolicitud);
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
        SolicitudTaxi solicitud = listaSolicitudes.get(position); // ← esta es la línea clave

        // Asignar datos a las vistas
        holder.tvPickupLocationName.setText(solicitud.getOrigen());
        holder.tvPickupLocation.setText(solicitud.getOrigenDireccion());
        holder.tvDestinationLocationName.setText(solicitud.getDestino());
        holder.tvDestinationLocation.setText(solicitud.getDestinoDireccion());

        holder.tvDistance.setText("Distancia: ~3 km");
        holder.tvTiempo.setText("Hace 1 min");
        holder.tvEstimatedTime.setText("~15 min");

        // Click al card
        holder.cardSolicitud.setOnClickListener(v -> {
            Intent intent = new Intent(context, DriverInfoSolicitudActivity.class);
            intent.putExtra("solicitud", solicitud); // ✅ Ahora sí existe
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaSolicitudes != null ? listaSolicitudes.size() : 0;
    }
}
