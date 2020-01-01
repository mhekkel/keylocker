package com.hekkelman.keylocker.datamodel;

import android.util.Log;

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

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.hekkelman.keylocker.xmlenc.EncryptedData;

public class KeyDb {
	public static final String KEY_DB_NAME = "keylockerfile.txt";
	public static final String KEY_DB_TEMP_NAME = "keylockerfile-temp.txt";
	private static KeyDb sInstance;
	private static int sRefCount = 0;
	private final File file;

	// fields
	private char[] password;
	private KeyChain keyChain;

	public static void setInstance(KeyDb keyDb) {
		sInstance = keyDb;
		sRefCount = 1;
	}

	public static KeyDb getInstance() {
		return sInstance;
	}

	// constructor
	public KeyDb(char[] password, File file) throws KeyDbException
	{
		this.password = password;
		this.file = file;

		if (file != null && file.exists())
			read();
		else
			keyChain = new KeyChain();
	}

	public void changePassword(char[] password) throws KeyDbException {
		this.password = password;
		write();
	}

	public void read() throws KeyDbException
	{
		try {
			read(new FileInputStream(this.file));
		} catch (FileNotFoundException e) {
			throw new MissingFileException();
		}
	}

	public void read(InputStream input) throws KeyDbException {
		InputStream is = EncryptedData.decrypt(this.password, input);

		try {
			Serializer serializer = new Persister();
			keyChain = serializer.read(KeyChain.class, is);
		} catch (Exception e) {
			throw new InvalidPasswordException();
		}
	}

	public void write() throws KeyDbException {
		try {
			// intermediate storage of unencrypted data
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			Serializer serializer = new Persister();
			serializer.write(keyChain, os);

			EncryptedData.encrypt(this.password, new ByteArrayInputStream(os.toByteArray()), new FileOutputStream(this.file));
		} catch (Exception e) {
			throw new KeyDbRuntimeException(e);
		}
	}

	public void write(OutputStream output) throws KeyDbException {
		try {
			// intermediate storage of unencrypted data
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			Serializer serializer = new Persister();
			serializer.write(keyChain, os);

			EncryptedData.encrypt(this.password, new ByteArrayInputStream(os.toByteArray()), output);
		} catch (Exception e) {
			throw new KeyDbRuntimeException(e);
		}
	}

	// synchronisation

	public void synchronize(File file) throws KeyDbException {
		KeyDb db = new KeyDb(this.password, file);
		if (synchronize(db))
			db.write();
	}

	public void synchronize(File file, char[] password) throws KeyDbException {
		KeyDb db = new KeyDb(password, file);
		if (synchronize(db))
			db.write();
	}

	public void synchronize(InputStream file) throws KeyDbException {
		KeyDb db = new KeyDb(this.password, null);
		db.read(file);
		synchronize(db);
	}

	public void synchronize(InputStream file, char[] password) throws KeyDbException {
		KeyDb db = new KeyDb(password, null);
		db.read(file);
		synchronize(db);
	}

	public boolean synchronize(KeyDb db) throws KeyDbException {
		boolean result = this.keyChain.synchronize(db.keyChain);
		write();
		return result;
	}

	// accessors

	public List<Key> getKeys() {
		List<Key> result = new ArrayList<>();

		for (Key k: keyChain.getKeys()) {
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

	public Key getKey(String keyId) {
		return keyChain.getKeyByID(keyId);
	}

	public Key storeKey(String keyID, String name, String user, String password, String url) throws KeyDbException {
		Key key = null;

		if (keyID != null)
			key = keyChain.getKeyByID(keyID);

		if (key == null)
			key = keyChain.createKey();

		key.setName(name);
		key.setUser(user);
		key.setPassword(password);
		key.setUrl(url);

		write();

		return key;
	}

	public void deleteKey(String keyID) throws KeyDbException {
		Key key = keyChain.getKeyByID(keyID);

		if (key != null)
			key.setDeleted(true);

		write();
	}

	public static void storeCachedKey(String keyID, String name, String user, String password, String url) {
		try {
			if (sInstance == null)	// cannot continue if there's no instance at all
				return;

			File cacheFile = new File(sInstance.file.getParent(), KEY_DB_TEMP_NAME);
			if (cacheFile.exists())
				cacheFile.delete();

			KeyDb tempDb = new KeyDb(sInstance.password, cacheFile);
			tempDb.storeKey(keyID, name, user, password, url);
		} catch (KeyDbException e) {
			e.printStackTrace();
		}
	}

	public static void removeCachedKey(String keyID) {
		try {
			if (sInstance == null)	// cannot continue if there's no instance at all
				return;

			File cacheFile = new File(sInstance.file.getParent(), KEY_DB_TEMP_NAME);
			if (cacheFile.exists()) {
				KeyDb tempDb = new KeyDb(sInstance.password, cacheFile);
				Key key = tempDb.getKey(keyID);

				if (key != null)
					Log.d("debug", "Key is not stored in temp file");

				cacheFile.delete();
			}
		} catch (KeyDbException e) {
			e.printStackTrace();
		}
	}

	public static Key getCachedKey() {
		Key key = null;

		if (sInstance != null) {    // cannot continue if there's no instance at all
			try {
				File cacheFile = new File(sInstance.file.getParent(), KEY_DB_TEMP_NAME);
				if (cacheFile.exists()) {
					KeyDb tempDb = new KeyDb(sInstance.password, cacheFile);

					List<Key> keys = tempDb.getKeys();
					if (keys.isEmpty() == false)
						key = keys.get(0);
				}
			} catch (KeyDbException e) {
				e.printStackTrace();
			}
		}

		return key;
	}

	public static void release() {
		if (--sRefCount == 0)
			sInstance = null;
	}

	public static void reference() {
		assert(sInstance != null);
		++sRefCount;
	}

	public KeyDb undeleteAll() throws KeyDbException {
		for (Key k : keyChain.getKeys()) {
			if (k.isDeleted()) {
				k.setDeleted(false);
			}
		}

		write();

		return this;
	}
}
