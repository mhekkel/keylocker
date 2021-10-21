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

package com.hekkelman.keylocker.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.LocaleList;

//import com.onedrive.sdk.authentication.MSAAuthenticator;
//import com.onedrive.sdk.concurrency.ICallback;
//import com.onedrive.sdk.core.ClientException;
//import com.onedrive.sdk.core.DefaultClientConfig;
//import com.onedrive.sdk.core.IClientConfig;
//import com.onedrive.sdk.extensions.IOneDriveClient;
//import com.onedrive.sdk.extensions.OneDriveClient;
//import com.onedrive.sdk.logger.LoggerLevel;

import androidx.appcompat.app.AppCompatActivity;

import com.hekkelman.keylocker.utilities.Settings;
import com.hekkelman.keylocker.datamodel.KeyDb;

import java.util.Locale;

/**
 * Base application
 */
public class BaseActivity extends AppCompatActivity {

    public static class ScreenOffReceiver extends BroadcastReceiver {
        public IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
                KeyDb.onReceivedScreenOff();
        }
    }

    public Settings settings;
    private ScreenOffReceiver screenOffReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = new Settings(this);

//        setTheme(settings.getTheme());
        setLocale();

        //Set navigation bar color
//        getWindow().setNavigationBarColor(Tools.getThemeColor(this,R.attr.navigationBarColor));

        super.onCreate(savedInstanceState);

        screenOffReceiver = new ScreenOffReceiver();
        registerReceiver(screenOffReceiver, screenOffReceiver.filter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(screenOffReceiver);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        setLocale();
        super.onResume();
    }

    public void setLocale() {
        Locale locale = settings.getLocale();

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();

        LocaleList localeList = new LocaleList(locale);
        LocaleList.setDefault(localeList);
        config.setLocale(locale);
        config.setLocales(localeList);
        createConfigurationContext(config);
    }
}
