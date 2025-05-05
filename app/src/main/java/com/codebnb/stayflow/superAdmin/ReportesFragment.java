package com.codebnb.stayflow.superAdmin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codebnb.stayflow.R;
import com.codebnb.stayflow.SuperAdminActivity;
import com.codebnb.stayflow.superAdmin.adapter.HotelAdapter;
import com.codebnb.stayflow.superAdmin.model.Hotel;

import java.util.ArrayList;
import java.util.List;

public class ReportesFragment extends Fragment {

    private RecyclerView recyclerViewHotels;
    private HotelAdapter hotelAdapter;
    private List<Hotel> hotelList;
    private EditText searchEditText;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reportes_superadmin, container, false);

        recyclerViewHotels = view.findViewById(R.id.recyclerViewHotels);
        searchEditText     = view.findViewById(R.id.searchEditText);

        recyclerViewHotels.setLayoutManager(new LinearLayoutManager(getContext()));
        loadHotelData(); // llena hotelList con instancias de Hotel

        hotelAdapter = new HotelAdapter(hotelList, this::navigateToFilterView);
        recyclerViewHotels.setAdapter(hotelAdapter);

        searchEditText.addTextChangedListener(new TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){
                hotelAdapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable e){}
        });

        return view;
    }

    private void loadHotelData() {
        hotelList = new ArrayList<>();
        hotelList.add(new Hotel("1", "Hotel 1", "Lorem ipsum dolor sit amet"));
        hotelList.add(new Hotel("2", "Hotel 2", "Sit amet lorem ipsum dolor"));
        hotelList.add(new Hotel("2", "Hotel 3", "Sit amet lorem ipsum dolor"));
        hotelList.add(new Hotel("2", "Hotel 4", "Sit amet lorem ipsum dolor"));
        hotelList.add(new Hotel("2", "Hotel 5", "Sit amet lorem ipsum dolor"));
        hotelList.add(new Hotel("2", "Hotel 6", "Sit amet lorem ipsum dolor"));
        // …más hoteles de ejemplo…
    }

    private void navigateToFilterView(Hotel hotel) {
        FilterReportFragment filterFragment = FilterReportFragment.newInstance(hotel.getName());

        if (getActivity() instanceof SuperAdminActivity) {
            ((SuperAdminActivity) getActivity()).hideBottomNavigation();
        }
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, filterFragment)
                .addToBackStack(null)
                .commit();
    }
}
