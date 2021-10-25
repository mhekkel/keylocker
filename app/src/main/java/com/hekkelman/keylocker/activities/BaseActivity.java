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

public class BaseActivity extends AppCompatActivity {

    public Settings settings;
    private ScreenOffReceiver screenOffReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = new Settings(this);

        super.onCreate(savedInstanceState);

        if (settings.getRelockOnScreenOff()) {
            screenOffReceiver = new ScreenOffReceiver();
            registerReceiver(screenOffReceiver, screenOffReceiver.filter);
        }
    }

    @Override
    protected void onDestroy() {
        if (screenOffReceiver != null)
            unregisterReceiver(screenOffReceiver);
        super.onDestroy();
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
