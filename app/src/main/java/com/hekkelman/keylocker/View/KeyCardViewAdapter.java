package com.hekkelman.keylocker.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hekkelman.keylocker.Activities.MainActivity;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.Utilities.Settings;
import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

// New CardView/RecycleView based interface
public class KeyCardViewAdapter extends RecyclerView.Adapter<KeyCardHolder> {

	private final Settings settings;
	private final Context context;
	private KeyDb mKeyDb;
	private List<Key> mKeys;
	private Callback callback;

	public void setPassword(char[] password, File file) throws KeyDbException {
		mKeyDb = new KeyDb(password, file);
		mKeys = mKeyDb.getKeys();
		notifyDataSetChanged();
	}

	public interface Callback {
		void onMoveEventStart();
		void onMoveEventStop();
	}

	public KeyCardViewAdapter(Context context) {
		this.context = context;
		this.settings = new Settings(context);
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
			public void onMoveEventStart() {
				if (callback != null)
					callback.onMoveEventStart();
			}

			@Override
			public void onMoveEventStop() {
				if (callback != null)
					callback.onMoveEventStop();
			}

			@Override
			public void onMenuButtonClicked(View parentView, int position) {
				showPopupMenu(parentView, position);
			}

			@Override
			public void onCopyButtonClicked(String text, int position) {
				copyHandler(position, text, settings.isMinimizeAppOnCopyEnabled());
			}

			@Override
			public void onCardSingleClicked(int position, String text) {
				switch (settings.getTapSingle()) {
					case NOTHING:
						break;
					case REVEAL:
//						establishPinIfNeeded(position);
//						cardTapToRevealHandler(position);
						break;
					case COPY:
//						establishPinIfNeeded(position);
						copyHandler(position, text, false);
						break;
					case COPY_BACKGROUND:
//						establishPinIfNeeded(position);
						copyHandler(position, text, true);
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
			public void onCardDoubleClicked(int position, String text) {
				switch (settings.getTapDouble()) {
					case REVEAL:
//						establishPinIfNeeded(position);
//						cardTapToRevealHandler(position);
						break;
					case COPY:
//						establishPinIfNeeded(position);
						copyHandler(position, text, false);
						break;
					case COPY_BACKGROUND:
//						establishPinIfNeeded(position);
						copyHandler(position, text, true);
						break;
					case SEND_KEYSTROKES:
//						establishPinIfNeeded(position);
//						sendKeystrokes(position);
						break;
					default:
						break;
				}
			}

//			@Override
//			public void onCounterClicked(int position) {
//
//			}
//
//			@Override
//			public void onCounterLongPressed(int position) {
//
//			}
		});

		return holder;
	}

	public void loadEntries() {
        if (mKeyDb != null) {
			mKeys = mKeyDb.getKeys();
			notifyDataSetChanged();
//			ArrayList<Entry> newEntries = DatabaseHelper.loadDatabase(context, encryptionKey);
//
//			entries.updateEntries(newEntries, true);
//			entriesChanged(RecyclerView.NO_POSITION);
        }
	}


	private void showPopupMenu(View parentView, int position) {
	}

	private void copyHandler(final int position, final String text, final boolean dropToBackground) {
//		Tools.copyToClipboard(context, text);
//		updateLastUsedAndFrequency(position, getRealIndex(position));
		if (dropToBackground) {
			((MainActivity)context).moveTaskToBack(true);
		}
	}

	@Override
	public void onBindViewHolder(KeyCardHolder holder, int position) {
		Key key = mKeys.get(position);
		holder.nameView.setText(key.getName());
		holder.userView.setText(key.getUser());
	}

	@Override
	public int getItemCount() {
		return mKeys.size();
	}
}
