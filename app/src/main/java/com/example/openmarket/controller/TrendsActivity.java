package com.example.openmarket.controller;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openmarket.R;
import com.example.openmarket.db.Repository;
import com.example.openmarket.model.Commodity;
import com.example.openmarket.model.PriceRecord;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TrendsActivity extends AppCompatActivity {
    private AutoCompleteTextView commodityAutoComplete;
    private LineChart priceTrendChart;
    private TextView tvMinPrice, tvMaxPrice, tvAvgPrice, commodityName;
    private MaterialToolbar toolbar;
    private View emptyView;
    private CircularProgressIndicator loadingIndicator;
    private MaterialCardView summaryCard, chartCard;
    private final DecimalFormat priceFormat = new DecimalFormat("K#,##0.00");
    private final SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final DateTimeFormatter outputDateFormat = DateTimeFormatter.ofPattern("MMM dd");

    //this is the first thing to run when the screen appears... its like the main method
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trends); //This loads the xml layout and defines how the screen looks

        initViews(); //connects the xml to the code
        setUpToolbar(); //prepares the top bar
        setupChart(); // configures the MPAndroidChart to set up the line chart
        setupSpinner(); //Prepares the dropdown for commodities
    }

    //this methods finds each UI by there ID and stores them in a variable
    private void initViews() {
        toolbar = findViewById(R.id.trendsToolbar);
        commodityAutoComplete = findViewById(R.id.commodityAutoComplete);
        priceTrendChart = findViewById(R.id.priceTrendChart);
        tvAvgPrice = findViewById(R.id.tvAvgPrice);
        tvMaxPrice = findViewById(R.id.tvMaxPrice);
        tvMinPrice = findViewById(R.id.tvMinPrice);
        commodityName = findViewById(R.id.commodityName);
        emptyView = findViewById(R.id.emptyState);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        summaryCard = findViewById(R.id.summaryCard);
        chartCard = findViewById(R.id.chartCard);
    }

    //Makes the toolbar at the top have a back button and a title
    private void setUpToolbar(){
        setSupportActionBar(toolbar);
        //this add the back arrow icon
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    //this set up the dropdown using auto complete textview

    private void setupSpinner() {
        List<Commodity> commodities = Repository.getCommodities(this);
        List<String> commodityName = commodities.stream()
                .map(Commodity::getName)
                .collect(Collectors.toList());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                commodityName
        );

        commodityAutoComplete.setAdapter(adapter);
        commodityAutoComplete.setThreshold(1);

        commodityAutoComplete.setOnItemClickListener(((parent, view, position, id) -> {
                    String selectedCommodityName = adapter.getItem(position);
                    if(selectedCommodityName != null){
                        Commodity selectedCompotity = commodities.stream()
                                .filter(c -> c.getName().equals(selectedCommodityName))
                                .findFirst()
                                .orElse(null);

                        if(selectedCompotity != null) {
                            loadCommodityData(selectedCompotity);
                        }
                    }
        }));


    /**private void setupSpinner() {
        //gets data from the utils
        List<String> commodities = DataUtils.getAllCommodities();

        //This connects the list of commodities to the dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                commodities
        );
        //This suggest the results after typing 1 character
        commodityAutoComplete.setAdapter(adapter);
        commodityAutoComplete.setThreshold(1);

        //This calls the loadCommodity methods when a user picks the commodity
        commodityAutoComplete.setOnItemClickListener(((parent, view, position, id) -> {
            String selectedCommodity = adapter.getItem(position);
            if(selectedCommodity != null){
                loadCommodityData(selectedCommodity);
            }
        }));*/
    }
    //This set up the chart
    private void setupChart() {
        //This configures the look and feel of the chart and enable interactions like the drag, zoom, scroll
        priceTrendChart.setDrawGridBackground(false);
        priceTrendChart.setDrawBorders(false);
        priceTrendChart.setTouchEnabled(true);
        priceTrendChart.setDragEnabled(true);
        priceTrendChart.setScaleEnabled(true);
        priceTrendChart.setPinchZoom(true);
        priceTrendChart.setExtraOffsets(10,10,10,10);

        Description description = new Description();
        description.setText("");
        priceTrendChart.setDescription(description);

        //Shows what the line represents
        Legend legend = priceTrendChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(12f);
        legend.setFormSize(12f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        //X-Axis, puts the data labels at the button and rotates them to fit.
        XAxis xAxis = priceTrendChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.parseColor("#666666"));
        xAxis.setLabelRotationAngle(-45f);

        //Left Y-Axis, formats the vertical price label and hides the right side axis
        YAxis leftAxis = priceTrendChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E0E0E0"));
        leftAxis.setTextSize(10f);
        leftAxis.setTextColor(Color.parseColor("#666666"));
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value){
                return "K" + (int) value;
            }
        });
        // Right Y-Axis disabled
        priceTrendChart.getAxisRight().setEnabled(false);
    }

    //add a ka short delay the upload the data
    private void loadCommodityData(Commodity commodity) {
        showLoading();

        //A short delay
        priceTrendChart.postDelayed(() -> {
            commodityName.setText(commodity.getName());
            List<PriceRecord> prices = Repository.getPricesForCommodity(this, commodity);

            if(prices.isEmpty()) {
                showEmptySate();
            } else {
                updateChart(commodity, prices);
                updateSummary(prices);
                showContent();

            }
        }, 300);
    }

    //This draws the lines chart
    private void updateChart(Commodity commodity, List<PriceRecord> prices) {


        List<Entry> entries = new ArrayList<>();
        List<String> dataLabels = new ArrayList<>();

        // converts prices into chart entries
        for (int i = 0; i < prices.size(); i++) {
            PriceRecord price = prices.get(i);
            entries.add(new Entry(i, (float) price.getPrice() ));
            dataLabels.add(formatDate(price.getLastUpdated()));
        }

        //defines how the line looks (color, shape, fill and smoothness)
        LineDataSet dataSet = new LineDataSet(entries, commodity.getName());

        //Dataset Styling
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setCircleHoleRadius(3f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(9f);
        dataSet.setValueTextColor(Color.parseColor("#333333"));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);

        //Fill gradient
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#4CAF50"));
        dataSet.setFillAlpha(30);

        //Value formatter
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value){
                return "K" + (int) value;
            }
        });

        //Highlight settings
        dataSet.setHighLightColor(Color.parseColor("#FF6F00"));
        dataSet.setDrawHighlightIndicators(true);
        dataSet.setHighlightLineWidth(2f);

        //attach data to chart
        LineData lineData = new LineData(dataSet);
        priceTrendChart.setData(lineData);

        //Set X-axis labels
        XAxis xAxis = priceTrendChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if(index >= 0 && index < dataLabels.size()) {
                    return dataLabels.get(index);
                }
                return "";
            }
        });

        priceTrendChart.animateXY(800, 800);
        priceTrendChart.invalidate();
    }

    //Calculates min, max and avg prices from the list
    @SuppressLint("SetTextI18n")
    /**private void updateSummaryWithDataUtils(String selectedCommodity) {
        Map<String, Float> stats = DataUtils.getPriceStatistics(selectedCommodity);

        Float min = stats.get("min");
        Float max = stats.get("max");
        Float avg = stats.get("avg");

        if(min == null || max == null || avg == null) {
            Log.e("TrendsActivity", "One or more stats values are null: " + stats);
        }

        tvMinPrice.setText(priceFormat.format(min));
        tvMaxPrice.setText(priceFormat.format(max));
        tvAvgPrice.setText(priceFormat.format(avg));

    } */

    private void updateSummary(List<PriceRecord> prices) {
        if(prices.isEmpty()) {
            tvMinPrice.setText("K0.00");
            tvMaxPrice.setText("K0.00");
            tvAvgPrice.setText("K0.00");
        }

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double total = 0;

        for(PriceRecord pr : prices) {
            double price = pr.getPrice();
            min = Math.min(min, price);
            max = Math.max(max, price);
            total += price;
        }
        double avg = total / prices.size();

        tvMinPrice.setText(priceFormat.format(min));
        tvMaxPrice.setText(priceFormat.format(max));
        tvAvgPrice.setText(priceFormat.format(avg));
    }
    //converts "yyyy-MM-dd" to "MMM-dd"
 private String formatDate(LocalDate date) {
        return date.format(outputDateFormat);
 }

    //All these methods controls visibility

    private void showLoading() {
        loadingIndicator.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        summaryCard.setVisibility(View.GONE);
        chartCard.setVisibility(View.GONE);
    }

    private void showContent() {
        loadingIndicator.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        summaryCard.setVisibility(View.VISIBLE);
        chartCard.setVisibility(View.VISIBLE);
    }

    private void showEmptySate() {
        loadingIndicator.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        summaryCard.setVisibility(View.GONE);
        chartCard.setVisibility(View.GONE);
    }

    //handles the arrow back click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            navigateToHome();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, CommodityListActivity.class);//change to themba's class
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    //cleans up the chart to prevents memory leaks
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(priceTrendChart != null) {
            priceTrendChart.clear();
        }
    }
}
