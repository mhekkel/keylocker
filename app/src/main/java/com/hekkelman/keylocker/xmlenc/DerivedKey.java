package com.hekkelman.keylocker.xmlenc;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import java.security.Key;

@Root
@Namespace(prefix = "x11", reference = "http://www.w3.org/2009/xmlenc11#")
public class DerivedKey {
    @Element(name = "KeyDerivationMethod")
    @Namespace(prefix = "x11", reference = "http://www.w3.org/2009/xmlenc11#")
    private KeyDerivationMethod method;

    public DerivedKey() {
        this.method = new KeyDerivationMethod();
    }

    public Key getKey(char[] password, boolean isBackup) {
        return method.getKey(password, isBackup);
    }
}
