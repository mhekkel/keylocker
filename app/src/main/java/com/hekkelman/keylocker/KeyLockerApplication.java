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

package com.hekkelman.keylocker;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;
import android.util.LruCache;
import android.widget.Toast;

import androidx.security.crypto.MasterKeys;
import com.hekkelman.keylocker.datamodel.*;
import com.hekkelman.keylocker.xmlenc.EncryptedData;
import com.onedrive.sdk.authentication.MSAAuthenticator;
import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.core.DefaultClientConfig;
import com.onedrive.sdk.core.IClientConfig;
import com.onedrive.sdk.extensions.IOneDriveClient;
import com.onedrive.sdk.extensions.OneDriveClient;
import com.onedrive.sdk.logger.LoggerLevel;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base application
 */
public class KeyLockerApplication extends Application {

    private char[] password;
    private KeyDb keyDb = null;

    /**
     * The service instance
     */
    private final AtomicReference<IOneDriveClient> mClient = new AtomicReference<>();

    /**
     * The system connectivity manager
     */
    private ConnectivityManager mConnectivityManager;

    private static KeyLockerApplication sInstance = null;


    /**
     * What to do when the application starts
     */
    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    static KeyLockerApplication getInstance() {
        return sInstance;
    }

    /**
     * Create the client configuration
     * @return the newly created configuration
     */
    private IClientConfig createConfig() {

        final MSAAuthenticator msaAuthenticator = new MSAAuthenticator() {
            @Override
            public String getClientId() {
                return "0000000040177E44";
            }

            @Override
            public String[] getScopes() {
                return new String[] {"onedrive.readwrite", "onedrive.appfolder", "wl.offline_access"};
            }
        };

        final IClientConfig config = DefaultClientConfig.createWithAuthenticator(msaAuthenticator);
        config.getLogger().setLoggingLevel(LoggerLevel.Debug);
        return config;
    }

    /**
     * Navigates the user to the wifi settings if there is a connection problem
     *
     * @return if the wifi activity was navigated to
     */
    synchronized boolean goToWifiSettingsIfDisconnected() {
        final NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            Toast.makeText(this, getString(R.string.wifi_unavailable_error_message), Toast.LENGTH_LONG).show();
            final Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }
        return false;
    }

    /**
     * Clears out the auth token from the application store
     */
    void signOut() {
        if (mClient.get() == null) {
            return;
        }
        mClient.get().getAuthenticator().logout(new ICallback<Void>() {
            @Override
            public void success(final Void result) {
                mClient.set(null);
                final Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void failure(final ClientException ex) {
                Toast.makeText(getBaseContext(), "Logout error " + ex, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Get an instance of the service
     *
     * @return The Service
     */
    synchronized IOneDriveClient getOneDriveClient() {
        if (mClient.get() == null) {
            throw new UnsupportedOperationException("Unable to generate a new service object");
        }
        return mClient.get();
    }

    /**
     * Used to setup the Services
     * @param activity the current activity
     * @param serviceCreated the callback
     */
    synchronized void createOneDriveClient(final Activity activity, final ICallback<Void> serviceCreated) {
        final DefaultCallback<IOneDriveClient> callback = new DefaultCallback<IOneDriveClient>(activity) {
            @Override
            public void success(final IOneDriveClient result) {
                mClient.set(result);
                serviceCreated.success(null);
            }

            @Override
            public void failure(final ClientException error) {
                serviceCreated.failure(error);
            }
        };
        new OneDriveClient
            .Builder()
            .fromConfig(createConfig())
            .loginAndBuildClient(activity, callback);
    }

    KeyDb getKeyDb() {
        if (this.keyDb == null)
            this.keyDb = new KeyDb(this);
        else
            this.keyDb.checkPassword();

        return this.keyDb;
    }

//    // Unlock the KeyDb file
//    public void unlockKeyDb(char[] password) throws KeyDbException {
//        KeyDb newKeyDb = new KeyDb(password);
//        this.keyDb = newKeyDb;
//    }

    public Key decryptKey(byte[] data) throws KeyDbException {

        Key key = null;
        InputStream is = EncryptedData.decrypt(this.password, new ByteArrayInputStream(data));

        try {
            Serializer serializer = new Persister();
            key = serializer.read(Key.class, is);
        } catch (Exception e) {
//            throw new InvalidPasswordException();
            Log.d("DEBUG", "Error decrypting key");
        }

        return key;
    }

    public byte[] encryptKey(Key key) {
        byte[] data = null;

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Serializer serializer = new Persister();
            serializer.write(key, os);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            EncryptedData.encrypt(this.password, new ByteArrayInputStream(os.toByteArray()), output);

            data = output.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public void setPassword(char[] password) {
    }

    private char[] getPassword() throws GeneralSecurityException, IOException {
        try (ByteArrayOutputStream bs = new ByteArrayOutputStream();
             ByteArrayInputStream is = new ByteArrayInputStream(this.stored_password))
        {
            String masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            java.security.Key key = keyStore.getKey(masterKey, null);

            byte[] iv = new byte[KEY_BYTE_SIZE];
            is.read(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(key.getEncoded(), "AES"),
                new IvParameterSpec(iv));

            try (InputStream dis = new BufferedInputStream(new CipherInputStream(is, cipher)))
            {
                int available = is.available();
                byte[] data = new byte[available];
                is.read(data);

                return new String(data).toCharArray();
            }
        }
    }

    public void validatePassword() throws InvalidPasswordException {
    }

//    public void saveKey(Key key) throws KeyDbException {
//        if (keyDb == null)
//            throw new KeyDbException();
////        keyDb.storeKey(key);
//    }
//
//    public Key loadKey(String keyID) {
//        return keyDb.getKey(keyID);
//    }
}
