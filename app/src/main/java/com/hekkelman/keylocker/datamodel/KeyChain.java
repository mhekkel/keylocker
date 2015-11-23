package com.hekkelman.keylocker.datamodel;


import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class KeyChain {
	@ElementList(type=Key.class)
	private List<Key> keys;

	@ElementList(type=Note.class)
	private List<Note> notes;
	
	public KeyChain() {
		this.keys = new Vector<Key>();
		this.notes = new Vector<Note>();
	}

	public List<Key> getKeys() {
		return keys;
	}

	public void setKeys(List<Key> keys) {
		this.keys = keys;
	}

	public List<Note> getNotes() {
		return notes;
	}

	public void setNotes(List<Note> notes) {
		this.notes = notes;
	}

	// more accessors
	Key getKeyByID(String id) {
		for (Key k : this.keys) {
			if (k.getId().equals(id))
				return k;
		}
		return null;
	}

	Note getNoteByID(String id) {
		for (Note n : this.notes) {
			if (n.getId().equals(id))
				return n;
		}
		return null;
	}

	public boolean synchronize(KeyChain kc) {
		boolean changed = false;

		for (Key key: this.keys) {
			Key kcKey = kc.getKeyByID(key.getId());
			if (kcKey == null) {
				kc.keys.add(new Key(key));
				changed = true;
			}
			else if (kcKey.synchronize(key) != 0)
				changed = true;
		}

		for (Key key: kc.keys) {
			Key tKey = this.getKeyByID(key.getId());
			if (tKey == null)
				this.keys.add(new Key(key));
			else
				tKey.synchronize(key);
		}

		for (Note note: this.notes) {
			Note kcNote = kc.getNoteByID(note.getId());
			if (kcNote == null) {
				kc.notes.add(new Note(note));
				changed = true;
			}
			else if (kcNote.synchronize(note) != 0)
				changed = true;
		}

		for (Note Note: kc.notes) {
			Note tNote = this.getNoteByID(Note.getId());
			if (tNote == null)
				this.notes.add(new Note(Note));
			else
				tNote.synchronize(Note);
		}

		purge();

		return changed;
	}

	private void purge() {
		for (Iterator<Key> keyIterator = this.keys.listIterator(); keyIterator.hasNext(); ) {
			Key key = keyIterator.next();
			if (key.isDeleted())
				keyIterator.remove();
		}

		for (Iterator<Note> noteIterator = this.notes.listIterator(); noteIterator.hasNext(); ) {
			Note note = noteIterator.next();
			if (note.isDeleted())
				noteIterator.remove();
		}
	}

	public Key createKey() {
		Key key = new Key();
		this.keys.add(key);
		return key;
	}
}
