package com.hekkelman.keylocker;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.hekkelman.keylocker.utilities.AppContainer;
import com.hekkelman.keylocker.utilities.Settings;

public class KeyLockerApp extends Application {
    public AppContainer mAppContainer;
    public Settings mSettings;
    private ScreenOffReceiver mScreenOffReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppContainer = new AppContainer(this);
        mSettings = new Settings(this);

        mScreenOffReceiver = new ScreenOffReceiver(mAppContainer, mSettings);
        registerReceiver(mScreenOffReceiver, mScreenOffReceiver.filter);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                if (mSettings.getRelockOnBackground())
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
        private final AppContainer mAppContainer;
        private final Settings mSettings;
        public IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);

        public ScreenOffReceiver(AppContainer appContainer, Settings mSettings) {
            this.mAppContainer = appContainer;
            this.mSettings = mSettings;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                if (mSettings.getRelockOnBackground())
                    mAppContainer.locked.setValue(true);
            }
        }
    }
}
