package com.iot.stayflowdev.superAdmin.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.Hotel;
import com.iot.stayflowdev.utils.ImageLoadingUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HotelAdapter
        extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder>
        implements Filterable {

    private static final String TAG = "HotelAdapter";

    public interface OnHotelClickListener {
        void onHotelClick(Hotel hotel);
    }

    private List<Hotel> hotelList;          // lista que se muestra
    private List<Hotel> hotelListFull;      // copia completa para filtrar
    private OnHotelClickListener listener;
    private Map<String, String> adminNamesMap; // Mapa para almacenar nombres de administradores por ID

    public HotelAdapter(List<Hotel> hotelList, OnHotelClickListener listener) {
        this.hotelList = new ArrayList<>();
        this.hotelListFull = new ArrayList<>();
        if (hotelList != null) {
            this.hotelList.addAll(hotelList);
            this.hotelListFull.addAll(hotelList);
        }
        this.listener = listener;
        Log.d(TAG, "HotelAdapter creado con " + this.hotelList.size() + " hoteles");
    }

    public void updateHotels(List<Hotel> newHotels) {
        this.hotelList.clear();
        this.hotelListFull.clear();
        if (newHotels != null) {
            this.hotelList.addAll(newHotels);
            this.hotelListFull.addAll(newHotels);
        }
        Log.d(TAG, "Lista de hoteles actualizada: " + hotelList.size() + " elementos");
        notifyDataSetChanged();
    }

    public void setAdminNamesMap(Map<String, String> adminNamesMap) {
        this.adminNamesMap = adminNamesMap;
        Log.d(TAG, "Mapa de nombres de administradores actualizado, tamaño: " +
              (adminNamesMap != null ? adminNamesMap.size() : 0));
        notifyDataSetChanged();
    }

    @NonNull @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "Creando ViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.superadmin_item_hotel_card, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        if (position >= hotelList.size()) {
            Log.e(TAG, "Posición fuera de límites: " + position + " para lista de tamaño " + hotelList.size());
            return;
        }

        Hotel hotel = hotelList.get(position);
        Log.d(TAG, "Mostrando hotel en posición " + position + ": " +
              (hotel != null ? (hotel.getNombre() + ", admin: " + hotel.getAdministradorAsignado()) : "null"));

        if (hotel == null) {
            Log.e(TAG, "Hotel null en posición " + position);
            return;
        }

        // Mostrar el nombre del hotel
        if (hotel.getNombre() != null) {
            holder.textViewHotelName.setText(hotel.getNombre());
        } else {
            holder.textViewHotelName.setText("Hotel sin nombre");
        }

        // Mostrar el nombre del administrador si está disponible
        if (hotel.getAdministradorAsignado() != null && !hotel.getAdministradorAsignado().isEmpty() && adminNamesMap != null) {
            String adminName = adminNamesMap.get(hotel.getAdministradorAsignado());
            if (adminName != null) {
                holder.textViewAdminName.setText("Administrador: " + adminName);
                Log.d(TAG, "Administrador encontrado: " + adminName);
            } else {
                holder.textViewAdminName.setText("Administrador: Asignado pero sin nombre");
                Log.d(TAG, "Administrador no encontrado en el mapa para ID: " + hotel.getAdministradorAsignado());
            }
        } else {
            holder.textViewAdminName.setText("Administrador: No asignado");
            Log.d(TAG, "Hotel sin administrador asignado");
        }

        // Cargar imagen del hotel desde la galería
        loadHotelImage(hotel, holder.imageViewHotel);

        // Hacer que toda la tarjeta sea clickeable
        holder.cardHotel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHotelClick(hotel);
            }
        });
    }

    @Override
    public int getItemCount() {
        int size = hotelList != null ? hotelList.size() : 0;
        Log.d(TAG, "getItemCount devuelve: " + size);
        return size;
    }

    @Override
    public Filter getFilter() {
        return hotelFilter;
    }

    private final Filter hotelFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Hotel> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(hotelListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Hotel hotel : hotelListFull) {
                    if (hotel.getNombre() != null && hotel.getNombre().toLowerCase().contains(filterPattern)) {
                        filteredList.add(hotel);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            hotelList.clear();
            if (results.values != null) {
                hotelList.addAll((List<Hotel>) results.values);
            }
            Log.d(TAG, "Filtro aplicado, ahora mostrando " + hotelList.size() + " hoteles");
            notifyDataSetChanged();
        }
    };

    static class HotelViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardHotel;
        TextView textViewHotelName, textViewAdminName;
        ImageView imageViewHotel; // Agregado para la imagen del hotel

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            cardHotel = itemView.findViewById(R.id.cardHotel);
            textViewHotelName = itemView.findViewById(R.id.textViewHotelName);
            textViewAdminName = itemView.findViewById(R.id.textViewAdminName);
            imageViewHotel = itemView.findViewById(R.id.imageViewHotel); // Inicializado aquí
        }
    }

    private void loadHotelImage(Hotel hotel, ImageView imageView) {
        // Establecer placeholder inicial
        imageView.setImageResource(R.drawable.ic_apartment);

        if (hotel.getId() == null || hotel.getId().isEmpty()) {
            Log.w(TAG, "Hotel sin ID, no se puede cargar imagen");
            return;
        }

        Log.d(TAG, "Cargando imagen para hotel: " + hotel.getNombre() + " (ID: " + hotel.getId() + ")");

        // Consultar la colección 'galeria' del hotel para obtener las imágenes
        FirebaseFirestore.getInstance()
                .collection("hoteles")
                .document(hotel.getId())
                .collection("galeria")
                .limit(1) // Solo obtener la primera imagen
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Obtener la primera imagen de la galería
                        String imageUrl = queryDocumentSnapshots.getDocuments().get(0).getString("url");

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Log.d(TAG, "Imagen encontrada para hotel " + hotel.getNombre() + ": " + imageUrl);

                            // Usar la nueva utilidad optimizada para cargar la imagen
                            ImageLoadingUtils.loadImageSafely(
                                imageView.getContext(),
                                imageUrl,
                                imageView,
                                R.drawable.ic_apartment,
                                new ImageLoadingUtils.ImageLoadCallback() {
                                    @Override
                                    public void onLoadSuccess() {
                                        // Limpiar fondo cuando la imagen carga exitosamente
                                        imageView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                                    }

                                    @Override
                                    public void onLoadFailed() {
                                        // Mantener fondo y placeholder
                                        imageView.setBackgroundColor(imageView.getContext().getColor(R.color.md_theme_surfaceVariant));
                                    }
                                }
                            );
                        } else {
                            Log.w(TAG, "URL de imagen vacía para hotel: " + hotel.getNombre());
                        }
                    } else {
                        Log.w(TAG, "No se encontraron imágenes en la galería para hotel: " + hotel.getNombre());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al consultar galería del hotel " + hotel.getNombre() + ": " + e.getMessage());
                    // Mantener ícono por defecto en caso de error
                    imageView.setImageResource(R.drawable.ic_apartment);
                });
    }
}
