package com.example.openmarket.utility;

import com.example.openmarket.model.CommodityPrice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataUtils {
    private static final Map<String, List<CommodityPrice>> priceCache = new HashMap<>();
    private static List<String> commoditiesList;

    static {
        initializeSampleData();
    }

    public static void initializeSampleData() {
        commoditiesList = Arrays.asList(
                "Maize",
                "Rice",
                "Tomatoes"
        );

        priceCache.put("Maize", generateMaizePrices());
        priceCache.put("Rice", generateRicePrices());
        priceCache.put("Tomatoes", generateTomatoPrices());
    }

    public static List<String> getAllCommodities() {
        return new ArrayList<>(commoditiesList);
    }

    public static List<CommodityPrice> getPricesForCommodity(String commodity) {
        if (commodity == null || commodity.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<CommodityPrice> prices = priceCache.get(commodity);

        if(prices != null) {
            return new ArrayList<>(prices);
        }
        return new ArrayList<>();
    }

  /** public static CommodityPrice getLatestPrice(String commodity) {
        List<CommodityPrice> prices = getPricesForCommodity(commodity);
        if(!prices.isEmpty()){
            return prices.get(prices.size() - 1);
        }
        return null;
    } */

    public static Map<String, Float> getPriceStatistics(String commodity) {
        Map<String,Float>  stats = new HashMap<>();
        List<CommodityPrice> prices = getPricesForCommodity(commodity);

        if(prices.isEmpty()) {
            stats.put("min", 0f);
            stats.put("max", 0f);
            stats.put("avg", 0f);
            stats.put("trends", 0f);
            return stats;
        }

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        float total = 0;

        for (CommodityPrice price : prices) {
            float p = price.getPrice();
            min = Math.min(min, p);
            max = Math.max(max, p);
            total += p;
        }
        float avg = total / prices.size();

        float trend = 0f;
        if (prices.size() > 1) {
            float firstPrice = prices.get(0).getPrice();
            float lastPrice = prices.get(prices.size() - 1).getPrice();
            trend = ((lastPrice - firstPrice) / firstPrice) * 100;

        }
        stats.put("min", min);
        stats.put("max", max);
        stats.put("avg", avg);
        stats.put("trend", trend);
        return stats;
    }

    public static void clearCache() {
        priceCache.clear();
        initializeSampleData();
    }

    private static List<CommodityPrice> generateMaizePrices(){
        List<CommodityPrice> prices = new ArrayList<>();

        prices.add(new CommodityPrice("2025-09-01", 120));
        prices.add(new CommodityPrice("2025-09-08", 125));
        prices.add(new CommodityPrice("2025-09-15", 140));
        prices.add(new CommodityPrice("2025-09-22", 135));
        prices.add(new CommodityPrice("2025-09-29", 160));
        prices.add(new CommodityPrice("2025-10-06", 150));

        return prices;
    }

    private static List<CommodityPrice> generateRicePrices(){
        List<CommodityPrice> prices = new ArrayList<>();

        prices.add(new CommodityPrice("2025-09-01", 180));
        prices.add(new CommodityPrice("2025-09-08", 175));
        prices.add(new CommodityPrice("2025-09-15", 190));
        prices.add(new CommodityPrice("2025-09-22", 155));
        prices.add(new CommodityPrice("2025-09-29", 150));
        prices.add(new CommodityPrice("2025-10-06", 165));

        return prices;
    }
    private static List<CommodityPrice> generateTomatoPrices(){
        List<CommodityPrice> prices = new ArrayList<>();

        prices.add(new CommodityPrice("2025-09-01", 50));
        prices.add(new CommodityPrice("2025-09-08", 350));
        prices.add(new CommodityPrice("2025-09-15", 200));
        prices.add(new CommodityPrice("2025-09-22", 165));
        prices.add(new CommodityPrice("2025-09-29", 190));
        prices.add(new CommodityPrice("2025-10-06", 250));

        return prices;
    }

}

