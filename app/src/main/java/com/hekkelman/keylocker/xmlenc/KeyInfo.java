package com.hekkelman.keylocker.xmlenc;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import java.security.Key;

@Root
@Namespace(prefix = "ds", reference = "http://www.w3.org/2000/09/xmldsig#")
public class KeyInfo {
    @Element(name = "DerivedKey")
    private DerivedKey derivedKey;

    public KeyInfo() {
        this.derivedKey = new DerivedKey();
    }

    public Key getKey(char[] password) {
        return derivedKey.getKey(password);
    }
}
