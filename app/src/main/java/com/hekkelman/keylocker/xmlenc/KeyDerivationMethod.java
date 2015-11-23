package com.hekkelman.keylocker.xmlenc;

import java.security.Key;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Validate;

@Root
public class KeyDerivationMethod {

	@Attribute(name="Algorithm")
	private static final String algorithm = "http://www.w3.org/2009/xmlenc11#pbkdf2";

	@Element(name="PBKDF2-params")
	@Namespace(prefix="x11", reference="http://www.w3.org/2009/xmlenc11#")
	private PBKDF2Params params;

	public KeyDerivationMethod()
	{
		this.params = new PBKDF2Params();
	}

	public Key getKey(char[] password, boolean newSalt) {
		return params.getKey(password, newSalt);
	}
}
