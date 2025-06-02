package com.iot.stayflowdev.superAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.superAdmin.adapter.HotelAdapter;
import com.iot.stayflowdev.superAdmin.model.Hotel;

import java.util.ArrayList;
import java.util.List;

public class ReportesActivity extends BaseSuperAdminActivity {

    public static final String EXTRA_HOTEL_NAME = "HOTEL_NAME";

    private RecyclerView recyclerViewHotels;
    private HotelAdapter hotelAdapter;
    private List<Hotel> hotelList;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // El layout ya se configura en BaseSuperAdminActivity

        initViews();
        setupRecyclerView();
        setupSearch();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.superadmin_reportes_superadmin;
    }

    @Override
    protected int getBottomNavigationSelectedItem() {
        return R.id.nav_reportes;
    }

    @Override
    protected String getToolbarTitle() {
        return "Reportes";
    }

    private void initViews() {
        recyclerViewHotels = findViewById(R.id.recyclerViewHotels);
        searchEditText = findViewById(R.id.searchEditText);
    }

    private void setupRecyclerView() {
        recyclerViewHotels.setLayoutManager(new LinearLayoutManager(this));
        loadHotelData(); // llena hotelList con instancias de Hotel

        hotelAdapter = new HotelAdapter(hotelList, this::navigateToFilterView);
        recyclerViewHotels.setAdapter(hotelAdapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hotelAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadHotelData() {
        hotelList = new ArrayList<>();
        hotelList.add(new Hotel("1", "Hotel 1", "Lorem ipsum dolor sit amet"));
        hotelList.add(new Hotel("2", "Hotel 2", "Sit amet lorem ipsum dolor"));
        hotelList.add(new Hotel("3", "Hotel 3", "Sit amet lorem ipsum dolor"));
        hotelList.add(new Hotel("4", "Hotel 4", "Sit amet lorem ipsum dolor"));
        hotelList.add(new Hotel("5", "Hotel 5", "Sit amet lorem ipsum dolor"));
        hotelList.add(new Hotel("6", "Hotel 6", "Sit amet lorem ipsum dolor"));
        // Más hoteles de ejemplo si es necesario
    }

    private void navigateToFilterView(Hotel hotel) {
        Intent intent = new Intent(ReportesActivity.this, FilterReportActivity.class);
        intent.putExtra(EXTRA_HOTEL_NAME, hotel.getName());
        startActivity(intent);
    }
}