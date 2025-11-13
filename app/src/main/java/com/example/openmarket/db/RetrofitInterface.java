package com.example.openmarket.db;

import com.example.openmarket.model.Commodity;
import com.example.openmarket.model.PriceRecord;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetrofitInterface {

    @GET("commodities/")
    Call<List<Commodity>> getCommodities();

    @POST("commodities/")
    Call<Commodity> addCommodity(@Body Commodity commodity);

    // Use the new DTO here
    @POST("prices/")
    Call<PriceRecord> addPrice(@Body PriceRecordRequest priceRecordRequest);

    @DELETE("commodities/{id}/")
    Call<Void> deleteCommodity(@Path("id") int commodityId);


    @DELETE("prices/{id}/")
    Call<Void> deletePrice(@Body PriceRecord priceRecord);

    // Pass commodity ID as query parameter
    @GET("prices/")
    Call<List<PriceRecord>> getPricesForCommodity(@retrofit2.http.Query("commodity_id") int commodityId);
}
