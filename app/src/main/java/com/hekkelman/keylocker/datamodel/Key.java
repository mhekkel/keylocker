package com.hekkelman.keylocker.datamodel;

import android.text.TextUtils;

import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.PersistenceException;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.core.Validate;

@Root
public class Key {
	private static final SimpleDateFormat FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Attribute(name="id")
	private String id;

	@Attribute(name="timestamp")
	private String timestamp;
	private Date _timestamp;		// converted
	
	@Attribute(name="deleted", required=false)
	private String deleted;
	
	@Element(name="name", required=false)
	private String name;
	
	@Element(name="user", required=false)
	private String user;
	
	@Element(name="pass", required=false)
	private String password;
	
	@Element(name="url", required=false)
	private String url;

	// constructors
	public Key(Key key) {
		this.id = key.id;
		this.timestamp = key.timestamp;
		this._timestamp = key._timestamp;
		this.deleted = key.deleted;
		this.name = key.name;
		this.user = key.user;
		this.password = key.password;
		this.url = key.url;
	}

	public Key()
	{
		FMT.setTimeZone(TimeZone.getTimeZone("UTC"));
		this._timestamp = new Date();
		this.timestamp = FMT.format(_timestamp);

		this.id = UUID.randomUUID().toString();
		this.deleted = "false";
	}

	public Key(String keyID, String name, String user, String password, String url) {
		FMT.setTimeZone(TimeZone.getTimeZone("UTC"));
		this._timestamp = new Date();
		this.timestamp = FMT.format(_timestamp);

		this.id = keyID;
		this.deleted = "false";
		this.name = name;
		this.user = user;
		this.password = password;
		this.url = url;
	}

	@Validate
	void validate() throws PersistenceException {
		try {
			_timestamp = FMT.parse(timestamp);
		} catch (ParseException e) {
			throw new PersistenceException("Invalid timestamp in key");
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deleted == null) ? 0 : deleted.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Key))
			return false;
		Key other = (Key) obj;
		if (deleted == null) {
			if (other.deleted != null)
				return false;
		} else if (!deleted.equals(other.deleted))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	private void updateTimeStamp() {
		this._timestamp = new Date();
		this.timestamp = FMT.format(_timestamp);
	}

	public boolean isDeleted() {
		return deleted.equals("true");
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted ? "true" : "false";
		updateTimeStamp();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		updateTimeStamp();
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
		updateTimeStamp();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
		updateTimeStamp();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		updateTimeStamp();
	}

	public String getId() {
		return id;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public int synchronize(Key key) {
		int result = _timestamp.compareTo(key._timestamp);

		if (!this.id.equals(key.id)) throw new AssertionError();

		if (result < 0)
		{
			this.timestamp = key.timestamp;
			this._timestamp = key._timestamp;
			this.deleted = key.deleted;
			this.name = key.name;
			this.user = key.user;
			this.password = key.password;
			this.url = key.url;
		}
		else if (result > 0)
		{
			key.timestamp = this.timestamp;
			key._timestamp = this._timestamp;
			key.deleted = this.deleted;
			key.name = this.name;
			key.user = this.user;
			key.password = this.password;
			key.url = this.url;
		}

		return result;
	}

	public boolean match(String query) {
		query = query.toLowerCase();

		return (TextUtils.isEmpty(name) == false && name.toLowerCase().contains(query)) ||
				(TextUtils.isEmpty(user) == false && user.toLowerCase().contains(query)) ||
				(TextUtils.isEmpty(url) == false && url.toLowerCase().contains(query));
	}
}
