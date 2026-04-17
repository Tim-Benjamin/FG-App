package com.farego.app.ui;
// ============================================================
// FILE: app/src/main/java/com/farego/app/ui/FareResultBottomSheet.java
// PURPOSE: Slide-up bottom sheet showing TroTro / Taxi / Uber fare
//          cards. Matches the mockup exactly — gold accents, dark
//          surface cards, low/mid/high range labels.
//          Confirm button dismisses sheet and activates route mode.
// ============================================================

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.farego.app.R;
import com.farego.app.databinding.BottomSheetFareResultBinding;
import com.farego.app.utils.FareCalculator;
import com.farego.app.viewmodel.FareViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.Serializable;

public class FareResultBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_RESULTS = "fare_results";

    private BottomSheetFareResultBinding binding;
    private FareViewModel                fareVm;

    // ── Static factory — pass results as Serializable array ──
    public static FareResultBottomSheet newInstance(FareCalculator.FareResult[] results) {
        FareResultBottomSheet sheet = new FareResultBottomSheet();
        Bundle args = new Bundle();
        // Flatten results into bundle primitives for safe passing
        args.putDouble("trotro_low",  results[0].low);
        args.putDouble("trotro_mid",  results[0].mid);
        args.putDouble("trotro_high", results[0].high);
        args.putDouble("taxi_low",    results[1].low);
        args.putDouble("taxi_mid",    results[1].mid);
        args.putDouble("taxi_high",   results[1].high);
        args.putDouble("uber_low",    results[2].low);
        args.putDouble("uber_mid",    results[2].mid);
        args.putDouble("uber_high",   results[2].high);
        args.putDouble("distance_km", results[0].distanceKm);
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = BottomSheetFareResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fareVm = new ViewModelProvider(requireActivity()).get(FareViewModel.class);

        Bundle args = requireArguments();
        double distanceKm = args.getDouble("distance_km");

        // ── Populate distance label ────────────────────────────
        binding.tvDistance.setText(String.format("%.1f km", distanceKm));

        // ── TroTro card ────────────────────────────────────────
        double tLow  = args.getDouble("trotro_low");
        double tHigh = args.getDouble("trotro_high");
        binding.tvTrotroFare.setText(formatRange(tLow, tHigh));
        binding.tvTrotroLabel.setText("5 min away");

        // ── Taxi card ──────────────────────────────────────────
        double xLow  = args.getDouble("taxi_low");
        double xHigh = args.getDouble("taxi_high");
        binding.tvTaxiFare.setText(formatRange(xLow, xHigh));
        binding.tvTaxiLabel.setText("Comfort");

        // ── Uber card ──────────────────────────────────────────
        double uLow  = args.getDouble("uber_low");
        double uHigh = args.getDouble("uber_high");
        binding.tvUberFare.setText(formatRange(uLow, uHigh));
        binding.tvUberLabel.setText("Comfort");

        // ── Card selection highlight ───────────────────────────
        binding.cardTrotro.setOnClickListener(v -> selectCard(0));
        binding.cardTaxi.setOnClickListener(v   -> selectCard(1));
        binding.cardUber.setOnClickListener(v   -> selectCard(2));
        selectCard(0); // TroTro selected by default

        // ── Confirm button — dismiss sheet, activate route ─────
        binding.btnConfirm.setOnClickListener(v -> {
            fareVm.confirmRoute();
            dismiss();
        });
    }

    private void selectCard(int index) {
        int goldStroke  = requireContext().getColor(R.color.colorPrimary);
        int clearStroke = requireContext().getColor(android.R.color.transparent);

        binding.cardTrotro.setStrokeColor(index == 0 ? goldStroke : clearStroke);
        binding.cardTaxi.setStrokeColor(index == 1   ? goldStroke : clearStroke);
        binding.cardUber.setStrokeColor(index == 2   ? goldStroke : clearStroke);

        binding.cardTrotro.setStrokeWidth(index == 0 ? 3 : 0);
        binding.cardTaxi.setStrokeWidth(index == 1   ? 3 : 0);
        binding.cardUber.setStrokeWidth(index == 2   ? 3 : 0);
    }

    private String formatRange(double low, double high) {
        if (low == high) return String.format("GH₵ %.0f", low);
        return String.format("GH₵ %.0f – %.0f", low, high);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}