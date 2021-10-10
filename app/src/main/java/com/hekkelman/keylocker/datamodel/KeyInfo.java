package com.hekkelman.keylocker.datamodel;

import android.text.TextUtils;

import java.util.Locale;

public class KeyInfo {
	private final String user;
	private final String name;
	private final String url;
	private final String id;

	public KeyInfo(String user, String name, String url, String id) {
		this.user = user;
		this.name = name;
		this.url = url;
		this.id = id;
	}

	public String getUser() {
		return user;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public boolean match(String query) {
		query = query.toLowerCase();

		return (TextUtils.isEmpty(name) == false && name.toLowerCase(Locale.getDefault()).contains(query)) ||
			(TextUtils.isEmpty(user) == false && user.toLowerCase(Locale.getDefault()).contains(query)) ||
			(TextUtils.isEmpty(url) == false && url.toLowerCase(Locale.getDefault()).contains(query));
	}
}
