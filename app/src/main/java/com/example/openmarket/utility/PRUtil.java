package com.example.openmarket.utility;

import android.content.Context;

import java.time.LocalDate;
import com.example.openmarket.model.Commodity;
import com.example.openmarket.model.PriceRecord;
import com.example.openmarket.db.Repository;
import java.util.*;
import java.util.stream.Collectors;

public class PRUtil {
    private static final int MAX = 0;
    private static final int MIN = 1;
    private static PriceRecord getCurrentPrice(Context context, Commodity commodity) {
        List<PriceRecord> priceRecords = Repository.getPricesForCommodity(context, commodity);
        PriceRecord currentPrice = null;
        LocalDate latestDate = priceRecords.get(0).getLastUpdated();

        for (PriceRecord pr : priceRecords) {
            if (latestDate.isAfter(pr.getLastUpdated())) {
                latestDate = pr.getLastUpdated();
                currentPrice = pr;
            }
        }

        return currentPrice;
    }

    private static List<Double> getExtremes(Context context, Commodity commodity) {
        double max, min;
        List<PriceRecord> priceRecords = Repository.getPricesForCommodity(context, commodity);

        max = priceRecords.get(0).getPrice();
        min = priceRecords.get(0).getPrice();

        for (PriceRecord pr : priceRecords) {
            if (pr.getPrice() > max) {
                max = pr.getPrice();
            } else if (pr.getPrice() < min) {
                min = pr.getPrice();
            }
        }
        
        return Arrays.asList(max, min);
    }
    
    private static List<PriceRecord> getPricesSortedByDate(Context context, Commodity commodity) {
        List<PriceRecord> priceRecords = Repository.getPricesForCommodity(context, commodity);

        return priceRecords.stream()
                .sorted((a, b) -> a.getLastUpdated().compareTo(b.getLastUpdated()))
                .collect(Collectors.toList());
    }

    private static double getRecentPriceChange(Context context, Commodity commodity) {
        List<PriceRecord> priceRecords = getPricesSortedByDate(context, commodity);

        double current = priceRecords.get(0).getPrice();
        double previous = priceRecords.get(1).getPrice();

        return ((current - previous) / previous) * 100;
    }
}
