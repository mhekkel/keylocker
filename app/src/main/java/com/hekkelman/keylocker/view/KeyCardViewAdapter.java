package com.hekkelman.keylocker.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;
import com.hekkelman.keylocker.datamodel.KeyNote;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class KeyCardViewAdapter extends KeyNoteCardViewAdapter<Key> {

    private KeyCardViewCallback keyCardViewCallback;


    public KeyCardViewAdapter(Context context) {
        super(context);
    }

    public void setCallback(KeyCardViewCallback cb) {
        this.keyCardViewCallback = cb;
    }

    @NonNull
    @Override
    public KeyCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_key_item, parent, false);

        KeyCardHolder holder = new KeyCardHolder(context, v);
        holder.setCallback(new KeyNoteCardHolder.KeyNoteCardHolderCallback() {
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

    @SuppressLint("NotifyDataSetChanged")
    public void loadEntries() {
        items = KeyDb.getKeys();
        notifyDataSetChanged();
    }

    protected void removeKey(int position) {
        try {
            Key key = items.get(position);
            KeyDb.deleteKey(key);
            items.remove(position);
            notifyItemRemoved(position);
        } catch (KeyDbException exception) {
//			Toast.makeText(MainActivity.this, R.string.sync_successful, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    void editHandler(String id) {
        keyCardViewCallback.onEditKey(id);
    }

    public interface KeyCardViewCallback {
        void onEditKey(String keyID);
    }

    protected static class KeyCardHolder extends KeyNoteCardHolder {

        public KeyCardHolder(Context context, View itemView) {
            super(context, itemView);
        }

        @Override
        protected String getItemTextForCopy() {
            return ((Key)item).getPassword();
        }

        protected void setItem(KeyNote keyNote) {
            super.setItem(keyNote);
            infoView.setText(((Key) keyNote).getUser());
        }
    }
}

//// New CardView/RecycleView based interface
//public class KeyCardViewAdapter extends RecyclerView.Adapter<KeyCardViewAdapter.KeyCardHolder>
//	implements Filterable {
//
//	private final Settings settings;
//	private final Context context;
//	public List<Key> keys;
//	private KeyCardViewCallback keyCardViewCallback;
//	private Filter searchFilter;
//
//	public KeyCardViewAdapter(Context context) {
//		this.context = context;
//		this.settings = new Settings(context);
//	}
//
//	public interface KeyCardViewCallback {
//		void onEditKey(String keyID);
//	}
//
//	public void setCallback(KeyCardViewCallback cb) {
//		this.keyCardViewCallback = cb;
//	}
//
//	@NonNull
//	@Override
//	public KeyCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_key_item, parent, false);
//
//		KeyCardHolder holder = new KeyCardHolder(context, v);
//		holder.setCallback(new KeyCardHolder.KeyCardHolderCallback() {
//			@Override
//			public void onMenuButtonClicked(View view, int position) {
//				showPopupMenu(view, position);
//			}
//
//			@Override
//			public void onCopyButtonClicked(String text) {
//				copyHandler(text, settings.isMinimizeAppOnCopyEnabled());
//			}
//
//			@Override
//			public void onCardSingleClicked(String keyID) {
//				onCardTapped(keyID, settings.getTapSingle());
//			}
//
//			@Override
//			public void onCardDoubleClicked(String keyID) {
//				onCardTapped(keyID, settings.getTapDouble());
//			}
//		});
//
//		return holder;
//	}
//
//	private void onCardTapped(String keyID, Settings.TapMode tapMode) {
//		Key key = KeyDb.getKey(keyID);
//		switch (tapMode) {
//			case EDIT:EDIT
//				keyCardViewCallback.onEditKey(key.getId());
//				break;
//			case COPY:
//				copyHandler(key.getPassword(), false);
//				break;
//			case COPY_BACKGROUND:
//				copyHandler(key.getPassword(), true);
//				break;
//			case SEND_KEYSTROKES:
////						sendKeystrokes(position);
//				break;
//			default:
//				break;
//		}
//	}
//
//	@SuppressLint("NotifyDataSetChanged")
//	public void loadEntries() {
//		keys = KeyDb.getKeys();
//		notifyDataSetChanged();
//	}
//
//	private void removeKey(int position) {
//		try {
//			Key key = keys.get(position);
//			KeyDb.deleteKey(key);
//			keys.remove(position);
//			notifyItemRemoved(position);
//		} catch (KeyDbException exception) {
////			Toast.makeText(MainActivity.this, R.string.sync_successful, Toast.LENGTH_LONG).show();
//		}
//	}
//
//	private void showPopupMenu(View view, int pos) {
//		View menuItemView = view.findViewById(R.id.menuButton);
//		PopupMenu popup = new PopupMenu(view.getContext(), menuItemView);
//		MenuInflater inflate = popup.getMenuInflater();
//		inflate.inflate(R.menu.menu_popup, popup.getMenu());
//
//		popup.setOnMenuItemClickListener(item -> {
//			int id = item.getItemId();
//			if (id == R.id.menu_popup_edit) {
//				Key key = keys.get(pos);
//				keyCardViewCallback.onEditKey(key.getId());
//				return true;
//			} else if (id == R.id.menu_popup_remove) {
//				removeKey(pos);
//				return true;
//			} else return false;
//		});
//		popup.show();
//	}
//
//	private void copyHandler(final String text, final boolean dropToBackground) {
//		Tools.copyToClipboard(context, text);
//
//		if (dropToBackground) ((MainActivity) context).moveTaskToBack(true);
//	}
//
//	@Override
//	public void onBindViewHolder(KeyCardHolder holder, int position) {
//		holder.setKey(keys.get(position));
//	}
//
//	@Override
//	public int getItemCount() {
//		return keys.size();
//	}
//
//	@Override
//	public long getItemId(int position) {
//		return keys.get(position).getListID();
//	}
//
//
//	protected static class KeyCardHolder extends RecyclerView.ViewHolder {
//		protected Key key;
//		protected CardView card;
//		protected TextView nameView;
//		protected TextView userView;
//		protected ImageButton copyButton;
//		protected ImageButton menuButton;
//
//		private KeyCardHolderCallback keyCardHolderCallback;
//
//		public interface KeyCardHolderCallback {
//			void onMenuButtonClicked(View view, int position);
//			void onCopyButtonClicked(String password);
//			void onCardSingleClicked(String keyID);
//			void onCardDoubleClicked(String keyID);
//		}
//
//		public KeyCardHolder(Context context, View itemView) {
//			super(itemView);
//
//			// Style the buttons in the current theme colors
//			ColorFilter colorFilter = Tools.getThemeColorFilter(context, android.R.attr.textColorSecondary);
//
//			card = (CardView) itemView;
//			nameView = itemView.findViewById(R.id.itemCaption);
//			userView = itemView.findViewById(R.id.itemUser);
//			menuButton = itemView.findViewById(R.id.menuButton);
//			copyButton = itemView.findViewById(R.id.copyButton);
//
//			menuButton.getDrawable().setColorFilter(colorFilter);
//			copyButton.getDrawable().setColorFilter(colorFilter);
//
//			menuButton.setOnClickListener(view ->
//					adapterPositionSafeCallback((callback, adapterPosition) ->
//							callback.onMenuButtonClicked(view, adapterPosition)
//					)
//			);
//
//			copyButton.setOnClickListener(view ->
//					adapterPositionSafeCallback((callback, adapterPosition) ->
//							callback.onCopyButtonClicked(key.getPassword())
//					)
//			);
//
//			card.setOnClickListener(new SimpleDoubleClickListener() {
//				@Override
//				public void onSingleClick(View v) {
//					adapterPositionSafeCallback((callback, adapterPosition) ->
//							callback.onCardSingleClicked(key.getId())
//					);
//				}
//
//				@Override
//				public void onDoubleClick(View v) {
//					adapterPositionSafeCallback((callback, adapterPosition) ->
//							callback.onCardDoubleClicked(key.getId())
//					);
//				}
//			});
//		}
//
//		protected void setKey(Key key) {
//			this.key = key;
//
//			nameView.setText(key.getName());
//			userView.setText(key.getUser());
//		}
//
//		@FunctionalInterface
//		private interface AdapterPositionSafeCallbackConsumer {
//			/** The specified {@link KeyCardHolderCallback} is guaranteed to be non-null, and adapterPosition is
//			 * guaranteed to be a valid position. */
//			void accept(@NonNull KeyCardHolderCallback callback, int adapterPosition);
//		}
//
//		private void adapterPositionSafeCallback(KeyCardHolder.AdapterPositionSafeCallbackConsumer safeCallback) {
//			int clickedPosition = getBindingAdapterPosition();
//			if (keyCardHolderCallback != null && clickedPosition != RecyclerView.NO_POSITION) {
//				safeCallback.accept(keyCardHolderCallback, clickedPosition);
//			}
//		}
//
//		public void setCallback(KeyCardHolderCallback cb) {
//			this.keyCardHolderCallback = cb;
//		}
//	}
//
//	public class KeyFilter extends Filter {
//		@Override
//		protected FilterResults performFiltering(CharSequence constraint) {
//			FilterResults results = new FilterResults();
//			List<Key> filtered;
//
//			if (constraint != null && constraint.length() > 0) {
//				constraint = constraint.toString().toUpperCase();
//				filtered = KeyDb.getFilteredKeys(constraint.toString());
//			} else
//				filtered = KeyDb.getKeys();
//
//			results.values = filtered;
//			return results;
//		}
//
//		@SuppressLint("NotifyDataSetChanged")
//		@Override
//		protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
//			keys = Collections.unmodifiableList((List<Key>) filterResults.values);
//			notifyDataSetChanged();
//		}
//	}
//
//	@Override
//	public Filter getFilter() {
//		if (searchFilter == null)
//			searchFilter = new KeyFilter();
//		return searchFilter;
//	}
//}
