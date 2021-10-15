package com.hekkelman.keylocker.View;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.hekkelman.keylocker.Activities.MainActivity;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.Utilities.Settings;
import com.hekkelman.keylocker.Utilities.Tools;
import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;

import java.io.File;
import java.util.List;

// New CardView/RecycleView based interface
public class KeyCardViewAdapter extends RecyclerView.Adapter<KeyCardHolder> {

	private final Settings settings;
	private final Context context;
	private char[] password;
	private File keyDbFile;
	private List<Key> keys;
	private Callback callback;

	public void setPassword(char[] password) {

		this.password = password;
		loadEntries();

		notifyDataSetChanged();
	}

	public interface Callback {
		void onEditKey(String keyID);
		void onRemoveKey(String keyID);
	}

	public KeyCardViewAdapter(Context context, File keyDbFile) {
		this.context = context;
		this.settings = new Settings(context);
		this.keyDbFile = keyDbFile;
	}

	public void setCallback(Callback cb) {
		this.callback = cb;
	}

	@NonNull
	@Override
	public KeyCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_key_item, parent, false);

		KeyCardHolder holder = new KeyCardHolder(context, v, settings.getTapToReveal());
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
			public void onCardSingleClicked(String text) {
				switch (settings.getTapSingle()) {
					case NOTHING:
						break;
					case REVEAL:
//						establishPinIfNeeded(position);
//						cardTapToRevealHandler(position);
						break;
					case COPY:
//						establishPinIfNeeded(position);
						copyHandler(text, false);
						break;
					case COPY_BACKGROUND:
//						establishPinIfNeeded(position);
						copyHandler(text, true);
						break;
					case SEND_KEYSTROKES:
//						establishPinIfNeeded(position);
//						sendKeystrokes(position);
						break;
					default:
						// If tap-to-reveal is disabled a single tab still needs to establish the PIN
//						if (!settings.getTapToReveal())
//							establishPinIfNeeded(position);
						break;
				}
			}

			@Override
			public void onCardDoubleClicked(String text) {
				switch (settings.getTapDouble()) {
					case REVEAL:
//						establishPinIfNeeded(position);
//						cardTapToRevealHandler(position);
						break;
					case COPY:
						copyHandler(text, false);
						break;
					case COPY_BACKGROUND:
						copyHandler(text, true);
						break;
					case SEND_KEYSTROKES:
//						sendKeystrokes(position);
						break;
					default:
						break;
				}
			}
		});

		return holder;
	}

	public void loadEntries() {
		keys = KeyDb.getKeys();
		notifyDataSetChanged();
//			ArrayList<Entry> newEntries = DatabaseHelper.loadDatabase(context, encryptionKey);
//
//			entries.updateEntries(newEntries, true);
//			entriesChanged(RecyclerView.NO_POSITION);
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
			} else {
				return false;
			}
		});
		popup.show();
	}

	private void copyHandler(final String text, final boolean dropToBackground) {
		Tools.copyToClipboard(context, text);

		if (dropToBackground) {
			((MainActivity)context).moveTaskToBack(true);
		}
	}

	@Override
	public void onBindViewHolder(KeyCardHolder holder, int position) {
		holder.setKey(keys.get(position));
	}

	@Override
	public int getItemCount() {
		return keys.size();
	}
}
