package com.example.bharatbuzz.Notification;

import android.content.Context;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NewsWorker extends Worker {


    public NewsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }



    @NonNull
    @Override
    public Result doWork() {
        Log.d("NewsWorker", "doWork() started");
        String title = getInputData().getString("title");
        String message = getInputData().getString("message");


        if (title == null)
        {
           title = "BharatBuzz Update!";

        }
        if (message == null){
            message = "Check out the latest news from BharatBuzz.";
        }
        NotificationHelper.showNotification(getApplicationContext(), title, message);
        return Result.success();
    }
}
