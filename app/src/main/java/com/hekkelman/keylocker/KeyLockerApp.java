package com.hekkelman.keylocker;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.core.os.HandlerCompat;

import com.hekkelman.keylocker.utilities.AppContainer;

public class KeyLockerApp extends Application {
    public AppContainer appContainer = new AppContainer();
}
