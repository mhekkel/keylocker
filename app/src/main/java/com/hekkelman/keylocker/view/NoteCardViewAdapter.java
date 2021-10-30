//package com.hekkelman.keylocker.view;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Filter;
//
//import com.hekkelman.keylocker.R;
//import com.hekkelman.keylocker.datamodel.Key;
//import com.hekkelman.keylocker.datamodel.KeyDb;
//import com.hekkelman.keylocker.datamodel.KeyDbException;
//import com.hekkelman.keylocker.datamodel.KeyNote;
//import com.hekkelman.keylocker.datamodel.Note;
//import com.hekkelman.keylocker.utilities.Settings;
//
//import java.util.List;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//public class NoteCardViewAdapter extends KeyNoteCardViewAdapter<Note> {
//
//	public NoteCardViewAdapter(Context context) {
//		super(context);
//	}
//
//	@SuppressLint("NotifyDataSetChanged")
//	public void loadEntries() {
//		items = KeyDb.getNotes();
//		notifyDataSetChanged();
//	}
//
//	protected void removeKeyOrNote(KeyNote note) throws KeyDbException {
//		KeyDb.deleteNote((Note)note);
//	}
//
//	protected void onCardTapped(String noteID, Settings.TapMode tapMode) {
//		Note note = KeyDb.getNote(noteID);
//		switch (tapMode) {
//			case EDIT:
//				editHandler(note.getId());
//				break;
//			case COPY:
//				copyHandler(note.getText(), false);
//				break;
//			case COPY_BACKGROUND:
//				copyHandler(note.getText(), true);
//				break;
//			case SEND_KEYSTROKES:
////						sendKeystrokes(position);
//				break;
//			default:
//				break;
//		}
//	}
//
//	@Override
//	public Filter getFilter() {
//		if (searchFilter == null)
//			searchFilter = new NoteFilter();
//		return searchFilter;
//	}
//
//	public class NoteFilter extends Filter {
//		@Override
//		protected FilterResults performFiltering(CharSequence constraint) {
//			FilterResults results = new FilterResults();
//			List<Note> filtered;
//
//			if (constraint != null && constraint.length() > 0) {
//				constraint = constraint.toString().toUpperCase();
//				filtered = KeyDb.getFilteredNotes(constraint.toString());
//			} else
//				filtered = KeyDb.getNotes();
//
//			results.values = filtered;
//			return results;
//		}
//
//		@SuppressLint("NotifyDataSetChanged")
//		@Override
//		protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
//			items = (List<Note>) filterResults.values;
//			notifyDataSetChanged();
//		}
//	}
//}
