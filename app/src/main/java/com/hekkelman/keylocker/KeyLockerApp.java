package com.hekkelman.keylocker;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.core.os.HandlerCompat;

public class KeyLockerApp extends Application {
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Handler getMainThreadHandler() {
        return mainThreadHandler;
    }
}
