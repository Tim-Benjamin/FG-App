package com.farego.app.ui;
// ============================================================
// FILE: app/src/main/java/com/farego/app/ui/HomeFragment.java
// PURPOSE: Core map screen. Uses MapLibre GL Android SDK v11
//          (package: org.maplibre.android.*)
//          Live GPS dot, search bars, fare calc, route drawing.
// ============================================================

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.farego.app.BuildConfig;
import com.farego.app.R;
import com.farego.app.databinding.FragmentHomeBinding;
import com.farego.app.network.model.GeocodeResponse;
import com.farego.app.utils.SessionManager;
import com.farego.app.viewmodel.FareViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// ── MapLibre GL Android v11 — org.maplibre.android.* ─────────
import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.CircleLayer;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.Property;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int    LOCATION_PERM_REQ = 1001;
    private static final String ROUTE_SOURCE      = "route-source";
    private static final String ROUTE_LAYER       = "route-layer";
    private static final String USER_SOURCE       = "user-source";
    private static final String USER_LAYER        = "user-layer";
    private static final String PULSE_LAYER       = "pulse-layer";
    private static final LatLng GHANA_CENTER      = new LatLng(7.9465, -1.0232);

    private FragmentHomeBinding         binding;
    private FareViewModel               fareVm;
    private MapLibreMap                 map;
    private MapView                     mapView;
    private FusedLocationProviderClient fusedClient;
    private LocationCallback            locationCallback;
    private SessionManager              session;
    private List<GeocodeResponse.Feature> suggestions = new ArrayList<>();
    private boolean searchingOrigin = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MapLibre.getInstance(requireContext());
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fareVm  = new ViewModelProvider(requireActivity()).get(FareViewModel.class);
        session = new SessionManager(requireContext());
        setupMap(savedInstanceState);
        setupSearchBars();
        setupButtons();
        observeViewModel();
        fareVm.syncFareRates(() ->
                Snackbar.make(view, "Offline — using cached rates", Snackbar.LENGTH_SHORT).show());
    }

    // ── Map ───────────────────────────────────────────────────
    private void setupMap(Bundle savedState) {
        mapView = binding.mapView;
        mapView.onCreate(savedState);
        String url = "https://api.maptiler.com/maps/dataviz-dark/style.json?key="
                + BuildConfig.MAPTILER_KEY;
        mapView.getMapAsync(m -> {
            map = m;
            map.setStyle(new Style.Builder().fromUri(url), style -> {
                map.setCameraPosition(new CameraPosition.Builder()
                        .target(GHANA_CENTER).zoom(6.5).build());
                addRouteLayer(style);
                addUserLayer(style);
                requestLocation();
            });
        });
    }

    private void addRouteLayer(Style style) {
        style.addSource(new GeoJsonSource(ROUTE_SOURCE));
        style.addLayer(new LineLayer(ROUTE_LAYER, ROUTE_SOURCE).withProperties(
                PropertyFactory.lineColor(Color.parseColor("#FFD700")),
                PropertyFactory.lineWidth(5f),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineOpacity(0.9f)
        ));
    }

    private void addUserLayer(Style style) {
        style.addSource(new GeoJsonSource(USER_SOURCE));
        style.addLayer(new CircleLayer(PULSE_LAYER, USER_SOURCE).withProperties(
                PropertyFactory.circleRadius(16f),
                PropertyFactory.circleColor(Color.parseColor("#FFD700")),
                PropertyFactory.circleOpacity(0.2f)
        ));
        style.addLayer(new CircleLayer(USER_LAYER, USER_SOURCE).withProperties(
                PropertyFactory.circleRadius(8f),
                PropertyFactory.circleColor(Color.parseColor("#FFD700")),
                PropertyFactory.circleStrokeWidth(2f),
                PropertyFactory.circleStrokeColor(Color.WHITE)
        ));
    }

    // ── GPS ───────────────────────────────────────────────────
    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERM_REQ);
        }
    }

    private void startLocationUpdates() {
        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        LocationRequest req = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(1500).build();
        locationCallback = new LocationCallback() {
            @Override public void onLocationResult(@NonNull LocationResult r) {
                android.location.Location loc = r.getLastLocation();
                if (loc != null) moveUserDot(loc.getLatitude(), loc.getLongitude());
            }
        };
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedClient.requestLocationUpdates(req, locationCallback, Looper.getMainLooper());
        }
    }

    private void moveUserDot(double lat, double lng) {
        if (map == null || map.getStyle() == null) return;
        GeoJsonSource src = (GeoJsonSource) map.getStyle().getSource(USER_SOURCE);
        if (src == null) return;
        JsonObject f = new JsonObject();
        f.addProperty("type", "Feature");
        JsonObject g = new JsonObject();
        g.addProperty("type", "Point");
        JsonArray c = new JsonArray(); c.add(lng); c.add(lat);
        g.add("coordinates", c);
        f.add("geometry", g);
        f.add("properties", new JsonObject());
        src.setGeoJson(f.toString());
    }

    // ── Search bars ───────────────────────────────────────────
    private void setupSearchBars() {
        binding.etOrigin.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(Editable s){}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c) {
                searchingOrigin = true; fareVm.searchPlaces(s.toString());
            }
        });
        binding.etDestination.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(Editable s){}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c) {
                searchingOrigin = false; fareVm.searchPlaces(s.toString());
            }
        });
        binding.lvSuggestions.setOnItemClickListener((p, v, pos, id) -> {
            if (pos >= suggestions.size()) return;
            GeocodeResponse.Feature sel = suggestions.get(pos);
            if (searchingOrigin) {
                fareVm.setOrigin(sel);
                binding.etOrigin.setText(sel.properties.name);
            } else {
                fareVm.setDestination(sel);
                binding.etDestination.setText(sel.properties.name);
            }
            binding.lvSuggestions.setVisibility(View.GONE);
            if (map != null) map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(sel.geometry.getLatitude(), sel.geometry.getLongitude()), 13), 900);
        });
    }

    // ── Buttons ───────────────────────────────────────────────
    private void setupButtons() {
        binding.btnCalculateFare.setOnClickListener(v -> fareVm.calculateFare(
                session.getUserId(),
                binding.etOrigin.getText().toString().trim(),
                binding.etDestination.getText().toString().trim()));
        binding.btnNewSearch.setOnClickListener(v -> fareVm.resetSearch());
        binding.btnDashboard.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_home_to_dashboard));
    }

    // ── Observers ─────────────────────────────────────────────
    private void observeViewModel() {
        fareVm.getSuggestions().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                binding.lvSuggestions.setVisibility(View.GONE); return;
            }
            suggestions = list;
            List<String> labels = new ArrayList<>();
            for (GeocodeResponse.Feature f : list)
                labels.add(f.properties.label != null ? f.properties.label : f.properties.name);
            binding.lvSuggestions.setAdapter(
                    new ArrayAdapter<>(requireContext(), R.layout.item_suggestion, labels));
            binding.lvSuggestions.setVisibility(View.VISIBLE);
        });

        fareVm.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnCalculateFare.setEnabled(!loading);
        });

        fareVm.getErrorMsg().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty())
                Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show();
        });

        fareVm.getFareResults().observe(getViewLifecycleOwner(), results -> {
            if (results != null)
                FareResultBottomSheet.newInstance(results)
                        .show(getChildFragmentManager(), "FareResult");
        });

        fareVm.getMapMode().observe(getViewLifecycleOwner(), mode -> {
            boolean active = mode == FareViewModel.MapMode.ROUTE_ACTIVE;
            binding.cardSearch.setVisibility(active ? View.GONE : View.VISIBLE);
            binding.btnNewSearch.setVisibility(active ? View.VISIBLE : View.GONE);
        });

        fareVm.getRouteGeometry().observe(getViewLifecycleOwner(), geo -> {
            if (geo != null && map != null && map.getStyle() != null) drawRoute(geo);
        });
    }

    // ── Route drawing ─────────────────────────────────────────
    private void drawRoute(String encoded) {
        List<LatLng> pts = decodePolyline(encoded);
        if (pts.isEmpty() || map == null || map.getStyle() == null) return;
        GeoJsonSource src = (GeoJsonSource) map.getStyle().getSource(ROUTE_SOURCE);
        if (src == null) return;
        JsonObject f = new JsonObject();
        f.addProperty("type", "Feature");
        JsonObject g = new JsonObject(); g.addProperty("type", "LineString");
        JsonArray coords = new JsonArray();
        for (LatLng p : pts) {
            JsonArray c = new JsonArray(); c.add(p.getLongitude()); c.add(p.getLatitude());
            coords.add(c);
        }
        g.add("coordinates", coords);
        f.add("geometry", g); f.add("properties", new JsonObject());
        src.setGeoJson(f.toString());
        if (pts.size() > 1) {
            LatLngBounds.Builder b = new LatLngBounds.Builder();
            for (LatLng p : pts) b.include(p);
            try { map.animateCamera(CameraUpdateFactory.newLatLngBounds(b.build(), 120), 1200);
            } catch (Exception ignored) {}
        }
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> pts = new ArrayList<>();
        int idx = 0, lat = 0, lng = 0;
        while (idx < encoded.length()) {
            int b, shift = 0, result = 0;
            do { b = encoded.charAt(idx++) - 63; result |= (b & 0x1f) << shift; shift += 5;
            } while (b >= 0x20);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            shift = 0; result = 0;
            do { b = encoded.charAt(idx++) - 63; result |= (b & 0x1f) << shift; shift += 5;
            } while (b >= 0x20);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            pts.add(new LatLng(lat / 1e5, lng / 1e5));
        }
        return pts;
    }

    // ── Permission ────────────────────────────────────────────
    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] perms,
                                           @NonNull int[] grants) {
        if (req == LOCATION_PERM_REQ && grants.length > 0
                && grants[0] == PackageManager.PERMISSION_GRANTED) startLocationUpdates();
    }

    // ── MapLibre lifecycle (all 7 required) ───────────────────
    @Override public void onStart()   { super.onStart();   if(mapView!=null) mapView.onStart(); }
    @Override public void onResume()  {
        super.onResume();
        if(mapView!=null) mapView.onResume();
        fareVm.syncFareRates(null);
    }
    @Override public void onPause()   {
        super.onPause();
        if(mapView!=null) mapView.onPause();
        if(fusedClient!=null && locationCallback!=null)
            fusedClient.removeLocationUpdates(locationCallback);
    }
    @Override public void onStop()    { super.onStop();    if(mapView!=null) mapView.onStop(); }
    @Override public void onDestroy() { super.onDestroy(); if(mapView!=null) mapView.onDestroy(); }
    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out); if(mapView!=null) mapView.onSaveInstanceState(out);
    }
    @Override public void onLowMemory() {
        super.onLowMemory(); if(mapView!=null) mapView.onLowMemory();
    }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}