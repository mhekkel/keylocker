package com.hekkelman.keylocker.datamodel;

import android.text.TextUtils;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Locale;

@Root
public class Key extends KeyNote {
    @Element(name = "user", required = false)
    private String user;
    @Element(name = "pass", required = false)
    private String password;
    @Element(name = "url", required = false)
    private String url;

    // constructors
    protected Key(Key key) {
        super(key);
        this.user = key.user;
        this.password = key.password;
        this.url = key.url;
    }

    protected Key() {
        super();
    }

    protected Key(String keyID, String name, String user, String password, String url) {
        super(keyID, name);

        this.user = user;
        this.password = password;
        this.url = url;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((password == null) ? 0 : password.hashCode());
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

    public String getUser() {
        return user;
    }

    protected void setUser(String user) {
        this.user = user;
        updateTimeStamp();
    }

    public String getPassword() {
        return password;
    }

    protected void setPassword(String password) {
        this.password = password;
        updateTimeStamp();
    }

    public String getUrl() {
        return url;
    }

    protected void setUrl(String url) {
        this.url = url;
        updateTimeStamp();
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int synchronize(Key key) {
        int result = _timestamp.compareTo(key._timestamp);

        if (!this.id.equals(key.id)) throw new AssertionError();

        if (result < 0) {
            this.timestamp = key.timestamp;
            this._timestamp = key._timestamp;
            this.deleted = key.deleted;
            this.name = key.name;
            this.user = key.user;
            this.password = key.password;
            this.url = key.url;
        } else if (result > 0) {
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

        return (!TextUtils.isEmpty(name) && name.toLowerCase(Locale.getDefault()).contains(query)) ||
                (!TextUtils.isEmpty(user) && user.toLowerCase(Locale.getDefault()).contains(query)) ||
                (!TextUtils.isEmpty(url) && url.toLowerCase(Locale.getDefault()).contains(query));
    }

    @Override
    public String getDescription() {
        return this.user;
    }

    @Override
    public String getText() {
        return this.password;
    }
}
