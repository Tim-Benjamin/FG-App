package com.farego.app.network.model;
// ============================================================
// FILE: app/src/main/java/com/farego/app/network/model/FareRateResponse.java
// PURPOSE: Gson deserialization model for JSON from fare_rates.php
//          Maps to Room FareRate entity via FareRateRepository.
// ============================================================

import com.google.gson.annotations.SerializedName;

public class FareRateResponse {

    @SerializedName("id")
    public int id;

    @SerializedName("transport_type")
    public String transportType;

    @SerializedName("base_rate")
    public double baseRate;

    @SerializedName("per_km_rate")
    public double perKmRate;

    @SerializedName("minimum_fare")
    public double minimumFare;

    @SerializedName("last_updated")
    public long lastUpdated;
}