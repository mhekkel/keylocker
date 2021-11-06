package com.hekkelman.keylocker;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

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

    /**
     * The system connectivity manager
     */
    private ConnectivityManager mConnectivityManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

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

    /**
     * Navigates the user to the wifi settings if there is a connection problem
     *
     * @return if the wifi activity was navigated to
     */
    synchronized public boolean goToWifiSettingsIfDisconnected() {
        final NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
//            Toast.makeText(this, getString(R.string.wifi_unavailable_error_message), Toast.LENGTH_LONG).show();
            final Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }
        return false;
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
