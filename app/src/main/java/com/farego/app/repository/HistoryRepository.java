package com.farego.app.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.farego.app.db.AppDatabase;
import com.farego.app.db.dao.SearchHistoryDao;
import com.farego.app.db.entity.SearchHistory;
import java.util.List;

public class HistoryRepository {

    private final SearchHistoryDao dao;

    public HistoryRepository(Context context) {
        this.dao = AppDatabase.getInstance(context).searchHistoryDao();
    }

    /** Observed by UserDashboardFragment — auto-refreshes RecyclerView */
    public LiveData<List<SearchHistory>> getHistoryForUser(int userId) {
        return dao.getHistoryForUser(userId);
    }

    /** Called by FareViewModel on every successful fare calculation */
    public void recordSearch(int userId,
                             String originLabel, String destinationLabel,
                             double distanceKm, double estimatedFare,
                             String transportType) {
        AppDatabase.dbExecutor.execute(() -> dao.insertHistory(
                new SearchHistory(
                        userId, originLabel, destinationLabel,
                        distanceKm, estimatedFare, transportType,
                        System.currentTimeMillis()
                )
        ));
    }

    public void clearHistory(int userId) {
        AppDatabase.dbExecutor.execute(() -> dao.clearHistoryForUser(userId));
    }
}