package com.iot.stayflowdev.superAdmin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.superAdmin.model.Hotel;

import java.util.ArrayList;
import java.util.List;

public class HotelAdapter
        extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder>
        implements Filterable {

    public interface OnHotelClickListener {
        void onHotelClick(Hotel hotel);
    }

    private List<Hotel> hotelList;          // lista que se muestra
    private List<Hotel> hotelListFull;      // copia completa para filtrar
    private OnHotelClickListener listener;

    public HotelAdapter(List<Hotel> hotelList, OnHotelClickListener listener) {
        this.hotelList = new ArrayList<>(hotelList);
        this.hotelListFull = new ArrayList<>(hotelList);
        this.listener = listener;
    }

    @NonNull @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.superadmin_item_hotel_card, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotelList.get(position);
        holder.textViewHotelName.setText(hotel.getName());
        holder.textViewHotelDescription.setText(hotel.getDescription());

        // Hacer que toda la tarjeta sea clickeable
        holder.cardHotel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHotelClick(hotel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return hotelList.size();
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
                    if (hotel.getName().toLowerCase().contains(filterPattern)) {
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
            hotelList.addAll((List<Hotel>) results.values);
            notifyDataSetChanged();
        }
    };

    static class HotelViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardHotel;
        TextView textViewHotelName, textViewHotelDescription;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            cardHotel = itemView.findViewById(R.id.cardHotel);
            textViewHotelName = itemView.findViewById(R.id.textViewHotelName);
            textViewHotelDescription = itemView.findViewById(R.id.textViewHotelDescription);
        }
    }
}
