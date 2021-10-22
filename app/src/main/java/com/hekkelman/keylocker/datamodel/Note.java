package com.hekkelman.keylocker.datamodel;

import android.annotation.SuppressLint;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

@Root
public class Note {
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Attribute(name = "id")
    private String id;

    @Attribute(name = "timestamp")
    private String timestamp;
    private Date _timestamp;

    @Attribute(name = "deleted")
    private String deleted;

    @Element(name = "name", required = false)
    private String name;

    @Element(name = "text", required = false)
    private String text;

    // constructor
    public Note() {
        FMT.setTimeZone(TimeZone.getTimeZone("UTC"));
        this._timestamp = new Date();
        this.timestamp = FMT.format(_timestamp);

        this.id = UUID.randomUUID().toString();
        this.deleted = "false";
    }

    public Note(Note note) {
        this.id = note.id;
        this.timestamp = note.timestamp;
        this._timestamp = note._timestamp;
        this.deleted = note.deleted;
        this.name = note.name;
        this.text = note.text;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((deleted == null) ? 0 : deleted.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Note))
            return false;
        Note other = (Note) obj;
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
        if (text == null) {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;
        if (timestamp == null) {
            if (other.timestamp != null)
                return false;
        } else if (!timestamp.equals(other.timestamp))
            return false;
        return true;
    }

    public String getId() {
        return this.id;
    }

    public boolean isDeleted() {
        return deleted.equals("true");
    }

    public int synchronize(Note note) {
        int result = _timestamp.compareTo(note._timestamp);

        if (result < 0) {
            this.timestamp = note.timestamp;
            this._timestamp = note._timestamp;
            this.deleted = note.deleted;
            this.name = note.name;
            this.text = note.text;
        } else if (result > 0) {
            note.timestamp = this.timestamp;
            note._timestamp = this._timestamp;
            note.deleted = this.deleted;
            note.name = this.name;
            note.text = this.text;
        }

        return result;
    }

}
