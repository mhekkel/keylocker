package com.hekkelman.keylocker;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.hekkelman.keylocker.utilities.AppContainer;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

public class KeyLockerApp extends Application {
    public AppContainer mAppContainer;
    private ScreenOffReceiver mScreenOffReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppContainer = new AppContainer(this);

        mScreenOffReceiver = new ScreenOffReceiver(mAppContainer);
        registerReceiver(mScreenOffReceiver, mScreenOffReceiver.filter);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                mAppContainer.locked.setValue(true);
            }
        });
    }

    @Override
    public void onTerminate() {
        if (mScreenOffReceiver != null)
            unregisterReceiver(mScreenOffReceiver);
        super.onTerminate();
    }

    public static class ScreenOffReceiver extends BroadcastReceiver {
        public IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        private final AppContainer appContainer;

        public ScreenOffReceiver(AppContainer appContainer) {
            this.appContainer = appContainer;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                appContainer.locked.setValue(true);
            }
        }
    }

}
