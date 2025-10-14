package com.example.openmarket.model;

import androidx.annotation.NonNull;

import com.example.openmarket.utility.Unit;
public class Commodity {
    private int id;
    private final String name;
    private final Unit unit;

    public Commodity (String name, Unit unit) {
        this.name = name;
        this.unit = unit;
    }

    public int getId() { return id; }

    public String getName() { return name; }

    public Unit getUnit() { return unit; }
    
    public void setId(int id) { this.id = id; }

    @NonNull
    @Override
    public String toString() { return name; }
}
