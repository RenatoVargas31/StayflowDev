package com.iot.stayflowdev.cliente.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.Hotel;

import java.util.ArrayList;
import java.util.List;

public class HotelesTopAdapter extends RecyclerView.Adapter<HotelesTopAdapter.HotelViewHolder> {

    private List<Hotel> hoteles = new ArrayList<>();
    private OnHotelClickListener listener;

    public interface OnHotelClickListener {
        void onHotelClick(Hotel hotel);
    }

    public HotelesTopAdapter(OnHotelClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hotel_top_card, parent, false);
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

    class HotelViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageHotel;
        private final TextView textNombreHotel;
        private final TextView textUbicacion;
        private final TextView textCalificacion;
        private final RatingBar ratingBar;
        private final Button btnSaberMas;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            imageHotel = itemView.findViewById(R.id.imageHotel);
            textNombreHotel = itemView.findViewById(R.id.textNombreHotel);
            textUbicacion = itemView.findViewById(R.id.textUbicacion);
            textCalificacion = itemView.findViewById(R.id.textCalificacion);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnSaberMas = itemView.findViewById(R.id.btnSaberMas);
        }

        public void bind(Hotel hotel) {
            // Asignar datos del hotel a las vistas
            textNombreHotel.setText(hotel.getNombre());
            textUbicacion.setText(hotel.getUbicacion());

            // Configurar calificación - Convertir de String a float
            float calificacion = 0f;
            try {
                // La calificación viene como String ("4.5", "3.8", etc.)
                if (hotel.getCalificacion() != null && !hotel.getCalificacion().isEmpty()) {
                    calificacion = Float.parseFloat(hotel.getCalificacion());
                }
            } catch (NumberFormatException e) {
                Log.e("HotelesTopAdapter", "Error al convertir calificación: " + e.getMessage());
                calificacion = 0f; // Valor por defecto en caso de error
            }

            // Actualizar texto y RatingBar con la calificación
            textCalificacion.setText(hotel.getCalificacion());
            ratingBar.setRating(calificacion);

            // Cargar imagen con Glide - Usamos la primera foto de la lista si existe
            if (hotel.getFotos() != null && !hotel.getFotos().isEmpty()) {
                String primeraFotoUrl = hotel.getFotos().get(0);
                Glide.with(itemView.getContext())
                        .load(primeraFotoUrl)
                        .placeholder(R.drawable.ic_hotel)
                        .error(R.drawable.ic_hotel)
                        .centerCrop()
                        .into(imageHotel);
            } else {
                // Imagen por defecto si no hay fotos
                imageHotel.setImageResource(R.drawable.ic_hotel);
            }

            // Configurar botón "Saber más"
            btnSaberMas.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHotelClick(hotel);
                }
            });
        }
    }
}
