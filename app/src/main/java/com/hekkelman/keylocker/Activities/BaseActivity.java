// ------------------------------------------------------------------------------
// Copyright (c) 2015 Microsoft Corporation
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
// ------------------------------------------------------------------------------

package com.hekkelman.keylocker.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;

//import com.onedrive.sdk.authentication.MSAAuthenticator;
//import com.onedrive.sdk.concurrency.ICallback;
//import com.onedrive.sdk.core.ClientException;
//import com.onedrive.sdk.core.DefaultClientConfig;
//import com.onedrive.sdk.core.IClientConfig;
//import com.onedrive.sdk.extensions.IOneDriveClient;
//import com.onedrive.sdk.extensions.OneDriveClient;
//import com.onedrive.sdk.logger.LoggerLevel;

import androidx.appcompat.app.AppCompatActivity;

import com.hekkelman.keylocker.Utilities.Settings;

import java.util.Locale;

/**
 * Base application
 */
public class BaseActivity extends AppCompatActivity {

    public class ScreenOffReceiver extends BroadcastReceiver {
        public IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                if (mBroadcastReceivedCallback != null) {
                    mBroadcastReceivedCallback.onReceivedScreenOff();
                }
                if (shouldDestroyOnScreenOff()) {
                    finish();
                }
            }
        }
    }

    interface BroadcastReceivedCallback {
        void onReceivedScreenOff();
    }

    public Settings mSettings;
    private ScreenOffReceiver mScreenOffReceiver;
    private BroadcastReceivedCallback mBroadcastReceivedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSettings = new Settings(this);

//        setTheme(settings.getTheme());
        setLocale();

        //Set navigation bar color
//        getWindow().setNavigationBarColor(Tools.getThemeColor(this,R.attr.navigationBarColor));

        super.onCreate(savedInstanceState);

        mScreenOffReceiver = new ScreenOffReceiver();
        registerReceiver(mScreenOffReceiver, mScreenOffReceiver.filter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mScreenOffReceiver);
        super.onDestroy();
    }

    public void setBroadcastCallback(BroadcastReceivedCallback cb) {
        this.mBroadcastReceivedCallback = cb;
    }

    protected boolean shouldDestroyOnScreenOff() {
        return true;
    }


    @Override
    public void onResume() {
        setLocale();

        super.onResume();
    }

    public void setLocale() {
        Locale locale = mSettings.getLocale();

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();

        LocaleList localeList = new LocaleList(locale);
        LocaleList.setDefault(localeList);
        config.setLocale(locale);
        config.setLocales(localeList);
        createConfigurationContext(config);
    }

    //
////    /**
////     * The service instance
////     */
////    private final AtomicReference<IOneDriveClient> mClient = new AtomicReference<>();
////
////    /**
////     * The system connectivity manager
////     */
////    private ConnectivityManager mConnectivityManager;
//
//    /**
//     * What to do when the application starts
//     */
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//
//    }
//
//    /**
//     * Create the client configuration
//     * @return the newly created configuration
//     */
//    private IClientConfig createConfig() {
//
//        final MSAAuthenticator msaAuthenticator = new MSAAuthenticator() {
//            @Override
//            public String getClientId() {
//                return "0000000040177E44";
//            }
//
//            @Override
//            public String[] getScopes() {
//                return new String[] {"onedrive.readwrite", "onedrive.appfolder", "wl.offline_access"};
//            }
//        };
//
//        final IClientConfig config = DefaultClientConfig.createWithAuthenticator(msaAuthenticator);
//        config.getLogger().setLoggingLevel(LoggerLevel.Debug);
//        return config;
//    }
//
//    /**
//     * Navigates the user to the wifi settings if there is a connection problem
//     *
//     * @return if the wifi activity was navigated to
//     */
//    synchronized boolean goToWifiSettingsIfDisconnected() {
//        final NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
//        if (info == null || !info.isConnected()) {
//            Toast.makeText(this, getString(R.string.wifi_unavailable_error_message), Toast.LENGTH_LONG).show();
//            final Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * Clears out the auth token from the application store
//     */
//    void signOut() {
//        if (mClient.get() == null) {
//            return;
//        }
//        mClient.get().getAuthenticator().logout(new ICallback<Void>() {
//            @Override
//            public void success(final Void result) {
//                mClient.set(null);
//                final Intent intent = new Intent(getBaseContext(), MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }
//
//            @Override
//            public void failure(final ClientException ex) {
//                Toast.makeText(getBaseContext(), "Logout error " + ex, Toast.LENGTH_LONG).show();
//            }
//        });
//    }
//
//    /**
//     * Get an instance of the service
//     *
//     * @return The Service
//     */
//    synchronized IOneDriveClient getOneDriveClient() {
//        if (mClient.get() == null) {
//            throw new UnsupportedOperationException("Unable to generate a new service object");
//        }
//        return mClient.get();
//    }
//
//    /**
//     * Used to setup the Services
//     * @param activity the current activity
//     * @param serviceCreated the callback
//     */
//    synchronized void createOneDriveClient(final Activity activity, final ICallback<Void> serviceCreated) {
//        final DefaultCallback<IOneDriveClient> callback = new DefaultCallback<IOneDriveClient>(activity) {
//            @Override
//            public void success(final IOneDriveClient result) {
//                mClient.set(result);
//                serviceCreated.success(null);
//            }
//
//            @Override
//            public void failure(final ClientException error) {
//                serviceCreated.failure(error);
//            }
//        };
//        new OneDriveClient
//            .Builder()
//            .fromConfig(createConfig())
//            .loginAndBuildClient(activity, callback);
//    }
}
