package com.hekkelman.keylocker.xmlenc;

import android.util.Base64;

import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Complete;
import org.simpleframework.xml.core.Persist;
import org.simpleframework.xml.core.Validate;

@Root
public class PBKDF2Params {
	private static final int HASH_BYTE_SIZE = 16;
	private static final int KEY_BYTE_SIZE = 16;
	private static final int ITERATION_COUNT = 1000;
	
	@Path("x11:Salt")
	@Namespace(prefix="x11", reference="http://www.w3.org/2009/xmlenc11#")
	@Element(name="Specified")
	private String salt;
	private byte[] _salt;

	@Element(name="IterationCount")
	@Namespace(prefix="x11", reference="http://www.w3.org/2009/xmlenc11#")
	private int iterationCount;

	@Element(name="KeyLength")
	@Namespace(prefix="x11", reference="http://www.w3.org/2009/xmlenc11#")
	private int keyLength;
	
	@Path("x11:PRF")
	@Attribute(name="Algorithm")
	private static final String algorithm = "http://www.w3.org/2001/04/xmldsig-more#hmac-sha1";

	// constructor for a new params block
	public PBKDF2Params()
	{
		SecureRandom random = new SecureRandom();

		this._salt = new byte[HASH_BYTE_SIZE];
		random.nextBytes(this._salt);
		
		this.iterationCount = ITERATION_COUNT;
		this.keyLength = KEY_BYTE_SIZE;
	}

	@Validate
	public void validate() throws Exception {
		this._salt = Base64.decode(this.salt, Base64.DEFAULT);

		if (_salt.length != HASH_BYTE_SIZE ||
				iterationCount < ITERATION_COUNT ||
				keyLength != KEY_BYTE_SIZE)
			throw new Exception("Invalid PBKDF2 Parameters");
	}
	
	public Key getKey(char[] password) {
		try {
			KeySpec ks = new PBEKeySpec(password, this._salt, iterationCount, 16 * 8);
			SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			SecretKey s = f.generateSecret(ks);
			return s;
		}
		catch (Exception e) {
			System.out.print(e);
			return null;
		}
	}

	@Persist
	public void prepare() {
		this.salt = new String(Base64.encode(this._salt, Base64.DEFAULT));
	}

	@Complete
	public void release() {
		this._salt = Base64.decode(this.salt, Base64.DEFAULT);
	}
}
