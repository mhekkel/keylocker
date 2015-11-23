package com.hekkelman.keylocker.datamodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
	public KeyDb(char[] password, File file) throws Exception
	{
		this.password = password;
		this.file = file;

		if (file.exists())
			read();
		else
			keyChain = new KeyChain();
	}

	public void read() throws Exception
	{
		read(new FileInputStream(this.file));
	}

	public void read(InputStream input) throws Exception {
		InputStream is = EncryptedData.decrypt(this.password, input);

		Serializer serializer = new Persister();
		keyChain = serializer.read(KeyChain.class, is);
	}

	public void write() throws Exception {
		// intermediate storage of unencrypted data
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		Serializer serializer = new Persister();
		serializer.write(keyChain, os);

		EncryptedData.encrypt(this.password, new ByteArrayInputStream(os.toByteArray()), new FileOutputStream(this.file));
	}

	public void write(OutputStream output) throws Exception {
		// intermediate storage of unencrypted data
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		Serializer serializer = new Persister();
		serializer.write(keyChain, os);

		EncryptedData.encrypt(this.password, new ByteArrayInputStream(os.toByteArray()), output);
	}

	// synchronisation

	public void synchronize(File file) throws Exception {
		KeyDb db = new KeyDb(this.password, file);
		if (synchronize(db))
			db.write();
	}

	public boolean synchronize(KeyDb db) throws Exception {
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

	public Key getKey(String keyId) throws Exception {
		Key result = keyChain.getKeyByID(keyId);

		if (result == null)
			throw new Exception("Key not found");

		return result;
	}

	public void storeKey(String keyID, String name, String user, String password, String url) throws Exception {
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
