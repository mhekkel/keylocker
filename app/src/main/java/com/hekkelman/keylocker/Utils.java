package com.hekkelman.keylocker;

public class Utils {

	public static void clear(char[] sensitiveData) {
		int length = sensitiveData.length;
		for (int i = 0; i < length; ++i)
			sensitiveData[i] = 0;
	}

}
