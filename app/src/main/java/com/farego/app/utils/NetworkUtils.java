package com.farego.app.utils;
// ============================================================
// FILE: app/src/main/java/com/farego/app/utils/NetworkUtils.java
// PURPOSE: Check internet connectivity before API calls.
//          Shows Snackbar offline message via the calling fragment.
// ============================================================

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkUtils {

    private NetworkUtils() { /* utility class */ }

    /**
     * Returns true if the device has an active internet connection.
     * Supports API 23+ using NetworkCapabilities.
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null &&
                (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }
}