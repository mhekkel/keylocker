package com.hekkelman.keylocker.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.hekkelman.keylocker.Activities.MainActivity;
import com.hekkelman.keylocker.R;

import java.util.Locale;

public class Settings {
    private final Context context;
    private final SharedPreferences settings;

    public enum TapMode { NOTHING, REVEAL, COPY, COPY_BACKGROUND, SEND_KEYSTROKES };

    public Settings(Context context) {
        this.context = context;
        this.settings = PreferenceManager.getDefaultSharedPreferences(context);
//
//        migrateDeprecatedSettings();
    }

    private String getResString(int resId) {
        return context.getString(resId);
    }

    private int getResInt(int resId) {
        return context.getResources().getInteger(resId);
    }

    private String getString(int keyId, int defaultId) {
        return settings.getString(getResString(keyId), getResString(defaultId));
    }

    public boolean getBoolean(int keyId, boolean defaultValue) {
        return settings.getBoolean(getResString(keyId), defaultValue);
    }

    private int getInt(int keyId, int defaultId) {
        return settings.getInt(getResString(keyId), getResInt(defaultId));
    }

    private int getIntValue(int keyId, int defaultValue) {
        return settings.getInt(getResString(keyId), defaultValue);
    }

    public Locale getLocale() {
        String lang = getString(R.string.settings_key_lang, R.string.settings_default_lang);

        if (lang.equals("system")) {
            return Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            String[] splitLang =  lang.split("_");

            if (splitLang.length > 1) {
                return new Locale(splitLang[0], splitLang[1]);
            } else {
                return new Locale(lang);
            }
        }
    }

    public void registerPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        settings.registerOnSharedPreferenceChangeListener(listener);
    }

    public boolean getScreenshotsEnabled() {
        return getBoolean(R.string.settings_key_enable_screenshot, false);
    }

    public boolean getRelockOnScreenOff() {
        return getBoolean(R.string.settings_key_relock_screen_off, true);
    }

    public boolean getRelockOnBackground() {
        return getBoolean(R.string.settings_key_relock_background, false);
    }

    public boolean isMinimizeAppOnCopyEnabled() {
        return getBoolean(R.string.settings_key_minimize_on_copy, false);
    }

    public TapMode getTapSingle() {
        String singleTap = getString(R.string.settings_key_tap_single, R.string.settings_default_tap_single);
        return TapMode.valueOf(singleTap.toUpperCase(Locale.ENGLISH));
    }

    public TapMode getTapDouble() {
        String doubleTap = getString(R.string.settings_key_tap_double, R.string.settings_default_tap_double);
        return TapMode.valueOf(doubleTap.toUpperCase(Locale.ENGLISH));
    }

    public boolean getTapToReveal() {
        return getTapSingle() == TapMode.REVEAL || getTapDouble() == TapMode.REVEAL;
    }

    public int getAuthInactivityDelay() {
        return getIntValue(R.string.settings_key_auth_inactivity_delay, 0);
    }

    public boolean getAuthInactivity() {
        return getBoolean(R.string.settings_key_auth_inactivity, false);
    }

    public boolean getUsePin() {
        return getBoolean(R.string.settings_key_use_pin, false);
    }

    public boolean getBlockAccessibility() {
        return getBoolean(R.string.settings_key_block_accessibility, false);
    }

    public boolean getBlockAutofill() {
        return getBoolean(R.string.settings_key_block_autofill, false);
    }


}
