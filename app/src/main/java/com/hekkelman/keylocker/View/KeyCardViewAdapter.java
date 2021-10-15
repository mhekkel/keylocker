package com.hekkelman.keylocker.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.hekkelman.keylocker.Activities.MainActivity;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.Utilities.Settings;
import com.hekkelman.keylocker.Utilities.Tools;
import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDb;

import java.util.ArrayList;
import java.util.List;

// New CardView/RecycleView based interface
public class KeyCardViewAdapter extends RecyclerView.Adapter<KeyCardHolder>
	implements Filterable {

	private final Settings settings;
	private final Context context;
	public List<Key> keys;
	private Callback callback;
	private Filter searchFilter;

	public KeyCardViewAdapter(Context context) {
		this.context = context;
		this.settings = new Settings(context);
	}

	public interface Callback {
		void onEditKey(String keyID);
		void onRemoveKey(String keyID);
	}

	public void setCallback(Callback cb) {
		this.callback = cb;
	}

	@NonNull
	@Override
	public KeyCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_key_item, parent, false);

		KeyCardHolder holder = new KeyCardHolder(context, v);
		holder.setCallback(new KeyCardHolder.Callback() {
			@Override
			public void onMenuButtonClicked(View view, int position) {
				showPopupMenu(view, position);
			}

			@Override
			public void onCopyButtonClicked(String text) {
				copyHandler(text, settings.isMinimizeAppOnCopyEnabled());
			}

			@Override
			public void onCardSingleClicked(String keyID) {
				onCardTapped(keyID, settings.getTapSingle());
			}

			@Override
			public void onCardDoubleClicked(String keyID) {
				onCardTapped(keyID, settings.getTapDouble());
			}
		});

		return holder;
	}

	private void onCardTapped(String keyID, Settings.TapMode tapMode) {
		Key key = KeyDb.getKey(keyID);
		switch (tapMode) {
			case EDIT:
				break;
			case COPY:
				copyHandler(key.getPassword(), false);
				break;
			case COPY_BACKGROUND:
				copyHandler(key.getPassword(), true);
				break;
			case SEND_KEYSTROKES:
//						sendKeystrokes(position);
				break;
			default:
				break;
		}
	}

	public void loadEntries() {
		keys = KeyDb.getKeys();
		notifyDataSetChanged();
	}

	private void showPopupMenu(View view, int pos) {
		View menuItemView = view.findViewById(R.id.menuButton);
		PopupMenu popup = new PopupMenu(view.getContext(), menuItemView);
		MenuInflater inflate = popup.getMenuInflater();
		inflate.inflate(R.menu.menu_popup, popup.getMenu());

		Key key = keys.get(pos);

		popup.setOnMenuItemClickListener(item -> {
			int id = item.getItemId();

			if (id == R.id.menu_popup_edit) {
				callback.onEditKey(key.getId());
				return true;
			} else if (id == R.id.menu_popup_remove) {
				callback.onRemoveKey(key.getId());
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
	public void onBindViewHolder(KeyCardHolder holder, int position) {
		holder.setKey(keys.get(position));
	}

	@Override
	public int getItemCount() {
		return keys.size();
	}

	@Override
	public long getItemId(int position) {
		return keys.get(position).getListID();
	}

	public class KeyFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			List<Key> filtered;

			if (constraint != null && constraint.length() > 0) {
				constraint = constraint.toString().toUpperCase();

				filtered = KeyDb.getFilteredKeys(constraint.toString());
			} else
				filtered = KeyDb.getKeys();

			results.values = filtered;
			results.count = filtered.size();

			return results;
		}

		@Override
		protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
			keys = (List<Key>) filterResults.values;
			notifyDataSetChanged();
		}
	}

	@Override
	public Filter getFilter() {
		if (searchFilter == null)
			searchFilter = new KeyFilter();
		return searchFilter;
	}
}
