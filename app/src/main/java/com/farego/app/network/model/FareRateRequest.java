package com.farego.app.network.model;
// ============================================================
// FILE: app/src/main/java/com/farego/app/network/model/FareRateRequest.java
// PURPOSE: Request body for admin POST and PUT to fare_rates.php
// ============================================================

import com.google.gson.annotations.SerializedName;

public class FareRateRequest {

    @SerializedName("transport_type")
    public String transportType;

    @SerializedName("base_rate")
    public double baseRate;

    @SerializedName("per_km_rate")
    public double perKmRate;

    @SerializedName("minimum_fare")
    public double minimumFare;

    public FareRateRequest(String transportType, double baseRate,
                           double perKmRate, double minimumFare) {
        this.transportType = transportType;
        this.baseRate      = baseRate;
        this.perKmRate     = perKmRate;
        this.minimumFare   = minimumFare;
    }
}