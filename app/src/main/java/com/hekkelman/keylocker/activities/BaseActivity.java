package com.hekkelman.keylocker.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.LocaleList;

import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.utilities.Settings;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Base application
 */
public class BaseActivity extends AppCompatActivity {

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

    public static class ScreenOffReceiver extends BroadcastReceiver {
        public IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
                KeyDb.onReceivedScreenOff();
        }
    }
}
