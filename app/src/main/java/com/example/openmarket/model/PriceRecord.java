package com.example.openmarket.model;

import java.time.LocalDate;
public class PriceRecord {
    private int id;
    private Commodity commodity;
     
    private final LocalDate lastUpdated;
    private final double price;

    public PriceRecord(Commodity commodity, double price, LocalDate lastUpdated) {
        this.commodity = commodity;
        this.price = price;
        this.lastUpdated = lastUpdated;
    }

    public int getId() { return id; }

    public Commodity getCommodity() { return commodity; }

    public double getPrice() { return price; }

    public LocalDate getLastUpdated() { return lastUpdated; }

    public void setId(int id) { this.id = id; }

    public void setCommodity(Commodity commodity) {
        this.commodity = commodity;
        setId(commodity.getId());
    }
}
