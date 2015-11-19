package com.hekkelman.keylocker.com.hekkelman.keylocker.datamodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.hekkelman.keylocker.xmlenc.EncryptedData;

public class KeyDb {



	
	// fields
	private KeyChain keyChain;
	
	// constructor
	KeyDb(char[] password, File file) throws Exception
	{
		read(password, file);
	}

	KeyDb()
	{
		keyChain = new KeyChain();
	}
			
	public void read(char[] password, File file) throws Exception
	{
		InputStream is = EncryptedData.decrypt(password, new FileInputStream(file));
		
		Serializer serializer = new Persister();
		keyChain = serializer.read(KeyChain.class, is);
	}
	
	
	public void write(char[] password, File file)
	{
		Serializer serializer = new Persister();
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		FileOutputStream of;

		try {
			serializer.write(keyChain, os);
			of = new FileOutputStream(file);
			EncryptedData.encrypt(password, new ByteArrayInputStream(os.toByteArray()), of);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<Key> getKeys()
	{
		return keyChain.getKeys();
	}
	
	
}
