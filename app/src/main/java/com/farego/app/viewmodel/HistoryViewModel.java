package com.farego.app.viewmodel;
// ============================================================
// FILE: app/src/main/java/com/farego/app/viewmodel/HistoryViewModel.java
// PURPOSE: Drives UserDashboardFragment RecyclerView.
// ============================================================

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.farego.app.db.entity.SearchHistory;
import com.farego.app.repository.HistoryRepository;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {

    private final HistoryRepository repo;
    private LiveData<List<SearchHistory>> history;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        repo = new HistoryRepository(application);
    }

    public LiveData<List<SearchHistory>> getHistory(int userId) {
        if (history == null) {
            history = repo.getHistoryForUser(userId);
        }
        return history;
    }

    public void clearHistory(int userId) {
        repo.clearHistory(userId);
    }
}