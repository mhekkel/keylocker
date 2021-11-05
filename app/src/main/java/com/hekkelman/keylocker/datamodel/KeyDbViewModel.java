package com.hekkelman.keylocker.datamodel;

import android.app.Application;

import com.hekkelman.keylocker.KeyLockerApp;
import com.hekkelman.keylocker.utilities.AppContainer;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class KeyDbViewModel extends AndroidViewModel {
    public AppContainer appContainer;
    public MutableLiveData<Boolean> locked;

    public KeyDbViewModel(Application application) {
        super(application);

        appContainer = ((KeyLockerApp)getApplication()).mAppContainer;
        this.locked = appContainer.locked;
    }
}
