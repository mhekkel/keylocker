package com.hekkelman.keylocker.datamodel;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.hekkelman.keylocker.Activities.KeyDetailActivity;
import com.hekkelman.keylocker.Activities.MainActivity;
import com.hekkelman.keylocker.Utilities.Settings;
import com.hekkelman.keylocker.xmlenc.EncryptedData;

public class KeyDb {
	public static final String KEY_DB_NAME = "keylockerfile.txt";

	private static KeyDb sInstance;
	private static Settings settings;

	private final File file;
	private final boolean backup;

	// fields
	private char[] password;
	private KeyChain keyChain;

	public static void init(Settings settings) {
		if (KeyDb.settings == null) {
			KeyDb.settings = settings;
			ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
				@Override
				public void onStop(@NonNull LifecycleOwner owner) {
					if (settings.getRelockOnBackground())
						KeyDb.sInstance = null;
				}
			});
		}
	}

	public static void onReceivedScreenOff() {
		if (settings.getRelockOnScreenOff())
			KeyDb.sInstance = null;
	}

	// Public interface
	public static boolean isUnlocked() {
		return sInstance != null;
	}

	public static void initialize(char[] password, File dir) throws KeyDbException {
		File file = new File(dir, KEY_DB_NAME);
		if (file.exists())
			file.delete();

		sInstance = new KeyDb(password, file);
		sInstance.write();
	}

	public static boolean isValidPassword(char[] plainPassword, File keyDbFile) {
		boolean result = false;

		try {
			sInstance = new KeyDb(plainPassword, keyDbFile);
			sInstance.read();
			result = true;
		} catch (KeyDbException e) {
			sInstance = null;
		}

		return result;
	}

	public static void changePassword(char[] password) throws KeyDbException {
		sInstance.password = password;
		sInstance.write();
	}

	public static List<Key> getKeys() {
		List<Key> result = new ArrayList<>();

		for (Key k: sInstance.keyChain.getKeys()) {
			if (k.isDeleted())
				continue;
			result.add(k);
		}

		Collections.sort(result, new Comparator<Key>() {
			@Override
			public int compare(Key lhs, Key rhs) {
				return lhs.getName().compareToIgnoreCase(rhs.getName());
			}
		});

		return result;
	}

	public static List<Key> getFilteredKeys(String query) {
		List<Key> result;

		if (sInstance != null) {
			result = sInstance.keyChain.getKeys();
			if (! TextUtils.isEmpty(query))
				result = result.stream().filter(key -> key.match(query)).collect(Collectors.toList());
		} else
			result = new ArrayList<>();

		return result;
	}

	public static Key getKey(String keyId) {
		return sInstance.keyChain.getKeyByID(keyId);
	}

	public static void setKey(Key key) throws KeyDbException {
		sInstance.keyChain.addKey(key);
		sInstance.write();
	}

	public static void deleteKey(String keyID) throws KeyDbException {
		Key key = sInstance.keyChain.getKeyByID(keyID);

		if (key != null)
			key.setDeleted(true);

		sInstance.write();
	}

	// Private interface

	// constructor, regular key db file
	private KeyDb(char[] password, File file) throws KeyDbException
	{
		this.password = password;
		this.file = file;
		this.backup = false;

		if (file != null && file.exists())
			read();
		else
			keyChain = new KeyChain();
	}

	// constructor backup key db file
	private KeyDb(char[] password) throws KeyDbException
	{
		this.password = password;
		this.file = null;
		this.backup = true;

		keyChain = new KeyChain();
	}

	private void read() throws KeyDbException
	{
		try {
			read(new FileInputStream(this.file));
		} catch (FileNotFoundException e) {
			throw new MissingFileException();
		}
	}

	private void read(InputStream input) throws KeyDbException {
		InputStream is = EncryptedData.decrypt(this.password, input);

		try {
			Serializer serializer = new Persister();
			keyChain = serializer.read(KeyChain.class, is);
		} catch (Exception e) {
			throw new InvalidPasswordException();
		}
	}

	private void write() throws KeyDbException {
		try {
			write(new FileOutputStream(this.file));
		} catch (FileNotFoundException e) {
			throw new KeyDbRuntimeException(e);
		}
	}

	private void write(OutputStream output) throws KeyDbException {
		try {
			// intermediate storage of unencrypted data
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			Serializer serializer = new Persister();
			serializer.write(keyChain, os);

			EncryptedData.encrypt(this.password, new ByteArrayInputStream(os.toByteArray()), output, this.backup);
		} catch (Exception e) {
			throw new KeyDbRuntimeException(e);
		}
	}

	// synchronisation

	private void synchronize(File file) throws KeyDbException {
		KeyDb db = new KeyDb(this.password, file);
		if (synchronize(db))
			db.write();
	}

	private void synchronize(File file, char[] password) throws KeyDbException {
		KeyDb db = new KeyDb(password, file);
		if (synchronize(db))
			db.write();
	}

	private void synchronize(InputStream file) throws KeyDbException {
		KeyDb db = new KeyDb(this.password, null);
		db.read(file);
		synchronize(db);
	}

	private void synchronize(InputStream file, char[] password) throws KeyDbException {
		KeyDb db = new KeyDb(password, null);
		db.read(file);
		synchronize(db);
	}

	private boolean synchronize(KeyDb db) throws KeyDbException {
		boolean result = this.keyChain.synchronize(db.keyChain);
		write();
		return result;
	}

	// accessors

//	public static void storeCachedKey(String keyID, String name, String user, String password, String url) {
//		try {
//			if (sInstance == null)	// cannot continue if there's no instance at all
//				return;
//
//			File cacheFile = new File(sInstance.file.getParent(), KEY_DB_TEMP_NAME);
//			if (cacheFile.exists())
//				cacheFile.delete();
//
//			KeyDb tempDb = new KeyDb(sInstance.password, cacheFile);
//			tempDb.storeKey(keyID, name, user, password, url);
//		} catch (KeyDbException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void removeCachedKey(String keyID) {
//		if (sInstance == null)	// cannot continue if there's no instance at all
//			return;
//
//		File cacheFile = new File(sInstance.file.getParent(), KEY_DB_TEMP_NAME);
//		if (cacheFile.exists()) {
//			KeyDb tempDb = new KeyDb(sInstance.password, cacheFile);
//			Key key = tempDb.getKey(keyID);
//
//			if (key != null)
//				Log.d("debug", "Key is not stored in temp file");
//
//			cacheFile.delete();
//		}
//	}
//
//	public static Key getCachedKey() {
//		Key key = null;
//
//		if (sInstance != null) {    // cannot continue if there's no instance at all
//			File cacheFile = new File(sInstance.file.getParent(), KEY_DB_TEMP_NAME);
//			if (cacheFile.exists()) {
//				KeyDb tempDb = new KeyDb(sInstance.password, cacheFile);
//
//				List<Key> keys = tempDb.getKeys();
//				if (keys.isEmpty() == false)
//					key = keys.get(0);
//			}
//		}
//
//		return key;
//	}
//
//	public static void release() {
//		if (--sRefCount == 0)
//			sInstance = null;
//	}
//
//	public static void reference() {
//		assert(sInstance != null);
//		++sRefCount;
//	}
//
//	public KeyDb undeleteAll() throws KeyDbException {
//		for (Key k : keyChain.getKeys()) {
//			if (k.isDeleted()) {
//				k.setDeleted(false);
//			}
//		}
//
//		write();
//
//		return this;
//	}
}
