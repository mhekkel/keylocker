package com.hekkelman.keylocker.datamodel;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.PersistenceException;
import org.simpleframework.xml.core.Validate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Root
public abstract class KeyNote {
    private static final AtomicLong currentListID = new AtomicLong();
    private final long listID = currentListID.incrementAndGet();

    @SuppressLint("SimpleDateFormat")
    protected static final SimpleDateFormat FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Attribute(name = "id")
    protected String id;
    @Attribute(name = "timestamp")
    protected String timestamp;
    protected Date _timestamp;        // converted
    @Attribute(name = "deleted", required = false)
    protected String deleted;
    @Element(name = "name", required = false)
    protected String name;

    // constructors
    public KeyNote(KeyNote keyNote) {
        this.id = keyNote.id;
        this.timestamp = keyNote.timestamp;
        this._timestamp = keyNote._timestamp;
        this.deleted = keyNote.deleted;
        this.name = keyNote.name;
    }

    public KeyNote() {
        FMT.setTimeZone(TimeZone.getTimeZone("UTC"));
        this._timestamp = new Date();
        this.timestamp = FMT.format(_timestamp);

        this.id = UUID.randomUUID().toString();
        this.deleted = "false";
    }

    public KeyNote(String keyID, String name) {
        FMT.setTimeZone(TimeZone.getTimeZone("UTC"));
        this._timestamp = new Date();
        this.timestamp = FMT.format(_timestamp);

        this.id = keyID;
        this.deleted = "false";
        this.name = name;
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
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Key))
            return false;
        Key other = (Key) obj;
        if (id == null) {
            return other.id == null;
        } else return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
//        result = prime * result + ((deleted == null) ? 0 : deleted.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
//        result = prime * result + ((name == null) ? 0 : name.hashCode());
//        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    protected void updateTimeStamp() {
        this._timestamp = new Date();
        this.timestamp = FMT.format(_timestamp);
    }

    public boolean isDeleted() {
        return deleted.equals("true");
    }

    protected void setDeleted(boolean deleted) {
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

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public long getListID() {
        return listID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int synchronize(KeyNote key) {
        int result = _timestamp.compareTo(key._timestamp);

        if (!this.id.equals(key.id)) throw new AssertionError();

        if (result < 0) {
            this.timestamp = key.timestamp;
            this._timestamp = key._timestamp;
            this.deleted = key.deleted;
            this.name = key.name;
        } else if (result > 0) {
            key.timestamp = this.timestamp;
            key._timestamp = this._timestamp;
            key.deleted = this.deleted;
            key.name = this.name;
        }

        return result;
    }

    public boolean match(String query) {
        query = query.toLowerCase(Locale.getDefault());
        return (!TextUtils.isEmpty(name) && name.toLowerCase(Locale.getDefault()).contains(query));
    }

    public abstract String getDescription();

    public abstract String getText();

    public void purgeData()
    {
        assert(isDeleted());
        this.name= "";
    }

    @Root
    public final static class Key extends KeyNote {
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

//        @Override
//        public int hashCode() {
//            final int prime = 31;
//            int result = super.hashCode();
//            result = prime * result + ((password == null) ? 0 : password.hashCode());
//            result = prime * result + ((url == null) ? 0 : url.hashCode());
//            result = prime * result + ((user == null) ? 0 : user.hashCode());
//            return result;
//        }

//        @Override
//        public boolean equals(Object obj) {
//            if (this == obj)
//                return true;
//            if (obj == null)
//                return false;
//            if (!(obj instanceof Key))
//                return false;
//            Key other = (Key) obj;
//            if (deleted == null) {
//                if (other.deleted != null)
//                    return false;
//            } else if (!deleted.equals(other.deleted))
//                return false;
//            if (id == null) {
//                if (other.id != null)
//                    return false;
//            } else if (!id.equals(other.id))
//                return false;
//            if (name == null) {
//                if (other.name != null)
//                    return false;
//            } else if (!name.equals(other.name))
//                return false;
//            if (password == null) {
//                if (other.password != null)
//                    return false;
//            } else if (!password.equals(other.password))
//                return false;
//            if (timestamp == null) {
//                if (other.timestamp != null)
//                    return false;
//            } else if (!timestamp.equals(other.timestamp))
//                return false;
//            if (url == null) {
//                if (other.url != null)
//                    return false;
//            } else if (!url.equals(other.url))
//                return false;
//            if (user == null) {
//                return other.user == null;
//            } else return user.equals(other.user);
//        }

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

        public void setId(String id) {
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

        public void purgeData() {
            super.purgeData();
            this.user= "";
            this.password= "";
            this.url= "";
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

    @Root
    public final static class Note extends KeyNote {
        @Element(name = "text", required = false)
        private String text;

        // constructor
        public Note() {
            super();
        }

        public Note(Note note) {
            super(note);
            this.text = note.text;
        }

//        @Override
//        public int hashCode() {
//            final int prime = 31;
//            int result = super.hashCode();
//            result = prime * result + ((text == null) ? 0 : text.hashCode());
//            return result;
//        }

//        @Override
//        public boolean equals(Object obj) {
//            if (this == obj)
//                return true;
//            if (obj == null)
//                return false;
//            if (!(obj instanceof Note))
//                return false;
//            Note other = (Note) obj;
//            if (deleted == null) {
//                if (other.deleted != null)
//                    return false;
//            } else if (!deleted.equals(other.deleted))
//                return false;
//            if (id == null) {
//                if (other.id != null)
//                    return false;
//            } else if (!id.equals(other.id))
//                return false;
//            if (name == null) {
//                if (other.name != null)
//                    return false;
//            } else if (!name.equals(other.name))
//                return false;
//            if (text == null) {
//                if (other.text != null)
//                    return false;
//            } else if (!text.equals(other.text))
//                return false;
//            if (timestamp == null) {
//                return other.timestamp == null;
//            } else return timestamp.equals(other.timestamp);
//        }

        public int synchronize(Note note) {
            int result = super.synchronize(note);

            if (result < 0) this.text = note.text;
            else if (result > 0) note.text = this.text;

            return result;
        }

        public void purgeData() {
            super.purgeData();
            this.text= "";
        }

        public String getText() {
            return text;
        }

        protected void setText(String text) {
            this.text = text;
        }

        public boolean match(String query) {
            query = query.toLowerCase(Locale.getDefault());
            return (!TextUtils.isEmpty(name) && name.toLowerCase(Locale.getDefault()).contains(query)) ||
                    (!TextUtils.isEmpty(text) && text.toLowerCase(Locale.getDefault()).contains(query));
        }

        @Override
        public String getDescription() {
            return this.text;
        }
    }
}
