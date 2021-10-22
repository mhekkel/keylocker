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
public class KeyNote {
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((deleted == null) ? 0 : deleted.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
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

    public void setName(String name, KeyDb.SafetyToken safetyToken) {
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
}
