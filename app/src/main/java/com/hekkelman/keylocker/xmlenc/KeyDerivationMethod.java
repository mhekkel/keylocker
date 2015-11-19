package com.hekkelman.keylocker.xmlenc;

import java.security.Key;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Validate;

@Root
public class KeyDerivationMethod {
	private static final String KEY_DERIVATION_METHOD_ALGORITHM = "http://www.w3.org/2009/xmlenc11#pbkdf2";
	
	@Attribute(name="Algorithm")
	private String algorithm;
	
	@Element(name="PBKDF2-params")
	@Namespace(prefix="x11", reference="http://www.w3.org/2009/xmlenc11#")
	private PBKDF2Params params;

	public KeyDerivationMethod()
	{
		this.algorithm = KEY_DERIVATION_METHOD_ALGORITHM;
		this.params = new PBKDF2Params();
	}

	@Validate
	public void validate() throws Exception {
		if (algorithm.equals(KEY_DERIVATION_METHOD_ALGORITHM) == false)
			throw new Exception("Invalid Key Derivation Method");
	}
	
	public Key getKey(char[] password) {
		return params.getKey(password);
	}
}
