package com.hekkelman.keylocker;

import java.net.URL;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest {
    public ApplicationTest() {
        ClassLoader classLoader = ApplicationTest.class.getClassLoader();
        URL resource = classLoader.getResource("org/apache/http/message/BasicLineFormatter.class");
        System.out.println(resource);
    }
}