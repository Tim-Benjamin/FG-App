package com.farego.app.network.model;
// ============================================================
// FILE: app/src/main/java/com/farego/app/network/model/ApiResponse.java
// PURPOSE: Generic success/error response from fare_rates.php
//          for POST, PUT, DELETE operations.
// ============================================================

import com.google.gson.annotations.SerializedName;

public class ApiResponse {

    @SerializedName("success")
    public boolean success;

    @SerializedName("error")
    public String error;

    @SerializedName("id")
    public int id;

    @SerializedName("updated_id")
    public int updatedId;

    @SerializedName("deleted_id")
    public int deletedId;
}