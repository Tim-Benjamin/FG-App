package com.farego.app.db.entity;
// ============================================================
// FILE: app/src/main/java/com/farego/app/db/entity/FareRate.java
// PURPOSE: Room entity mirroring the remote MySQL fare_rates table.
//          This is the local cache used for all fare calculations.
// ============================================================

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fare_rates")
public class FareRate {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "transport_type")
    public String transportType;   // "TroTro", "Taxi", "Uber"

    @ColumnInfo(name = "base_rate")
    public double baseRate;

    @ColumnInfo(name = "per_km_rate")
    public double perKmRate;

    @ColumnInfo(name = "minimum_fare")
    public double minimumFare;

    @ColumnInfo(name = "last_updated")
    public long lastUpdated;       // Unix epoch milliseconds

    // ── Convenience constructor ───────────────────────────────
    public FareRate(String transportType, double baseRate,
                    double perKmRate, double minimumFare, long lastUpdated) {
        this.transportType = transportType;
        this.baseRate       = baseRate;
        this.perKmRate      = perKmRate;
        this.minimumFare    = minimumFare;
        this.lastUpdated    = lastUpdated;
    }
}