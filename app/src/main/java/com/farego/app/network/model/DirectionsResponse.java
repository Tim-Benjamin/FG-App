package com.farego.app.network.model;
// ============================================================
// FILE: app/src/main/java/com/farego/app/network/model/DirectionsResponse.java
// PURPOSE: Deserializes ORS Directions API response.
//          Extracts road distance (metres) and encoded polyline for map.
// ============================================================

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DirectionsResponse {

    @SerializedName("routes")
    public List<Route> routes;

    public static class Route {
        @SerializedName("summary")
        public Summary summary;

        @SerializedName("geometry")
        public String geometry;   // Encoded polyline string for MapLibre
    }

    public static class Summary {
        @SerializedName("distance")
        public double distance;   // Metres — divide by 1000 for km

        @SerializedName("duration")
        public double duration;   // Seconds
    }

    /** Convenience: returns distance in kilometres */
    public double getDistanceKm() {
        if (routes == null || routes.isEmpty()) return 0;
        return routes.get(0).summary.distance / 1000.0;
    }

    /** Convenience: returns encoded polyline for route drawing */
    public String getEncodedGeometry() {
        if (routes == null || routes.isEmpty()) return "";
        return routes.get(0).geometry;
    }

    /** Duration in minutes */
    public double getDurationMinutes() {
        if (routes == null || routes.isEmpty()) return 0;
        return routes.get(0).summary.duration / 60.0;
    }
}