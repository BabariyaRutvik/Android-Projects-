package com.example.calculator.Database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryRepository {
    private HistoryDao historyDao;
    private LiveData<List<HistoryItem>> allHistory;
    private ExecutorService executorService;

    public HistoryRepository(Application application) {
        HistoryDatabase db = HistoryDatabase.getInstance(application);
        historyDao = db.historyDao();
        allHistory = historyDao.getAllHistory();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<HistoryItem>> getAllHistory() {
        return allHistory;
    }

    public void insert(HistoryItem item) {
        executorService.execute(() -> historyDao.insert(item));
    }

    public void delete(HistoryItem item) {
        executorService.execute(() -> historyDao.delete(item));
    }

    public void deleteAll() {
        executorService.execute(() -> historyDao.deleteAll());
    }

    public void deleteSelected(List<Integer> ids) {
        executorService.execute(() -> historyDao.deleteSelected(ids));
    }
}
