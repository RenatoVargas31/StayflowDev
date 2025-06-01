package com.codebnb.stayflow.superAdmin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codebnb.stayflow.R;
import com.codebnb.stayflow.superAdmin.model.Hotel;

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

        holder.itemView.setOnClickListener(v -> listener.onHotelClick(hotel));
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
            List<Hotel> filtered = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filtered.addAll(hotelListFull);
            } else {
                String pattern = constraint.toString().toLowerCase().trim();
                for (Hotel h : hotelListFull) {
                    if (h.getName().toLowerCase().contains(pattern)) {
                        filtered.add(h);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filtered;
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            hotelList.clear();
            hotelList.addAll((List<Hotel>) results.values);
            notifyDataSetChanged();
        }
    };

    static class HotelViewHolder extends RecyclerView.ViewHolder {
        TextView textViewHotelName;
        TextView textViewHotelDescription;

        HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewHotelName        = itemView.findViewById(R.id.textViewHotelName);
            textViewHotelDescription = itemView.findViewById(R.id.textViewHotelDescription);
        }
    }
}
