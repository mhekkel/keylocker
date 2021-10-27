package com.hekkelman.keylocker.utilities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppContainer {
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Handler getMainThreadHandler() {
        return mainThreadHandler;
    }
}
