package com.farego.app.utils;
// ============================================================
// FILE: app/src/main/java/com/farego/app/utils/FareCalculator.java
// PURPOSE: Pure fare calculation logic — no Android or Room imports.
//          Fully unit-testable with JUnit. Values come from Room
//          (synced from MySQL) at call time.
// ============================================================

public class FareCalculator {

    // ── Result container ──────────────────────────────────────
    public static class FareResult {
        public final double low;
        public final double mid;
        public final double high;
        public final String transportType;
        public final double distanceKm;

        public FareResult(String transportType, double distanceKm,
                          double low, double mid, double high) {
            this.transportType = transportType;
            this.distanceKm    = distanceKm;
            this.low           = low;
            this.mid           = mid;
            this.high          = high;
        }

        /** Formatted GH₵ range string e.g. "GH₵ 15 – 20" */
        public String formatRange() {
            return String.format("GH₵ %.0f – %.0f", low, high);
        }
    }

    private FareCalculator() { /* utility class */ }

    /**
     * Calculates fare for a given transport type.
     *
     * Formula: fare = baseRate + (distanceKm × perKmRate)
     * Floor:   Math.max(fare, minimumFare)
     * Variance:
     *   - TroTro: ±15% (real-world negotiation range)
     *   - Taxi / Uber: fixed (no negotiation)
     *
     * @param transportType  "TroTro", "Taxi", or "Uber"
     * @param distanceKm     road distance in kilometres (from ORS)
     * @param baseRate       from Room FareRate
     * @param perKmRate      from Room FareRate
     * @param minimumFare    from Room FareRate
     * @return FareResult with low, mid, high estimates
     */
    public static FareResult calculate(String transportType,
                                       double distanceKm,
                                       double baseRate,
                                       double perKmRate,
                                       double minimumFare) {
        double mid = Math.max(baseRate + (distanceKm * perKmRate), minimumFare);

        double low, high;

        if ("TroTro".equalsIgnoreCase(transportType)) {
            // ±15% variance reflects real-world negotiation
            low  = Math.max(mid * 0.85, minimumFare);
            high = mid * 1.15;
        } else {
            // Taxi and Uber are metered — fixed fare, no variance
            low  = mid;
            high = mid;
        }

        // Round to nearest whole cedi for clean display
        low  = Math.round(low);
        mid  = Math.round(mid);
        high = Math.round(high);

        return new FareResult(transportType, distanceKm, low, mid, high);
    }

    /**
     * Convenience: calculates fares for all three transport types at once.
     * Called by FareViewModel after Room query returns all three rates.
     */
    public static FareResult[] calculateAll(double distanceKm,
                                            double trotroBase, double trotroPerKm, double trotroMin,
                                            double taxiBase,   double taxiPerKm,   double taxiMin,
                                            double uberBase,   double uberPerKm,   double uberMin) {
        return new FareResult[]{
                calculate("TroTro", distanceKm, trotroBase, trotroPerKm, trotroMin),
                calculate("Taxi",   distanceKm, taxiBase,   taxiPerKm,   taxiMin),
                calculate("Uber",   distanceKm, uberBase,   uberPerKm,   uberMin),
        };
    }
}