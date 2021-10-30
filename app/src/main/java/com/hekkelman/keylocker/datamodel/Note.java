package com.hekkelman.keylocker.datamodel;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

@Root
public class Note extends KeyNote {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((text == null) ? 0 : text.hashCode());
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

    public int synchronize(Note note) {
        int result = super.synchronize(note);

        if (result < 0) this.text = note.text;
        else if (result > 0) note.text = this.text;

        return result;
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
