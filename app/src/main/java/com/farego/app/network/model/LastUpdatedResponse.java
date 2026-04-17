package com.farego.app.network.model;
// ============================================================
// FILE: app/src/main/java/com/farego/app/network/model/LastUpdatedResponse.java
// PURPOSE: Response model for ?meta=last_updated sync check endpoint.
// ============================================================

import com.google.gson.annotations.SerializedName;

public class LastUpdatedResponse {
    @SerializedName("last_updated")
    public long lastUpdated;
}