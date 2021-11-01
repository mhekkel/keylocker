package com.hekkelman.keylocker.view;

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

import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.activities.MainActivity;
import com.hekkelman.keylocker.datamodel.KeyDbException;
import com.hekkelman.keylocker.datamodel.KeyNote;
import com.hekkelman.keylocker.utilities.Settings;
import com.hekkelman.keylocker.utilities.Tools;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

// New CardView/RecycleView based interface
public abstract class KeyNoteCardViewAdapter<KeyOrNote extends KeyNote> extends RecyclerView.Adapter<KeyNoteCardViewAdapter.KeyNoteCardHolder>
        implements Filterable {

    public final Context context;
    protected final Settings settings;
    protected List<KeyOrNote> items;
    protected Filter searchFilter;
    protected KeyNoteEditCallback keyNoteEditCallback;
    protected KeyNoteRemovedCallback keyNoteRemovedCallback;

    public KeyNoteCardViewAdapter(Context context) {
        this.context = context;
        this.settings = new Settings(context);
    }

    @NonNull
    @Override
    public KeyNoteCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_key_item, parent, false);

        KeyNoteCardHolder holder = new KeyNoteCardHolder(context, v);
        holder.setCallback(new KeyNoteCardHolder.KeyNoteCardHolderCallback() {
            @Override
            public void onMenuButtonClicked(View view, int position) {
                showPopupMenu(view, position);
            }

            @Override
            public void onCopyButtonClicked(String text) {
                copyHandler(text, false);
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

    protected abstract void onCardTapped(String keyID, Settings.TapMode tapMode);

    public void setEditCallback(KeyNoteEditCallback cb) {
        this.keyNoteEditCallback = cb;
    }

    public void setKeyNoteRemovedCallback(KeyNoteRemovedCallback cb) {
        this.keyNoteRemovedCallback = cb;
    }

    abstract public void loadEntries();

    protected void removeHandler(int position) {
        try {
            KeyOrNote item = items.get(position);
            removeKeyOrNote(item);
            items.remove(position);
            notifyItemRemoved(position);

            if (keyNoteRemovedCallback != null)
                keyNoteRemovedCallback.onRemoved(item.getId());
        } catch (KeyDbException exception) {
//			Toast.makeText(MainActivity.this, R.string.sync_successful, Toast.LENGTH_LONG).show();
        }
    }

    protected abstract void removeKeyOrNote(KeyNote keyOrNote) throws KeyDbException;

    protected void showPopupMenu(View view, int pos) {
        View menuItemView = view.findViewById(R.id.menuButton);
        PopupMenu popup = new PopupMenu(view.getContext(), menuItemView);
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.menu_popup, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_popup_edit) {
                KeyOrNote keyNote = items.get(pos);
                editHandler(keyNote.getId());
                return true;
            } else if (id == R.id.menu_popup_remove) {
                removeHandler(pos);
                return true;
            } else return false;
        });
        popup.show();
    }

    void editHandler(final String id) {
        this.keyNoteEditCallback.onEdit(id);
    }

    protected void copyHandler(final String text, final boolean dropToBackground) {
        Tools.copyToClipboard(context, text);
        if (dropToBackground) ((MainActivity) context).moveTaskToBack(true);
    }

    @Override
    public void onBindViewHolder(KeyNoteCardHolder holder, int position) {
        holder.setItem(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getListID();
    }

    public interface KeyNoteEditCallback {
        void onEdit(String keyID);
    }

    public interface KeyNoteRemovedCallback {
        void onRemoved(String keyID);
    }

    static class KeyNoteCardHolder extends RecyclerView.ViewHolder {
        protected KeyNote item;
        protected CardView card;
        protected TextView nameView;
        protected TextView infoView;
        protected ImageButton copyButton;
        protected ImageButton menuButton;

        private KeyNoteCardHolderCallback keyNoteCardHolderCallback;

        public KeyNoteCardHolder(Context context, View itemView) {
            super(itemView);

            // Style the buttons in the current theme colors
            ColorFilter colorFilter = Tools.getThemeColorFilter(context, android.R.attr.textColorSecondary);

            card = (CardView) itemView;
            nameView = itemView.findViewById(R.id.itemCaption);
            infoView = itemView.findViewById(R.id.itemUser);
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
                            callback.onCopyButtonClicked(item.getDescription())
                    )
            );

            card.setOnClickListener(new SimpleDoubleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    adapterPositionSafeCallback((callback, adapterPosition) ->
                            callback.onCardSingleClicked(item.getId())
                    );
                }

                @Override
                public void onDoubleClick(View v) {
                    adapterPositionSafeCallback((callback, adapterPosition) ->
                            callback.onCardDoubleClicked(item.getId())
                    );
                }
            });
        }

        protected void setItem(KeyNote keyNote) {
            this.item = keyNote;

            nameView.setText(keyNote.getName());
            infoView.setText(keyNote.getDescription());
        }

        protected void adapterPositionSafeCallback(AdapterPositionSafeCallbackConsumer safeCallback) {
            int clickedPosition = getBindingAdapterPosition();
            if (keyNoteCardHolderCallback != null && clickedPosition != RecyclerView.NO_POSITION) {
                safeCallback.accept(keyNoteCardHolderCallback, clickedPosition);
            }
        }

        public void setCallback(KeyNoteCardHolderCallback cb) {
            this.keyNoteCardHolderCallback = cb;
        }

        public interface KeyNoteCardHolderCallback {
            void onMenuButtonClicked(View view, int position);

            void onCopyButtonClicked(String text);

            void onCardSingleClicked(String keyID);

            void onCardDoubleClicked(String keyID);
        }

        @FunctionalInterface
        private interface AdapterPositionSafeCallbackConsumer {
            /**
             * The specified {@link KeyNoteCardHolderCallback} is guaranteed to be non-null, and adapterPosition is
             * guaranteed to be a valid position.
             */
            void accept(@NonNull KeyNoteCardHolderCallback callback, int adapterPosition);
        }
    }

}
