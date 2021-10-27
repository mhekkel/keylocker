package com.hekkelman.keylocker.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Filter;

import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;
import com.hekkelman.keylocker.datamodel.KeyNote;
import com.hekkelman.keylocker.utilities.Settings;

import java.util.List;

public class KeyCardViewAdapter extends KeyNoteCardViewAdapter<Key> {

    public KeyCardViewAdapter(Context context) {
        super(context);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadEntries() {
        items = KeyDb.getKeys();
        notifyDataSetChanged();
    }

    protected void removeKeyOrNote(KeyNote key) throws KeyDbException {
        KeyDb.deleteKey((Key)key);
    }

    protected void onCardTapped(String keyID, Settings.TapMode tapMode) {
        Key key = KeyDb.getKey(keyID);
        switch (tapMode) {
            case EDIT:
                editHandler(key.getId());
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

    @Override
    public Filter getFilter() {
        if (searchFilter == null)
            searchFilter = new KeyFilter();
        return searchFilter;
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
            return results;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            items = (List<Key>) filterResults.values;
            notifyDataSetChanged();
        }
    }
}
