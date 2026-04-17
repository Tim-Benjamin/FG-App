package com.farego.app.db.dao;
// ============================================================
// FILE: app/src/main/java/com/farego/app/db/dao/SearchHistoryDao.java
// PURPOSE: Room DAO for search_history table.
// ============================================================

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.farego.app.db.entity.SearchHistory;
import java.util.List;

@Dao
public interface SearchHistoryDao {

    /** Observed by UserDashboardFragment — auto-updates RecyclerView */
    @Query("SELECT * FROM search_history WHERE user_id = :userId ORDER BY timestamp DESC")
    LiveData<List<SearchHistory>> getHistoryForUser(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistory(SearchHistory history);

    @Query("DELETE FROM search_history WHERE user_id = :userId")
    void clearHistoryForUser(int userId);
}