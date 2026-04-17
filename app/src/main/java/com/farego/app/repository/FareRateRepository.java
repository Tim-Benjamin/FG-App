package com.farego.app.repository;
// ============================================================
// FILE: app/src/main/java/com/farego/app/repository/FareRateRepository.java
// PURPOSE: Single source of truth for fare rates.
//          Sync strategy:
//            1. Fetch ?meta=last_updated from server
//            2. Compare to Room's latest timestamp
//            3. If server is newer → full fetch → clear Room → insert all
//            4. If offline or equal → use Room silently
//          All network calls run on AppDatabase.dbExecutor (off main thread).
// ============================================================

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.farego.app.db.AppDatabase;
import com.farego.app.db.dao.FareRateDao;
import com.farego.app.db.entity.FareRate;
import com.farego.app.network.FareRateApiService;
import com.farego.app.network.RetrofitClient;
import com.farego.app.network.model.FareRateResponse;
import com.farego.app.network.model.LastUpdatedResponse;
import com.farego.app.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FareRateRepository {

    private static final String TAG = "FareRateRepository";

    private final FareRateDao        dao;
    private final FareRateApiService api;
    private final Context            context;

    public FareRateRepository(Context context) {
        this.context = context.getApplicationContext();
        this.dao     = AppDatabase.getInstance(this.context).fareRateDao();
        this.api     = RetrofitClient.getFareRateService();
    }

    // ── Read ──────────────────────────────────────────────────

    /** Observed by FareViewModel — Room notifies on every change */
    public LiveData<List<FareRate>> getAllRates() {
        return dao.getAllRates();
    }

    // ── Sync ──────────────────────────────────────────────────

    /**
     * Call on every app foreground resume.
     * Lightweight: only downloads full dataset when actually stale.
     *
     * @param onOfflineWithEmptyCache called if offline AND no cached data exists
     */
    public void syncIfStale(Runnable onOfflineWithEmptyCache) {
        if (!NetworkUtils.isConnected(context)) {
            Log.d(TAG, "Offline — using Room cache");
            AppDatabase.dbExecutor.execute(() -> {
                long cached = dao.getLatestTimestamp();
                if (cached == 0 && onOfflineWithEmptyCache != null) {
                    onOfflineWithEmptyCache.run();
                }
            });
            return;
        }

        // Step 1: lightweight timestamp check
        api.getLastUpdated().enqueue(new Callback<LastUpdatedResponse>() {
            @Override
            public void onResponse(Call<LastUpdatedResponse> call,
                                   Response<LastUpdatedResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.w(TAG, "Timestamp check failed — keeping cache");
                    return;
                }
                long serverTs = response.body().lastUpdated;

                // Step 2: compare off main thread
                AppDatabase.dbExecutor.execute(() -> {
                    long localTs = dao.getLatestTimestamp();
                    if (serverTs > localTs) {
                        Log.d(TAG, "Stale cache — fetching full rates");
                        fetchAndReplaceAll();
                    } else {
                        Log.d(TAG, "Cache up to date (ts=" + localTs + ")");
                    }
                });
            }

            @Override
            public void onFailure(Call<LastUpdatedResponse> call, Throwable t) {
                Log.w(TAG, "Timestamp call failed: " + t.getMessage());
            }
        });
    }

    /** Step 3: download full rates and atomically replace Room data */
    private void fetchAndReplaceAll() {
        api.getAllRates().enqueue(new Callback<List<FareRateResponse>>() {
            @Override
            public void onResponse(Call<List<FareRateResponse>> call,
                                   Response<List<FareRateResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                List<FareRate> entities = new ArrayList<>();
                for (FareRateResponse r : response.body()) {
                    FareRate fr = new FareRate(
                            r.transportType, r.baseRate,
                            r.perKmRate, r.minimumFare, r.lastUpdated
                    );
                    fr.id = r.id;
                    entities.add(fr);
                }

                AppDatabase.dbExecutor.execute(() -> {
                    dao.clearAll();
                    dao.insertAll(entities);
                    Log.d(TAG, "Room updated with " + entities.size() + " rates");
                });
            }

            @Override
            public void onFailure(Call<List<FareRateResponse>> call, Throwable t) {
                Log.w(TAG, "Full fetch failed: " + t.getMessage());
            }
        });
    }

    // ── Admin write operations ────────────────────────────────
    // (Placeholder stubs — full admin section comes in a later phase)

    public interface WriteCallback {
        void onSuccess();
        void onError(String message);
    }
}