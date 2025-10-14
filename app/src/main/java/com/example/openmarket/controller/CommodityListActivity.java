package com.example.openmarket.controller;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openmarket.CommodityAdapter;
import com.example.openmarket.R;
import com.example.openmarket.db.Repository;
import com.example.openmarket.model.Commodity;
import com.example.openmarket.model.PriceRecord;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CommodityListActivity extends AppCompatActivity {

    RecyclerView recyclerCommodities;
    CommodityAdapter adapter;
    List<CommodityAdapter.CommodityDisplayData> displayDataList;
    FloatingActionButton fabAddPrice;

    ImageButton trends;


    View trendsScreen;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commodity_list);

        recyclerCommodities = findViewById(R.id.recyclerCommodities);
        recyclerCommodities.setLayoutManager(new LinearLayoutManager(this));

        fabAddPrice = findViewById(R.id.fabAddPrice);
        fabAddPrice.setOnClickListener(v -> {
            Intent intent = new Intent(CommodityListActivity.this, AddCommodityActivity.class);
            startActivity(intent);
        });

        trendsScreen = findViewById(R.id.trendsBtn);
        trendsScreen.setOnClickListener(v -> {
            Intent intent = new Intent(CommodityListActivity.this, TrendsActivity.class);
            startActivity(intent);
        });

        trendsScreen = findViewById(R.id.trendBtn);
        trendsScreen.setOnClickListener(v -> {
            Intent intent = new Intent(CommodityListActivity.this, TrendsActivity.class);
            startActivity(intent);
        });

        loadCommodities();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from AddCommodity activity
        loadCommodities();
    }

    private void loadCommodities() {
        // Load data from FakeRepo
        displayDataList = new ArrayList<>();
        List<Commodity> commodities = Repository.getCommodities(this);

        for (Commodity commodity : commodities) {
            List<PriceRecord> priceHistory = Repository.getPricesForCommodity(this, commodity);

            if (!priceHistory.isEmpty()) {
                // Sort by date to get latest and previous prices
                priceHistory.sort(Comparator.comparing(PriceRecord::getLastUpdated).reversed());

                PriceRecord latestPrice = priceHistory.get(0);
                double changePercent = 0.0;

                // Calculate percentage change if we have historical data
                if (priceHistory.size() > 1) {
                    PriceRecord previousPrice = priceHistory.get(1);
                    changePercent = ((latestPrice.getPrice() - previousPrice.getPrice())
                            / previousPrice.getPrice()) * 100;
                }

                // Format the data for display
                String name = commodity.getName();
                String lastUpdated = latestPrice.getLastUpdated()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String formattedPrice = String.format(Locale.getDefault(), "K%.2f",
                        latestPrice.getPrice());
                String changePercentStr = String.format(Locale.getDefault(),
                        "%+.1f%%", changePercent);

                // Get appropriate image resource based on commodity name
                int imageRes = getCommodityImage(commodity.getName());

                displayDataList.add(new CommodityAdapter.CommodityDisplayData(
                        name, lastUpdated, formattedPrice, changePercentStr, imageRes
                ));
            }
        }

        adapter = new CommodityAdapter(this, displayDataList);
        recyclerCommodities.setAdapter(adapter);
    }

    private int getCommodityImage(String commodityName) {
        // Map commodity names to drawable resources
        // For now, using default launcher icon - replace with actual commodity icons
        switch (commodityName.toLowerCase()) {
            case "maize":
                return R.drawable.ic_launcher_foreground; // Replace with ic_maize
            case "copper":
                return R.drawable.ic_launcher_foreground; // Replace with ic_copper
            case "mealie meal":
                return R.drawable.ic_launcher_foreground; // Replace with ic_mealie_meal
            case "cooking oil":
                return R.drawable.ic_launcher_foreground; // Replace with ic_cooking_oil
            default:
                return R.drawable.ic_launcher_foreground;
        }
    }
}