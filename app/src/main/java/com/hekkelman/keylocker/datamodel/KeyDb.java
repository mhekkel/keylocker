package com.hekkelman.keylocker.datamodel;

import android.app.Activity;
import android.content.Context;

import java.io.*;
import java.security.*;
import java.util.*;

import android.content.Intent;
import androidx.security.crypto.MasterKeys;
import com.hekkelman.keylocker.UnlockActivity;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class KeyDb {

	public static final String KEY_DB_NAME = "keylockerfile.txt";
	private static final String KEY_PASSWORD_NAME = "keylocker-password";

//	private static KeyDb sInstance;
//	private final File file;

	// fields
	private KeyChain keyChain;

	// we store the password encrypted with a max duration of validity
	private static final int KEY_BYTE_SIZE = 16;
	private byte[] stored_password;
	private Timer password_timer;

//	private class Password
//	{
//
//	}


//	public static boolean keyDbExists() {
//		return new File(KEY_DB_NAME).exists();
//	}
	// constructors

	// First constructor used to create a new global instance
	public KeyDb(Context context)
	{
		this.password_timer = new Timer();
	}

	public KeyDb(char[] password, File file) throws KeyDbException
	{
		this.password_timer = new Timer();

		setPassword(password);

//		this.file = file;
		this.keyChain = new KeyChain();
	}

	private void setPassword(char[] password) {
		try (ByteArrayOutputStream bs = new ByteArrayOutputStream())
		{
			String masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
			KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
			keyStore.load(null);
			java.security.Key key = keyStore.getKey(masterKey, null);

			byte[] iv = new byte[KEY_BYTE_SIZE];

			SecureRandom random = new SecureRandom();
			random.nextBytes(iv);

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE,
				new SecretKeySpec(key.getEncoded(), "AES"),
				new IvParameterSpec(iv));

			try (OutputStream cs = new CipherOutputStream(bs, cipher))
			{
				cs.write(iv);
// TODO moet anders, maar hoe?
				cs.write(password.toString().getBytes());
			}

			this.stored_password = bs.toByteArray();

			this.password_timer.schedule(new TimerTask() {
				@Override
				public void run() {
					clearPassword();
				}
			}, 60);

		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void clearPassword() {
		// TODO overwrite with null
		this.stored_password = null;
	}

	public void checkPassword(Activity activity) {
		if (this.stored_password == null)
		{
			activity.startActivity(new Intent(activity, UnlockActivity.class));
				activity.finish();
		}
	}

	private char[] getPassword() throws IOException, GeneralSecurityException {
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

	public static void create(char[] password) throws KeyDbException {
		File file = new File(KEY_DB_NAME);
		if (file.exists() && ! file.delete())
			throw new KeyDbException();

		new KeyDb(password, file);
	}

//	public static void load(Context context, LoadKeyDbCallback cb) {
//		try
//		{
//			sInstance = new KeyDb(context);
//			cb.onLoaded();
//		}
//		catch (KeyDbException ex)
//		{
//			cb.onFailed(ex);
//		}
//	}
//
//	private static char[] decryptPassword(Context context) {
//	}

//	public static KeyDb getInstance() {
//		return sInstance;
//	}

//	public void changePassword(char[] password) throws KeyDbException {
//		this.password = password;
//		write();
//	}

//	public void read(char[] password) throws KeyDbException
//	{
//		try {
//			read(password, new FileInputStream(this.file));
//		} catch (FileNotFoundException e) {
//			throw new MissingFileException();
//		}
//	}
//
//	public void write() throws KeyDbException {
//		try {
//			write(new FileOutputStream(this.file));
//		} catch (Exception e) {
//			throw new KeyDbRuntimeException(e);
//		}
//	}
//
//	public void read(char[] password, InputStream input) throws KeyDbException {
//		InputStream is = EncryptedData.decrypt(password, input);
//
//		try {
//			Serializer serializer = new Persister();
//			keyChain = serializer.read(KeyChain.class, is);
//		} catch (Exception e) {
//			throw new InvalidPasswordException();
//		}
//	}
//
//	public void write(OutputStream output) throws KeyDbException {
//		try {
//			char[] password = getPassword();
//			keyChain.encrypt(output, password);
//		} catch (Exception e) {
//			throw new KeyDbRuntimeException(e);
//		}
//	}
//
//	// synchronisation
//
//	public void synchronize(File file) throws KeyDbException {
//		KeyDb db = new KeyDb(this.password, file);
//		if (synchronize(db))
//			db.write();
//	}
//
//	public void synchronize(File file, char[] password) throws KeyDbException {
//		KeyDb db = new KeyDb(password, file);
//		if (synchronize(db))
//			db.write();
//	}
//
//	public void synchronize(InputStream file) throws KeyDbException {
//		KeyDb db = new KeyDb(this.password, null);
//		db.read(file);
//		synchronize(db);
//	}
//
//	public void synchronize(InputStream file, char[] password) throws KeyDbException {
//		KeyDb db = new KeyDb(password, null);
//		db.read(file);
//		synchronize(db);
//	}
//
//	public boolean synchronize(KeyDb db) throws KeyDbException {
//		boolean result = this.keyChain.synchronize(db.keyChain);
//		write();
//		return result;
//	}

	// accessors

	public List<KeyInfo> getKeys() {
		List<KeyInfo> result = new ArrayList<>();

		for (Key k: keyChain.getKeys()) {
			if (k.isDeleted())
				continue;
			result.add(new KeyInfo(k.getUser(), k.getName(), k.getUrl(), k.getId()));
		}

		Collections.sort(result, new Comparator<KeyInfo>() {
			@Override
			public int compare(KeyInfo lhs, KeyInfo rhs) {
				return lhs.getName().compareToIgnoreCase(rhs.getName());
			}
		});

		return result;
	}

	public Key getKey(String keyId) {
		return keyChain.getKeyByID(keyId);
	}

//	public Key storeKey(String keyID, String name, String user, String password, String url) throws KeyDbException {
//		Key key = null;
//
//		if (keyID != null)
//			key = keyChain.getKeyByID(keyID);
//
//		if (key == null)
//			key = keyChain.createKey();
//
//		key.setName(name);
//		key.setUser(user);
//		key.setPassword(password);
//		key.setUrl(url);
//
//		write();
//
//		return key;
//	}

	public void deleteKey(String keyID) throws KeyDbException {
		Key key = keyChain.getKeyByID(keyID);

		if (key != null)
			key.setDeleted(true);

//		write();
	}

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
//		try {
//			if (sInstance == null)	// cannot continue if there's no instance at all
//				return;
//
//			File cacheFile = new File(sInstance.file.getParent(), KEY_DB_TEMP_NAME);
//			if (cacheFile.exists()) {
//				KeyDb tempDb = new KeyDb(sInstance.password, cacheFile);
//				Key key = tempDb.getKey(keyID);
//
//				if (key != null)
//					Log.d("debug", "Key is not stored in temp file");
//
//				cacheFile.delete();
//			}
//		} catch (KeyDbException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static Key getCachedKey() {
//		Key key = null;
//
//		if (sInstance != null) {    // cannot continue if there's no instance at all
//			try {
//				File cacheFile = new File(sInstance.file.getParent(), KEY_DB_TEMP_NAME);
//				if (cacheFile.exists()) {
//					KeyDb tempDb = new KeyDb(sInstance.password, cacheFile);
//
//					List<Key> keys = tempDb.getKeys();
//					if (keys.isEmpty() == false)
//						key = keys.get(0);
//				}
//			} catch (KeyDbException e) {
//				e.printStackTrace();
//			}
//		}
//
//		return key;
//	}

//	public static void release() {
//		if (--sRefCount == 0)
//			sInstance = null;
//	}
//
// 	public static void reference() {
//		assert(sInstance != null);
//		++sRefCount;
//	}

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
