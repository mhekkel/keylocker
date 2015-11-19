package com.hekkelman.keylocker.com.hekkelman.keylocker.datamodel;


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
}
