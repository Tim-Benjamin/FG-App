package com.farego.app.network;
// ============================================================
// FILE: app/src/main/java/com/farego/app/network/OrsApiService.java
// PURPOSE: Retrofit interface for OpenRouteService API.
//          Base URL: https://api.openrouteservice.org
//          Used for: geocoding autocomplete + road distance.
// ============================================================

import com.farego.app.network.model.DirectionsResponse;
import com.farego.app.network.model.GeocodeResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface OrsApiService {

    /**
     * Forward geocoding — convert a search query to lat/lng coordinates.
     * Used for the origin/destination autocomplete search bars.
     *
     * Example call:
     * GET /geocode/search?api_key=...&text=Kaneshie&boundary.country=GH&size=5
     */
    @GET("geocode/search")
    Call<GeocodeResponse> searchPlaces(
            @Header("Authorization") String apiKey,
            @Query("text")             String searchText,
            @Query("boundary.country") String countryCode,  // "GH" for Ghana
            @Query("size")             int    resultCount   // max suggestions to return
    );

    /**
     * Road directions between two coordinates.
     * Returns distance in metres and encoded polyline for map drawing.
     *
     * Coordinates format: "longitude,latitude" e.g. "-0.2057,5.5502"
     */
    @GET("v2/directions/driving-car")
    Call<DirectionsResponse> getDirections(
            @Header("Authorization") String apiKey,
            @Query("start")          String originCoords,       // "lng,lat"
            @Query("end")            String destinationCoords   // "lng,lat"
    );
}