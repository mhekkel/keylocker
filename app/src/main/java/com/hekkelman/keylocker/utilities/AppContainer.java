package com.hekkelman.keylocker.utilities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.hekkelman.keylocker.datamodel.KeyLockerFile;

import androidx.core.os.HandlerCompat;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppContainer {
    public ExecutorService executorService = Executors.newSingleThreadExecutor();
    public Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());

    public KeyLockerFile keyDb;
    public MutableLiveData<Boolean> locked = new MutableLiveData<>(true);

    public AppContainer(Context context) {
        KeyLockerFile.mFilesDir = context.getFilesDir();
    }
}
