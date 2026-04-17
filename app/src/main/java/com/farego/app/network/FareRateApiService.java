package com.farego.app.network;
// ============================================================
// FILE: app/src/main/java/com/farego/app/network/FareRateApiService.java
// PURPOSE: Retrofit interface for XAMPP PHP REST API.
//          Base URL: http://{your-pc-ip}/farego/
//          GET endpoints are public; POST/PUT/DELETE need X-Admin-Key.
// ============================================================

import com.farego.app.network.model.ApiResponse;
import com.farego.app.network.model.FareRateRequest;
import com.farego.app.network.model.FareRateResponse;
import com.farego.app.network.model.LastUpdatedResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface FareRateApiService {

    // ── Public (no auth) ─────────────────────────────────────

    /** Full fare rates — used to populate Room cache after stale check */
    @GET("fare_rates.php")
    Call<List<FareRateResponse>> getAllRates();

    /** Lightweight sync check — returns only the latest last_updated ms */
    @GET("fare_rates.php?meta=last_updated")
    Call<LastUpdatedResponse> getLastUpdated();

    // ── Admin only (X-Admin-Key header required) ──────────────

    @POST("fare_rates.php")
    Call<ApiResponse> createRate(
            @Header("X-Admin-Key") String adminKey,
            @Body FareRateRequest body
    );

    @PUT("fare_rates.php")
    Call<ApiResponse> updateRate(
            @Header("X-Admin-Key") String adminKey,
            @Query("id")           int id,
            @Body FareRateRequest  body
    );

    @DELETE("fare_rates.php")
    Call<ApiResponse> deleteRate(
            @Header("X-Admin-Key") String adminKey,
            @Query("id")           int id
    );
}