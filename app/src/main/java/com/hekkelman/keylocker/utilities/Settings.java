package com.hekkelman.keylocker.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.hekkelman.keylocker.R;

import java.util.Locale;
import java.util.Set;

public class Settings {
    private final Context context;
    private final SharedPreferences settings;

    public enum TapMode {NOTHING, EDIT, COPY, COPY_BACKGROUND, SEND_KEYSTROKES}
    public enum UnlockKeyboardMode { TEXT, DIGITS }

    public Settings(Context context) {
        this.context = context;
        this.settings = PreferenceManager.getDefaultSharedPreferences(context);
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
        return Integer.parseInt(settings.getString(getResString(keyId), Integer.toString(defaultValue)));
    }

    public void setBoolean(int keyId, boolean value) {
        settings.edit()
                .putBoolean(getResString(keyId), value)
                .apply();
    }

    @SuppressWarnings("SameParameterValue")
    private void setInt(int keyId, int value) {
        settings.edit()
                .putInt(getResString(keyId), value)
                .apply();
    }

    private void setString(int keyId, String value) {
        settings.edit()
                .putString(getResString(keyId), value)
                .apply();
    }

    @SuppressWarnings("SameParameterValue")
    private void setStringSet(int keyId, Set<String> value) {
        settings.edit()
                .putStringSet(getResString(keyId), value)
                .apply();
    }

    public void registerPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        settings.registerOnSharedPreferenceChangeListener(listener);
    }

    public boolean getRelockOnBackground() {
        return getBoolean(R.string.settings_key_relock_background, true);
    }

    public boolean getAndroidBackupServiceEnabled() {
        return getBoolean(R.string.settings_key_enable_android_backup_service, true);
    }

    public TapMode getTapSingle() {
        String singleTap = getString(R.string.settings_key_tap_single, R.string.settings_default_tap_single);
        return TapMode.valueOf(singleTap.toUpperCase(Locale.ENGLISH));
    }

    public TapMode getTapDouble() {
        String doubleTap = getString(R.string.settings_key_tap_double, R.string.settings_default_tap_double);
        return TapMode.valueOf(doubleTap.toUpperCase(Locale.ENGLISH));
    }

    public UnlockKeyboardMode getUnlockKeyboard() {
        String unlockKeyboard = getString(R.string.settings_key_unlock_keyboard, R.string.settings_default_unlock_keyboard);
        return UnlockKeyboardMode.valueOf(unlockKeyboard.toUpperCase(Locale.ENGLISH));
    }

    public void setUnlockKeyboard(UnlockKeyboardMode mode) {
        setString(R.string.settings_key_unlock_keyboard, mode.toString());
    }

    public int getAuthInactivityDelay() {
        return getIntValue(R.string.settings_key_auth_inactivity_delay, 0);
    }

    public boolean getAuthInactivity() {
        return getBoolean(R.string.settings_key_auth_inactivity, false);
    }

    public boolean getBlockAccessibility() {
        return getBoolean(R.string.settings_key_block_accessibility, false);
    }

    public boolean getBlockAutofill() {
        return getBoolean(R.string.settings_key_block_autofill, false);
    }

    public String getLocalBackupDir() {
        return getString(R.string.settings_key_backup_dir, R.string.empty_string);
    }

    public void setLocalBackupDir(String localBackupDir) {
        setString(R.string.settings_key_backup_dir, localBackupDir);
    }

    public int getGeneratedPasswordLength() {
        return getIntValue(R.string.settings_key_password_length, 12);
    }

    public boolean getGeneratedPasswordNoAmbiguous() {
        return getBoolean(R.string.settings_key_password_not_ambiguous, true);
    }

    public boolean getGeneratedPasswordIncludeCapitals() {
        return getBoolean(R.string.settings_key_password_include_capitals, true);
    }

    public boolean getGeneratedPasswordIncludeDigits() {
        return getBoolean(R.string.settings_key_password_include_digits, true);
    }

    public boolean getGeneratedPasswordIncludeSymbols() {
        return getBoolean(R.string.settings_key_password_include_symbols, true);
    }

    public String getWebDAVBackupKeyID() {
        return getString(R.string.settings_key_backup_webdav_key, R.string.empty_string);
    }

    public void setWebDAVBackupKeyID(String keyID) {
        setString(R.string.settings_key_backup_webdav_key, keyID);
    }
}
