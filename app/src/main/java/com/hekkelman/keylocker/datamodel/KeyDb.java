package com.hekkelman.keylocker.datamodel;

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
	private static KeyDb sInstance;
	private final File file;

	// fields
	private char password[];
	private KeyChain keyChain;

	public static void setInstance(KeyDb keyDb) {
		sInstance = keyDb;
	}

	public static KeyDb getInstance() {
		return sInstance;
	}

	// constructor
	public KeyDb(char[] password, File file) throws KeyDbException
	{
		this.password = password;
		this.file = file;

		if (file.exists())
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

	public void synchronize(File file, char password[]) throws KeyDbException {
		KeyDb db = new KeyDb(password, file);
		if (synchronize(db))
			db.write();
	}

	public boolean synchronize(KeyDb db) throws KeyDbException {
		boolean result = this.keyChain.synchronize(db.keyChain);
		write();
		return result;
	}

	// accessors

	public List<Key> getKeys() {
		List<Key> result = new ArrayList<>(keyChain.getKeys());

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

	public void storeKey(String keyID, String name, String user, String password, String url) throws KeyDbException {
		Key key;

		if (keyID == null)
			key = keyChain.createKey();
		else
			key = keyChain.getKeyByID(keyID);

		key.setName(name);
		key.setUser(user);
		key.setPassword(password);
		key.setUrl(url);

		write();
	}
}
