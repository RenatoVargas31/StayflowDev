package com.iot.stayflowdev.cliente.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.Hotel;
import com.iot.stayflowdev.model.LugaresCercanos;
import com.iot.stayflowdev.model.Servicio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HotelBusquedaAdapter extends RecyclerView.Adapter<HotelBusquedaAdapter.HotelViewHolder> {

    private static final String TAG = "HotelBusquedaAdapter";
    private List<Hotel> hoteles = new ArrayList<>();
    private Map<String, List<Servicio>> serviciosPorHotel = new HashMap<>();
    private Map<String, List<LugaresCercanos>> lugaresPorHotel = new HashMap<>();
    private HotelItemClickListener listener;

    public interface HotelItemClickListener {
        void onHotelClick(Hotel hotel);
    }

    public HotelBusquedaAdapter(HotelItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hotel_busqueda_card, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hoteles.get(position);
        holder.bind(hotel);
    }

    @Override
    public int getItemCount() {
        return hoteles.size();
    }

    public void setHoteles(List<Hotel> hoteles) {
        this.hoteles = hoteles;
        notifyDataSetChanged();
    }

    public void setServiciosPorHotel(String hotelId, List<Servicio> servicios) {
        serviciosPorHotel.put(hotelId, servicios);
        notifyDataSetChanged();
    }

    public void setLugaresPorHotel(String hotelId, List<LugaresCercanos> lugares) {
        lugaresPorHotel.put(hotelId, lugares);
        notifyDataSetChanged();
    }

    /**
     * Obtiene el número de lugares históricos asociados a un hotel
     * @param hotelId ID del hotel
     * @return Número de lugares históricos cercanos
     */
    public int getNumeroLugaresPorHotel(String hotelId) {
        List<LugaresCercanos> lugares = lugaresPorHotel.get(hotelId);
        return lugares != null ? lugares.size() : 0;
    }

    class HotelViewHolder extends RecyclerView.ViewHolder {
        private final TextView textHotelName, textHotelUbicacion, textRating, textLugaresHistoricos;
        private final ImageView imgHotel;
        private final RatingBar ratingBar;
        private final LinearLayout layoutServicioRestaurante, layoutServicioPiscina,
                layoutServicioWifi, layoutServicioEstacionamiento, layoutServicioMascotas;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            textHotelName = itemView.findViewById(R.id.text_hotel_name);
            textHotelUbicacion = itemView.findViewById(R.id.text_hotel_ubicacion);
            textRating = itemView.findViewById(R.id.text_rating);
            textLugaresHistoricos = itemView.findViewById(R.id.text_lugares_historicos);
            imgHotel = itemView.findViewById(R.id.img_hotel);
            ratingBar = itemView.findViewById(R.id.rating_bar);

            // Servicios
            layoutServicioRestaurante = itemView.findViewById(R.id.layout_servicio_restaurante);
            layoutServicioPiscina = itemView.findViewById(R.id.layout_servicio_piscina);
            layoutServicioWifi = itemView.findViewById(R.id.layout_servicio_wifi);
            layoutServicioEstacionamiento = itemView.findViewById(R.id.layout_servicio_estacionamiento);
            layoutServicioMascotas = itemView.findViewById(R.id.layout_servicio_mascotas);

            // Configurar click listener para toda la tarjeta
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onHotelClick(hoteles.get(position));
                }
            });
        }

        public void bind(Hotel hotel) {
            // Información básica del hotel
            textHotelName.setText(hotel.getNombre());
            textHotelUbicacion.setText(hotel.getUbicacion());

            // Calificación
            float calificacion = 0f;
            try {
                if (hotel.getCalificacion() != null && !hotel.getCalificacion().isEmpty()) {
                    calificacion = Float.parseFloat(hotel.getCalificacion());
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error al convertir calificación: " + e.getMessage());
            }
            textRating.setText(hotel.getCalificacion());
            ratingBar.setRating(calificacion);

            // Imagen del hotel
            if (hotel.getFotos() != null && !hotel.getFotos().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(hotel.getFotos().get(0))
                        .placeholder(R.drawable.ic_hotel)
                        .error(R.drawable.ic_hotel)
                        .into(imgHotel);
            } else {
                imgHotel.setImageResource(R.drawable.ic_hotel);
            }

            // Lugares históricos cercanos
            List<LugaresCercanos> lugares = lugaresPorHotel.get(hotel.getId());
            if (lugares != null && !lugares.isEmpty()) {
                textLugaresHistoricos.setText(lugares.size() + " lugares históricos cercanos");
            } else {
                textLugaresHistoricos.setText("Sin lugares históricos cercanos");
            }

            // Mostrar u ocultar servicios según disponibilidad
            mostrarServicios(hotel);
        }

        private void mostrarServicios(Hotel hotel) {
            // Por defecto ocultar todos los servicios
            layoutServicioRestaurante.setVisibility(View.GONE);
            layoutServicioPiscina.setVisibility(View.GONE);
            layoutServicioWifi.setVisibility(View.GONE);
            layoutServicioEstacionamiento.setVisibility(View.GONE);
            layoutServicioMascotas.setVisibility(View.GONE);

            // Mostrar servicios disponibles
            List<Servicio> servicios = serviciosPorHotel.get(hotel.getId());
            if (servicios != null && !servicios.isEmpty()) {
                for (Servicio servicio : servicios) {
                    if (servicio.getNombre() == null) continue;

                    switch (servicio.getNombre().toLowerCase()) {
                        case "restaurante":
                            layoutServicioRestaurante.setVisibility(View.VISIBLE);
                            break;
                        case "piscina":
                            layoutServicioPiscina.setVisibility(View.VISIBLE);
                            break;
                        case "wifi":
                            layoutServicioWifi.setVisibility(View.VISIBLE);
                            break;
                        case "estacionamiento":
                            layoutServicioEstacionamiento.setVisibility(View.VISIBLE);
                            break;
                        case "mascotas":
                            layoutServicioMascotas.setVisibility(View.VISIBLE);
                            break;
                    }
                }
            }
        }
    }
}
