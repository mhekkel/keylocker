package com.hekkelman.keylocker.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Filter;

import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbDao;
import com.hekkelman.keylocker.datamodel.KeyDbException;
import com.hekkelman.keylocker.datamodel.KeyNote;
import com.hekkelman.keylocker.datamodel.Note;
import com.hekkelman.keylocker.utilities.Settings;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NoteCardViewAdapter extends KeyNoteCardViewAdapter<Note> {

	private KeyDbDao keyDb;

	public NoteCardViewAdapter(Context context) {
		super(context);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void loadEntries() {
		items = keyDb.getAllNotes();
		notifyDataSetChanged();
	}

	protected void removeKeyOrNote(KeyNote note) throws KeyDbException {
		keyDb.deleteNote((Note)note);
	}

	protected void onCardTapped(String noteID, Settings.TapMode tapMode) {
		Optional<Note> note = keyDb.getNote(noteID);
		if (note.isPresent()) {
			switch (tapMode) {
				case EDIT:
					editHandler(note.get().getId());
					break;
				case COPY:
					copyHandler(note.get().getText(), false);
					break;
				case COPY_BACKGROUND:
					copyHandler(note.get().getText(), true);
					break;
				case SEND_KEYSTROKES:
//						sendKeystrokes(position);
					break;
				default:
					break;
			}
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	@Override
	public void loadEntries(KeyDb keyDb) {
		this.keyDb = keyDb;
		items = keyDb.getAllNotes();
		notifyDataSetChanged();
	}

	@Override
	public Filter getFilter() {
		if (searchFilter == null)
			searchFilter = new NoteFilter();
		return searchFilter;
	}

	public class NoteFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			List<Note> filtered;

			if (constraint != null && constraint.length() > 0) {
				constraint = constraint.toString().toUpperCase();
				CharSequence finalConstraint = constraint;
				filtered = keyDb.getAllNotes()
						.stream()
						.filter(note -> note.match(finalConstraint.toString()))
						.collect(Collectors.toList());
			} else
				filtered = keyDb.getAllNotes();

			results.values = filtered;
			return results;
		}

		@SuppressLint("NotifyDataSetChanged")
		@Override
		protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
			items = (List<Note>) filterResults.values;
			notifyDataSetChanged();
		}
	}
}
