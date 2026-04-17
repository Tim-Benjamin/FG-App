package com.farego.app.db.dao;
// ============================================================
// FILE: app/src/main/java/com/farego/app/db/dao/FareRateDao.java
// PURPOSE: Room DAO for fare_rates table.
//          LiveData return = automatic UI refresh on data change.
// ============================================================

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.farego.app.db.entity.FareRate;
import java.util.List;

@Dao
public interface FareRateDao {

    // ── Read ──────────────────────────────────────────────────

    /** All rates — observed by FareResultBottomSheet via LiveData */
    @Query("SELECT * FROM fare_rates ORDER BY id ASC")
    LiveData<List<FareRate>> getAllRates();

    /** Single rate by transport type — used by FareCalculator */
    @Query("SELECT * FROM fare_rates WHERE transport_type = :type LIMIT 1")
    FareRate getRateByType(String type);

    /** Latest last_updated timestamp — used for sync check */
    @Query("SELECT MAX(last_updated) FROM fare_rates")
    long getLatestTimestamp();

    // ── Write ─────────────────────────────────────────────────

    /** Bulk insert after a full sync from the server */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<FareRate> rates);

    /** Wipe all local rates before re-inserting fresh data from server */
    @Query("DELETE FROM fare_rates")
    void clearAll();
}