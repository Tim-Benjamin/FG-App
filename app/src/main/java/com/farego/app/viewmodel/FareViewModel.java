package com.farego.app.viewmodel;
// ============================================================
// FILE: app/src/main/java/com/farego/app/viewmodel/FareViewModel.java
// PURPOSE: Drives HomeFragment and FareResultBottomSheet.
//          Holds selected locations, triggers ORS distance call,
//          calculates fares from Room rates, and exposes results.
// ============================================================

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.farego.app.BuildConfig;
import com.farego.app.db.entity.FareRate;
import com.farego.app.network.RetrofitClient;
import com.farego.app.network.model.DirectionsResponse;
import com.farego.app.network.model.GeocodeResponse;
import com.farego.app.repository.FareRateRepository;
import com.farego.app.repository.HistoryRepository;
import com.farego.app.utils.FareCalculator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FareViewModel extends AndroidViewModel {

    // ── Repositories ──────────────────────────────────────────
    private final FareRateRepository  fareRateRepo;
    private final HistoryRepository   historyRepo;

    // ── Room source ───────────────────────────────────────────
    public final LiveData<List<FareRate>> fareRates;

    // ── Selected locations (set by HomeFragment search) ───────
    private final MutableLiveData<GeocodeResponse.Feature> origin      = new MutableLiveData<>();
    private final MutableLiveData<GeocodeResponse.Feature> destination = new MutableLiveData<>();

    // ── Geocode search suggestions ────────────────────────────
    private final MutableLiveData<List<GeocodeResponse.Feature>> suggestions = new MutableLiveData<>();

    // ── Fare results (set after ORS + calculation) ────────────
    private final MutableLiveData<FareCalculator.FareResult[]> fareResults = new MutableLiveData<>();

    // ── Route geometry for MapLibre (encoded polyline) ────────
    private final MutableLiveData<String> routeGeometry = new MutableLiveData<>();

    // ── Distance in km (shown in UI) ─────────────────────────
    private final MutableLiveData<Double> distanceKm = new MutableLiveData<>();

    // ── Loading / error states ────────────────────────────────
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String>  errorMsg  = new MutableLiveData<>();

    // ── UI mode: SEARCHING vs ROUTE_ACTIVE ───────────────────
    public enum MapMode { SEARCHING, ROUTE_ACTIVE }
    private final MutableLiveData<MapMode> mapMode = new MutableLiveData<>(MapMode.SEARCHING);

    public FareViewModel(@NonNull Application application) {
        super(application);
        fareRateRepo = new FareRateRepository(application);
        historyRepo  = new HistoryRepository(application);
        fareRates    = fareRateRepo.getAllRates();
    }

    // ── Exposed LiveData ──────────────────────────────────────
    public LiveData<List<GeocodeResponse.Feature>> getSuggestions() { return suggestions; }
    public LiveData<FareCalculator.FareResult[]>   getFareResults()  { return fareResults; }
    public LiveData<String>                         getRouteGeometry(){ return routeGeometry; }
    public LiveData<Double>                         getDistanceKm()   { return distanceKm; }
    public LiveData<Boolean>                        getIsLoading()    { return isLoading; }
    public LiveData<String>                         getErrorMsg()     { return errorMsg; }
    public LiveData<MapMode>                        getMapMode()      { return mapMode; }
    public LiveData<GeocodeResponse.Feature>        getOrigin()       { return origin; }
    public LiveData<GeocodeResponse.Feature>        getDestination()  { return destination; }

    // ── Location setters (called when user picks a suggestion) ─
    public void setOrigin(GeocodeResponse.Feature f)      { origin.setValue(f); }
    public void setDestination(GeocodeResponse.Feature f) { destination.setValue(f); }

    // ── Geocode search ────────────────────────────────────────
    public void searchPlaces(String query) {
        if (query.trim().length() < 2) {
            suggestions.setValue(null);
            return;
        }
        RetrofitClient.getOrsService()
                .searchPlaces(BuildConfig.ORS_API_KEY, query, "GH", 6)
                .enqueue(new Callback<GeocodeResponse>() {
                    @Override
                    public void onResponse(Call<GeocodeResponse> c, Response<GeocodeResponse> r) {
                        if (r.isSuccessful() && r.body() != null) {
                            suggestions.postValue(r.body().features);
                        }
                    }
                    @Override public void onFailure(Call<GeocodeResponse> c, Throwable t) {
                        errorMsg.postValue("Search failed: " + t.getMessage());
                    }
                });
    }

    // ── Calculate fare (main action) ──────────────────────────
    /**
     * Triggered when user taps "Calculate Fare".
     * Flow: ORS directions → distance → Room rates → FareCalculator → emit results
     */
    public void calculateFare(int userId, String originLabel, String destLabel) {
        GeocodeResponse.Feature o = origin.getValue();
        GeocodeResponse.Feature d = destination.getValue();
        if (o == null || d == null) {
            errorMsg.setValue("Please select both origin and destination");
            return;
        }

        isLoading.setValue(true);

        String startCoords = o.geometry.getLongitude() + "," + o.geometry.getLatitude();
        String endCoords   = d.geometry.getLongitude() + "," + d.geometry.getLatitude();

        RetrofitClient.getOrsService()
                .getDirections(BuildConfig.ORS_API_KEY, startCoords, endCoords)
                .enqueue(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> c, Response<DirectionsResponse> r) {
                        isLoading.postValue(false);
                        if (!r.isSuccessful() || r.body() == null) {
                            errorMsg.postValue("Could not get directions. Check your API key.");
                            return;
                        }
                        double km = r.body().getDistanceKm();
                        distanceKm.postValue(km);
                        routeGeometry.postValue(r.body().getEncodedGeometry());

                        // Get rates from Room (current cached value)
                        List<FareRate> rates = fareRates.getValue();
                        if (rates == null || rates.size() < 3) {
                            errorMsg.postValue("Fare rates not loaded. Please try again.");
                            return;
                        }

                        FareRate trotro = null, taxi = null, uber = null;
                        for (FareRate rate : rates) {
                            switch (rate.transportType) {
                                case "TroTro": trotro = rate; break;
                                case "Taxi":   taxi   = rate; break;
                                case "Uber":   uber   = rate; break;
                            }
                        }
                        if (trotro == null || taxi == null || uber == null) return;

                        FareCalculator.FareResult[] results = FareCalculator.calculateAll(
                                km,
                                trotro.baseRate, trotro.perKmRate, trotro.minimumFare,
                                taxi.baseRate,   taxi.perKmRate,   taxi.minimumFare,
                                uber.baseRate,   uber.perKmRate,   uber.minimumFare
                        );
                        fareResults.postValue(results);

                        // Record in search history (mid estimate for TroTro)
                        historyRepo.recordSearch(userId, originLabel, destLabel,
                                km, results[0].mid, "TroTro");
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> c, Throwable t) {
                        isLoading.postValue(false);
                        errorMsg.postValue("Network error: " + t.getMessage());
                    }
                });
    }

    /** Called when user taps Confirm — switches map to route tracking mode */
    public void confirmRoute() {
        mapMode.setValue(MapMode.ROUTE_ACTIVE);
    }

    /** Called when user taps Back / New Search */
    public void resetSearch() {
        mapMode.setValue(MapMode.SEARCHING);
        fareResults.setValue(null);
        routeGeometry.setValue(null);
        origin.setValue(null);
        destination.setValue(null);
        distanceKm.setValue(null);
    }

    // ── Sync trigger (called from HomeFragment.onResume) ──────
    public void syncFareRates(Runnable onOfflineEmptyCache) {
        fareRateRepo.syncIfStale(onOfflineEmptyCache);
    }
}