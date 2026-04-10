package ru.mirea.ivanovrr.mireaproject;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

public class Worker extends androidx.work.Worker {
    private static final String TAG = "MireaProjectWorker";

    public Worker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Фоновая задача началась");
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e(TAG, "Фоновая задача прервана", e);
            return Result.failure();
        }

        Log.d(TAG, "Фоновая задача успешно завершена");
        return Result.success();
    }
}
