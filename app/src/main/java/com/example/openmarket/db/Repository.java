package com.example.openmarket.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.openmarket.model.Commodity;
import com.example.openmarket.model.PriceRecord;
import com.example.openmarket.utility.DatabaseHelper;
import com.example.openmarket.utility.LocalDateParser;
import com.example.openmarket.utility.Unit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

public class Repository {
    private static final String TAG = "Repository";

    private final DatabaseHelper dbHelper;
    private final RemoteRepository remoteRepo;

    private static Repository instance;

    private static final List<Commodity> commodities = new ArrayList<>();
    private static final List<PriceRecord> priceRecords = new ArrayList<>();

    private Repository(Context context) {
        dbHelper = new DatabaseHelper(context.getApplicationContext());
        remoteRepo = new RemoteRepository(context);

        if (isCommoditiesEmpty()) {
            seedLocalData();
        }

        fetchRemoteData();
    }

    public static synchronized Repository getInstance(Context context) {
        if (instance == null) {
            instance = new Repository(context);
        }
        return instance;
    }

    /** ------------------- PUBLIC METHODS ------------------- **/

    public static List<Commodity> getCommodities(Context context) {
        return getInstance(context).getCommoditiesInternal();
    }

    public static void addCommodity(Context context, Commodity commodity) {
        Repository repo = getInstance(context);
        repo.addCommodityInternal(commodity);

        repo.remoteRepo.addCommodity(commodity, new RemoteRepository.SimpleCallback() {
            @Override public void onSuccess() {}
            @Override public void onFailure(String error) { Log.e(TAG, error); }
        });
    }

    public static boolean deleteCommodity(Context context, Commodity commodity) {
        Repository repo = getInstance(context);
        boolean localDeleted = repo.deleteCommodityInternal(commodity);

        // Remote delete
        repo.remoteRepo.api.deleteCommodity(commodity.getId())
                .enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (!response.isSuccessful()) {
                            Log.e("RepoSync", "Failed to delete remote commodity: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Log.e("RepoSync", "Remote delete failed: " + Objects.requireNonNull(t.getMessage()));
                    }
                });

