package com.farego.app.db.entity;
// ============================================================
// FILE: app/src/main/java/com/farego/app/db/entity/SearchHistory.java
// PURPOSE: Room entity that records every fare calculation a user makes.
//          Displayed in UserDashboardFragment ordered by timestamp DESC.
// ============================================================

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx. room.Index;

@Entity(
        tableName  = "search_history",
        indices    = { @Index("user_id") },
        foreignKeys = @ForeignKey(
                entity        = User.class,
                parentColumns = "id",
                childColumns  = "user_id",
                onDelete      = ForeignKey.CASCADE
        )
)
public class SearchHistory {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "origin_label")
    public String originLabel;       // Human-readable origin name

    @ColumnInfo(name = "destination_label")
    public String destinationLabel;  // Human-readable destination name

    @ColumnInfo(name = "distance_km")
    public double distanceKm;

    @ColumnInfo(name = "estimated_fare")
    public double estimatedFare;     // Mid estimate stored for history

    @ColumnInfo(name = "transport_type")
    public String transportType;     // Which option was confirmed

    @ColumnInfo(name = "timestamp")
    public long timestamp;           // Unix epoch ms

    public SearchHistory(int userId, String originLabel, String destinationLabel,
                         double distanceKm, double estimatedFare,
                         String transportType, long timestamp) {
        this.userId           = userId;
        this.originLabel      = originLabel;
        this.destinationLabel = destinationLabel;
        this.distanceKm       = distanceKm;
        this.estimatedFare    = estimatedFare;
        this.transportType    = transportType;
        this.timestamp        = timestamp;
    }
}