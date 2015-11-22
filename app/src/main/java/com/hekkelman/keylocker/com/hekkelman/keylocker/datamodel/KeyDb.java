package com.hekkelman.keylocker.com.hekkelman.keylocker.datamodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
		write(new FileOutputStream(this.file));
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
		synchronize(db);
	}

	public void synchronize(KeyDb db) throws Exception {
		this.keyChain.synchronize(db.keyChain);
		write();
	}

	// accessors

	public List<Key> getKeys()
	{
		return keyChain.getKeys();
	}

	public Key getKey(String keyId) throws Exception {
		Key result = keyChain.getKeyByID(keyId);

		if (result == null)
			throw new Exception("Key not found");

		return result;
	}
}
