package com.farego.app.network;
// ============================================================
// FILE: app/src/main/java/com/farego/app/network/RetrofitClient.java
// PURPOSE: Singleton factory for both Retrofit instances.
//          OrsApiService  → https://api.openrouteservice.org
//          FareRateApiService → http://{your-pc-ip}/farego/
// ============================================================

import com.farego.app.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static final String ORS_BASE_URL = "https://api.openrouteservice.org/";

    private static OrsApiService      orsService;
    private static FareRateApiService fareRateService;

    private RetrofitClient() { /* singleton */ }

    // ── Shared OkHttp client with logging ────────────────────
    private static OkHttpClient buildHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    // ── ORS instance ──────────────────────────────────────────
    public static synchronized OrsApiService getOrsService() {
        if (orsService == null) {
            orsService = new Retrofit.Builder()
                    .baseUrl(ORS_BASE_URL)
                    .client(buildHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(OrsApiService.class);
        }
        return orsService;
    }

    // ── FareGo XAMPP API instance ─────────────────────────────
    public static synchronized FareRateApiService getFareRateService() {
        if (fareRateService == null) {
            // Base URL from build config (set in local.properties)
            // e.g. http://192.168.1.105/farego/
            String baseUrl = BuildConfig.FAREGO_BASE_URL;
            if (!baseUrl.endsWith("/")) baseUrl += "/";

            fareRateService = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(buildHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(FareRateApiService.class);
        }
        return fareRateService;
    }
}