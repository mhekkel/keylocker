package com.hekkelman.keylocker.xmlenc;

import android.util.Base64;

import com.hekkelman.keylocker.datamodel.KeyDbException;
import com.hekkelman.keylocker.datamodel.KeyDbRuntimeException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

@Root(name="EncryptedData")
@Namespace(reference="http://www.w3.org/2001/04/xmlenc#", prefix="x")
public class EncryptedData
{
	private static final int KEY_BYTE_SIZE = 16;
	
	@Attribute(name="Type")
	private static final String type = "http://www.w3.org/2001/04/xmlenc#Element";

	@Path("x:EncryptionMethod")
	@Attribute(name="Algorithm")
	private static final String algorithm = "http://www.w3.org/2009/xmlenc11#aes256-cbc";

	@Element(name="KeyInfo")
	private KeyInfo keyInfo;
	
	@Path("x:CipherData")
	@Element(name="CipherValue")
	@Namespace(reference="http://www.w3.org/2001/04/xmlenc#", prefix="x")
	private String value;
	
	// constructor
	private EncryptedData()
	{
		this.keyInfo = new KeyInfo();
		this.value = null;
	}

	static public InputStream decrypt(char[] password, InputStream is) throws KeyDbException {
		byte[] data = new byte[0];
		Cipher cipher = null;

		try {
			Serializer serializer = new Persister();
			EncryptedData encData = serializer.read(EncryptedData.class, is);

			Key key = encData.keyInfo.getKey(password, false);

			data = Base64.decode(encData.value, Base64.DEFAULT);
			byte[] iv = new byte[KEY_BYTE_SIZE];
			System.arraycopy(data, 0, iv, 0, KEY_BYTE_SIZE);

			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

			cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(key.getEncoded(), "AES"),
                    new IvParameterSpec(iv));
		} catch (Exception e) {
			throw new KeyDbRuntimeException(e);
		}

		return new BufferedInputStream(new CipherInputStream(new ByteArrayInputStream(data, KEY_BYTE_SIZE, data.length - KEY_BYTE_SIZE), cipher));
	}
	
	static public void encrypt(char[] password, InputStream data, OutputStream os, boolean isBackup) throws KeyDbException {
		try {
			EncryptedData encData = new EncryptedData();

			byte[] iv = new byte[KEY_BYTE_SIZE];

			SecureRandom random = new SecureRandom();
			random.nextBytes(iv);

			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			bs.write(iv);

			Key key = encData.keyInfo.getKey(password, isBackup);

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(key.getEncoded(), "AES"),
                    new IvParameterSpec(iv));

			OutputStream cs = new CipherOutputStream(bs, cipher);

			for (;;)
            {
                byte[] b = new byte[16];

                int l = data.read(b);
                if (l > 0)
                    cs.write(b, 0, l);

                if (l < 16)
                    break;
            }
			cs.close();

			encData.value = Base64.encodeToString(bs.toByteArray(), Base64.DEFAULT);

			Serializer serializer = new Persister();
			serializer.write(encData, os);
		} catch (Exception e) {
			throw new KeyDbRuntimeException(e);
		}
	}
}