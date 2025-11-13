package com.example.openmarket.db;

import android.content.Context;
import android.util.Log;

import com.example.openmarket.model.Commodity;
import com.example.openmarket.model.PriceRecord;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RemoteRepository {
    private static final String TAG = "RemoteRepository";
    final RetrofitInterface api;
    private final Context context;

    public RemoteRepository(Context context) {
        this.context = context;
        this.api = RetrofitClient.getClient().create(RetrofitInterface.class);
    }

    public void fetchCommodities(FetchCallback callback) {
        api.getCommodities().enqueue(new Callback<List<Commodity>>() {
            @Override
            public void onResponse(Call<List<Commodity>> call, Response<List<Commodity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Commodity>> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void addCommodity(Commodity commodity, SimpleCallback callback) {
        api.addCommodity(commodity).enqueue(new Callback<Commodity>() {
            @Override
            public void onResponse(Call<Commodity> call, Response<Commodity> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess();
                } else callback.onFailure("Failed to add commodity: " + response.code());
            }

            @Override
            public void onFailure(Call<Commodity> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void addPrice(PriceRecord priceRecord, SimpleCallback callback) {
        PriceRecordRequest request = new PriceRecordRequest(priceRecord);
        api.addPrice(request).enqueue(new Callback<PriceRecord>() {
            @Override
            public void onResponse(Call<PriceRecord> call, Response<PriceRecord> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess();
                } else callback.onFailure("Failed to add price: " + response.code());
            }

            @Override
            public void onFailure(Call<PriceRecord> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }


    public interface FetchCallback {
        void onSuccess(List<Commodity> data);
        void onFailure(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(String error);
    }
}

