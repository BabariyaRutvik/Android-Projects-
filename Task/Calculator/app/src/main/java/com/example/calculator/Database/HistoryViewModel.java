package com.example.calculator.Database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private HistoryRepository repository;
    private LiveData<List<HistoryItem>> allHistory;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new HistoryRepository(application);
        allHistory = repository.getAllHistory();
    }

    public LiveData<List<HistoryItem>> getAllHistory() {
        return allHistory;
    }

    public void insert(HistoryItem item) {
        repository.insert(item);
    }

    public void delete(HistoryItem item) {
        repository.delete(item);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public void deleteSelected(List<Integer> ids) {
        repository.deleteSelected(ids);
    }

    public void prune(int limit) {
        repository.prune(limit);
    }
}
