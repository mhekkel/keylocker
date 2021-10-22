package com.hekkelman.keylocker.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ColorFilter;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.activities.MainActivity;
import com.hekkelman.keylocker.datamodel.Note;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;
import com.hekkelman.keylocker.utilities.Settings;
import com.hekkelman.keylocker.utilities.Tools;

import java.util.Collections;
import java.util.List;

// New CardView/RecycleView based interface
public class NoteCardViewAdapter extends RecyclerView.Adapter<NoteCardViewAdapter.NoteCardHolder>
	implements Filterable {

	private final Settings settings;
	private final Context context;
	public List<Note> notes;
	private NoteCardViewCallback noteCardViewCallback;
	private Filter searchFilter;

	public NoteCardViewAdapter(Context context) {
		this.context = context;
		this.settings = new Settings(context);
	}

	public interface NoteCardViewCallback {
		void onEditNote(String noteID);
	}

	public void setCallback(NoteCardViewCallback cb) {
		this.noteCardViewCallback = cb;
	}

	@NonNull
	@Override
	public NoteCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_note_item, parent, false);

		NoteCardHolder holder = new NoteCardHolder(context, v);
		holder.setCallback(new NoteCardHolder.NoteCardHolderCallback() {
			@Override
			public void onMenuButtonClicked(View view, int position) {
				showPopupMenu(view, position);
			}

			@Override
			public void onCopyButtonClicked(String text) {
				copyHandler(text, settings.isMinimizeAppOnCopyEnabled());
			}

			@Override
			public void onCardSingleClicked(String noteID) {
				onCardTapped(noteID, settings.getTapSingle());
			}

			@Override
			public void onCardDoubleClicked(String noteID) {
				onCardTapped(noteID, settings.getTapDouble());
			}
		});

		return holder;
	}

	private void onCardTapped(String noteID, Settings.TapMode tapMode) {
		Note note = KeyDb.getNote(noteID);
		switch (tapMode) {
			case EDIT:
				noteCardViewCallback.onEditNote(note.getId());
				break;
			case COPY:
				copyHandler(note.getText(), false);
				break;
			case COPY_BACKGROUND:
				copyHandler(note.getText(), true);
				break;
//			case SEND_NOTESTROKES:
//						sendKeystrokes(position);
//				break;
			default:
				break;
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void loadEntries() {
		notes = KeyDb.getNotes();
		notifyDataSetChanged();
	}

	private void removeNote(int position) {
		try {
			Note note = notes.get(position);
			KeyDb.deleteNote(note);
			notes.remove(position);
			notifyItemRemoved(position);
		} catch (KeyDbException exception) {
//			Toast.makeText(MainActivity.this, R.string.sync_successful, Toast.LENGTH_LONG).show();
		}
	}

	private void showPopupMenu(View view, int pos) {
		View menuItemView = view.findViewById(R.id.menuButton);
		PopupMenu popup = new PopupMenu(view.getContext(), menuItemView);
		MenuInflater inflate = popup.getMenuInflater();
		inflate.inflate(R.menu.menu_popup, popup.getMenu());

		popup.setOnMenuItemClickListener(item -> {
			int id = item.getItemId();
			if (id == R.id.menu_popup_edit) {
				Note note = notes.get(pos);
				noteCardViewCallback.onEditNote(note.getId());
				return true;
			} else if (id == R.id.menu_popup_remove) {
				removeNote(pos);
				return true;
			} else return false;
		});
		popup.show();
	}

	private void copyHandler(final String text, final boolean dropToBackground) {
		Tools.copyToClipboard(context, text);

		if (dropToBackground) ((MainActivity) context).moveTaskToBack(true);
	}

	@Override
	public void onBindViewHolder(NoteCardHolder holder, int position) {
		holder.setNote(notes.get(position));
	}

	@Override
	public int getItemCount() {
		return notes.size();
	}

	@Override
	public long getItemId(int position) {
		return notes.get(position).getListID();
	}


	protected static class NoteCardHolder extends RecyclerView.ViewHolder {
		protected Note note;
		protected CardView card;
		protected TextView nameView;
		protected TextView userView;
		protected ImageButton copyButton;
		protected ImageButton menuButton;

		private NoteCardHolderCallback noteCardHolderCallback;

		public interface NoteCardHolderCallback {
			void onMenuButtonClicked(View view, int position);
			void onCopyButtonClicked(String password);
			void onCardSingleClicked(String noteID);
			void onCardDoubleClicked(String noteID);
		}

		public NoteCardHolder(Context context, View itemView) {
			super(itemView);

			// Style the buttons in the current theme colors
			ColorFilter colorFilter = Tools.getThemeColorFilter(context, android.R.attr.textColorSecondary);

			card = (CardView) itemView;
			nameView = itemView.findViewById(R.id.itemCaption);
			userView = itemView.findViewById(R.id.itemUser);
			menuButton = itemView.findViewById(R.id.menuButton);
			copyButton = itemView.findViewById(R.id.copyButton);

			menuButton.getDrawable().setColorFilter(colorFilter);
			copyButton.getDrawable().setColorFilter(colorFilter);

			menuButton.setOnClickListener(view ->
					adapterPositionSafeCallback((callback, adapterPosition) ->
							callback.onMenuButtonClicked(view, adapterPosition)
					)
			);

			copyButton.setOnClickListener(view ->
					adapterPositionSafeCallback((callback, adapterPosition) ->
							callback.onCopyButtonClicked(note.getText())
					)
			);

			card.setOnClickListener(new SimpleDoubleClickListener() {
				@Override
				public void onSingleClick(View v) {
					adapterPositionSafeCallback((callback, adapterPosition) ->
							callback.onCardSingleClicked(note.getId())
					);
				}

				@Override
				public void onDoubleClick(View v) {
					adapterPositionSafeCallback((callback, adapterPosition) ->
							callback.onCardDoubleClicked(note.getId())
					);
				}
			});
		}

		protected void setNote(Note note) {
			this.note = note;

			nameView.setText(note.getName());
			userView.setText(note.getText());
		}

		@FunctionalInterface
		private interface AdapterPositionSafeCallbackConsumer {
			/** The specified {@link NoteCardHolderCallback} is guaranteed to be non-null, and adapterPosition is
			 * guaranteed to be a valid position. */
			void accept(@NonNull NoteCardHolderCallback callback, int adapterPosition);
		}

		private void adapterPositionSafeCallback(AdapterPositionSafeCallbackConsumer safeCallback) {
			int clickedPosition = getBindingAdapterPosition();
			if (noteCardHolderCallback != null && clickedPosition != RecyclerView.NO_POSITION) {
				safeCallback.accept(noteCardHolderCallback, clickedPosition);
			}
		}

		public void setCallback(NoteCardHolderCallback cb) {
			this.noteCardHolderCallback = cb;
		}
	}

	public class NoteFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			List<Note> filtered;

			if (constraint != null && constraint.length() > 0) {
				constraint = constraint.toString().toUpperCase();
				filtered = KeyDb.getFilteredNotes(constraint.toString());
			} else
				filtered = KeyDb.getNotes();

			results.values = filtered;
			return results;
		}

		@SuppressLint("NotifyDataSetChanged")
		@Override
		protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
			notes = Collections.unmodifiableList((List<Note>) filterResults.values);
			notifyDataSetChanged();
		}
	}

	@Override
	public Filter getFilter() {
		if (searchFilter == null)
			searchFilter = new NoteFilter();
		return searchFilter;
	}
}
