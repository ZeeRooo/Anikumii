package com.zeerooo.anikumii.services;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MyAnimeListUpdater extends Worker {
    public MyAnimeListUpdater(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        context.startService(new Intent(context, MALApiService.class).putExtra("action", (byte) 3));
    }

    @NonNull
    @Override
    public Result doWork() {
        return Result.success();
    }
}
