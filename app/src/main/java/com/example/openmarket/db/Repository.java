package com.example.openmarket.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.openmarket.model.*;
import com.example.openmarket.utility.DatabaseHelper;
import com.example.openmarket.utility.LocalDateParser;
import com.example.openmarket.utility.Unit;
import java.util.*;
import java.time.LocalDate;

public class Repository {
    private final DatabaseHelper dbHelper;

    private static Repository instance;

    private static final List<Commodity> commodities = new ArrayList<>();
    private static final List<PriceRecord> priceRecords = new ArrayList<>();

    static {
        commodities.add(new Commodity("Maize", Unit.KILOGRAMS));
        commodities.add(new Commodity("Copper", Unit.TONNES));
        commodities.add(new Commodity("Mealie Meal", Unit.BAGS));
        commodities.add(new Commodity("Cooking Oil", Unit.LITRES));

        priceRecords.add(new PriceRecord(commodities.get(0), 123.5, LocalDate.now().minusDays(5)));
        priceRecords.add(new PriceRecord(commodities.get(0), 122.3, LocalDate.now().minusDays(3)));
        priceRecords.add(new PriceRecord(commodities.get(1), 1250, LocalDate.now().minusDays(5)));
        priceRecords.add(new PriceRecord(commodities.get(1), 1275, LocalDate.now().minusDays(4)));
        priceRecords.add(new PriceRecord(commodities.get(2), 15.7, LocalDate.now().minusDays(5)));
        priceRecords.add(new PriceRecord(commodities.get(2), 16, LocalDate.now().minusDays(2)));
    }

    private Repository(Context context) {
        dbHelper = new DatabaseHelper(context.getApplicationContext());

        if (isCommoditiesEmpty()) {
            for (Commodity commodity : commodities) {
                addCommodityInternal(commodity);
            }

            for (PriceRecord pr : priceRecords) {
                addPriceInternal(pr);
            }
        }
    }

    private static synchronized Repository getInstance(Context context) {
        if (instance == null) instance = new Repository(context);
        return instance;
    }

    public static void addPrice(Context context, PriceRecord priceRecord) {
        getInstance(context).addPriceInternal(priceRecord);
    }

    public static List<Commodity> getCommodities(Context context) {
        return getInstance(context).getCommoditiesInternal();
    }

    public static List<PriceRecord> getPricesForCommodity(Context context, Commodity commodity) {
        return getInstance(context).getPricesForCommodityInternal(commodity.getId());
    }

    public static void addCommodity(Context context, Commodity commodity) {
        getInstance(context).addCommodityInternal(commodity);
    }

    public static boolean deleteCommodity(Context context, Commodity commodity) {
        return getInstance(context).deleteCommodityInternal(commodity.getId());
    }

    public static void deletePrice(Context context, Commodity commodity, String dateText) {
        getInstance(context).deletePriceInternal(commodity.getId(), dateText);
    }

    private void addCommodityInternal(Commodity commodity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", commodity.getName());
        values.put("unit", commodity.getUnit().toString());
        long id = db.insert("commodities", null, values);
        commodity.setId((int) id);
    }

    private List<Commodity> getCommoditiesInternal() {
        List<Commodity> commodityList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM commodities", null);

        if (cursor.moveToFirst()) {
            do {
                Commodity c = new Commodity(
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        Unit.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("unit")))
                );
                c.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                commodityList.add(c);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return commodityList;
    }
    
    private Commodity getCommodity(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Commodity commodity = null;
        
        Cursor cursor = db.rawQuery("SELECT * FROM commodities WHERE id = ?",
                new String[] {String.valueOf(id)});
        
        if (cursor.moveToFirst()) {
            commodity = new Commodity(
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    Unit.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("unit")))
            );
            commodity.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        }

        cursor.close();
        return commodity;
    }

    private void addPriceInternal(PriceRecord pr) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("commodity_id", pr.getCommodity().getId());
        values.put("price", pr.getPrice());
        values.put("date", pr.getLastUpdated().toString());

        db.insert("price_records", null, values);
    }

    private List<PriceRecord> getPricesForCommodityInternal(int id) {
        List<PriceRecord> prices = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM price_records WHERE commodity_id = ?",
                new String[] {String.valueOf(id)}
        );
        
        if (cursor.moveToFirst()) {
            do {
                PriceRecord pr = new PriceRecord(
                        getCommodity(id),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        LocalDateParser.parseDate(cursor.getString(cursor.getColumnIndexOrThrow("date")))
                );
                pr.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                prices.add(pr);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return prices;
    }

    private boolean deletePriceInternal(int commodityId, String dateText) {
        LocalDate date = LocalDateParser.parseDate(dateText);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        assert date != null;
        int result = db.delete(
                "price_records",
                "commodity_id =  ? AND date = ?",
                new String[] {String.valueOf(commodityId), date.toString()}
        );
        return result > 0;
    }

    private boolean deleteCommodityInternal(int commodityId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int result = db.delete(
                "commodities",
                "id = ?",
                new String[] {String.valueOf(commodityId)}
        );
        return result > 0;
    }

    private boolean isCommoditiesEmpty() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM commodities", null);
        boolean isEmpty = true;

        if (cursor.moveToFirst()) {
            isEmpty = cursor.getInt(0) == 0;
        }
        cursor.close();
        return isEmpty;
    }


}