        return localDeleted;
    }

    public static boolean deletePrice(Context context, PriceRecord priceRecord) {
        Repository repo = getInstance(context);
        boolean localDeleted = repo.deletePriceInternal(priceRecord.getCommodity().getId(),
                priceRecord.getLastUpdated().toString());

        // Remote delete
        repo.remoteRepo.api.deletePrice(priceRecord.getId())
                .enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (!response.isSuccessful()) {
                            Log.e("RepoSync", "Failed to delete remote price: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Log.e("RepoSync", "Remote delete failed: " + Objects.requireNonNull(t.getMessage()));
                    }
                });

        return localDeleted;
    }




    public static List<PriceRecord> getPricesForCommodity(Context context, Commodity commodity) {
        return getInstance(context).getPricesForCommodityInternal(commodity.getId());
    }

    public static void addPrice(Context context, PriceRecord priceRecord) {
        Repository repo = getInstance(context);
        repo.addPriceInternal(priceRecord);

        repo.remoteRepo.addPrice(priceRecord, new RemoteRepository.SimpleCallback() {
            @Override public void onSuccess() {}
            @Override public void onFailure(String error) { Log.e(TAG, error); }
        });
    }

    /** ------------------- INTERNAL METHODS ------------------- **/

    private void seedLocalData() {
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

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Commodity c : commodities) addCommodityInternal(c);
            for (PriceRecord p : priceRecords) addPriceInternal(p);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void fetchRemoteData() {
        remoteRepo.fetchCommodities(new RemoteRepository.FetchCallback() {
            @Override
            public void onSuccess(List<Commodity> data) {
                for (Commodity remoteCommodity : data) {
                    if (!existsLocally(remoteCommodity)) {
                        addCommodityInternal(remoteCommodity);
                    }
                }

                // After syncing commodities, fetch prices for all
                fetchRemotePrices();
            }

            @Override
            public void onFailure(String error) {
                Log.e("RepoSync", "Failed to fetch remote commodities: " + error);
            }
        });
    }

    private void fetchRemotePrices() {
        for (Commodity c : getCommoditiesInternal()) {
            remoteRepo.api.getPricesForCommodity(c.getId())
                    .enqueue(new retrofit2.Callback<List<PriceRecord>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<PriceRecord>> call, @NonNull Response<List<PriceRecord>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                for (PriceRecord pr : response.body()) {
                                    boolean exists = priceRecords.stream()
                                            .anyMatch(p -> p.getCommodity().getId() == c.getId() && p.getLastUpdated().equals(pr.getLastUpdated()));
                                    if (!exists) {
                                        pr.setCommodity(c);
                                        addPriceInternal(pr);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<List<PriceRecord>> call, @NonNull Throwable t) {
                            Log.e("RepoSync", "Failed to fetch prices for " + c.getName() + ": " + Objects.requireNonNull(t.getMessage()));
                        }
                    });
        }
    }

    private boolean existsLocally(Commodity commodity) {
        for (Commodity c : commodities) {
            if (c.getName().equalsIgnoreCase(commodity.getName())) return true;
        }
        return false;
    }

    /** ------------------- DATABASE METHODS ------------------- **/

    private void addCommodityInternal(Commodity commodity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", commodity.getName());
        values.put("unit", commodity.getUnit() != null ? commodity.getUnit().toString() : Unit.DEFAULT.toString());
        long id = db.insert("commodities", null, values);
        commodity.setId((int) id);
        commodities.add(commodity);
    }

    private List<Commodity> getCommoditiesInternal() {
        List<Commodity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM commodities", null);
        if (cursor.moveToFirst()) {
            do {
                Commodity c = new Commodity(
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        Unit.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("unit")))
                );
                c.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                list.add(c);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private boolean deleteCommodityInternal(Commodity commodity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deleted = db.delete("commodities", "id = ?", new String[]{String.valueOf(commodity.getId())});
        if (deleted > 0) {
            commodities.removeIf(c -> c.getId() == commodity.getId());
            priceRecords.removeIf(p -> p.getCommodity().getId() == commodity.getId());
        }
        return deleted > 0;
    }

    private void addPriceInternal(PriceRecord pr) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("commodity_id", pr.getCommodity().getId());
        values.put("price", pr.getPrice());
        // fallback to today if date is null
        LocalDate date = pr.getLastUpdated() != null ? pr.getLastUpdated() : LocalDate.now();
        values.put("date", date.toString());
        db.insert("price_records", null, values);
    }

    private List<PriceRecord> getPricesForCommodityInternal(int commodityId) {
        List<PriceRecord> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM price_records WHERE commodity_id = ?", new String[]{String.valueOf(commodityId)});
        if (cursor.moveToFirst()) {
            do {
                PriceRecord pr = new PriceRecord(
                        getCommodityInternal(commodityId),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        LocalDateParser.parseDate(cursor.getString(cursor.getColumnIndexOrThrow("date")))
                );
                pr.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                list.add(pr);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private boolean deletePriceInternal(int commodityId, String dateText) {
        LocalDate date = LocalDateParser.parseDate(dateText);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deleted = db.delete("price_records", "commodity_id = ? AND date = ?", new String[]{String.valueOf(commodityId), date.toString()});
        if (deleted > 0) {
            priceRecords.removeIf(p -> p.getCommodity().getId() == commodityId && p.getLastUpdated().equals(date));
        }
        return deleted > 0;
    }

    private Commodity getCommodityInternal(int id) {
        for (Commodity c : commodities) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    private boolean isCommoditiesEmpty() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM commodities", null);
        boolean empty = true;
        if (cursor.moveToFirst()) empty = cursor.getInt(0) == 0;
        cursor.close();
        return empty;
    }
}
