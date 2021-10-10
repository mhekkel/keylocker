package com.hekkelman.keylocker.datamodel;

public interface LoadKeyDbCallback {
	void onLoaded();
	void onFailed(KeyDbException ex);
}
