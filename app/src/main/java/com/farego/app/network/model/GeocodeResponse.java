package com.farego.app.network.model;
// ============================================================
// FILE: app/src/main/java/com/farego/app/network/model/GeocodeResponse.java
// PURPOSE: Deserializes ORS geocoding API response.
//          Used for autocomplete search suggestions.
// ============================================================

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeocodeResponse {

    @SerializedName("features")
    public List<Feature> features;

    public static class Feature {
        @SerializedName("properties")
        public Properties properties;

        @SerializedName("geometry")
        public Geometry geometry;
    }

    public static class Properties {
        @SerializedName("label")
        public String label;        // Full human-readable address

        @SerializedName("name")
        public String name;         // Short place name

        @SerializedName("country")
        public String country;
    }

    public static class Geometry {
        @SerializedName("coordinates")
        public List<Double> coordinates; // [longitude, latitude]

        public double getLongitude() { return coordinates.get(0); }
        public double getLatitude()  { return coordinates.get(1); }
    }
}