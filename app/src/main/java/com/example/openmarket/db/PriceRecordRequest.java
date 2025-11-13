package com.example.openmarket.db;

import com.example.openmarket.model.PriceRecord;

public class PriceRecordRequest {
    private final int commodity_id;
    private final double price;
    private final String date;

    public PriceRecordRequest(PriceRecord priceRecord) {
        this.commodity_id = priceRecord.getCommodity().getId();
        this.price = priceRecord.getPrice();
        this.date = priceRecord.getLastUpdated().toString(); // format as needed
    }

    // getters (Retrofit uses these for serialization)
    public int getCommodity_id() { return commodity_id; }
    public double getPrice() { return price; }
    public String getDate() { return date; }
}
