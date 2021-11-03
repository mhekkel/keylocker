package com.hekkelman.keylocker.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Filter;

import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbDao;
import com.hekkelman.keylocker.datamodel.KeyDbException;
import com.hekkelman.keylocker.datamodel.KeyNote;
import com.hekkelman.keylocker.utilities.Settings;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class KeyCardViewAdapter extends KeyNoteCardViewAdapter<Key> {

    private KeyDbDao keyDb;

    public KeyCardViewAdapter(Context context) {
        super(context);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadEntries(KeyDb keyDb) {
        this.keyDb = keyDb;
        items = keyDb.getAllKeys();
        notifyDataSetChanged();
    }

    protected void removeKeyOrNote(KeyNote key) throws KeyDbException {
        keyDb.deleteKey((Key)key);
    }

    protected void onCardTapped(String keyID, Settings.TapMode tapMode) {
        Optional<Key> key = keyDb.getKey(keyID);
        if (key.isPresent()) {
            switch (tapMode) {
                case EDIT:
                    editHandler(key.get().getId());
                    break;
                case COPY:
                    copyHandler(key.get().getPassword(), false);
                    break;
                case COPY_BACKGROUND:
                    copyHandler(key.get().getPassword(), true);
                    break;
                case SEND_KEYSTROKES:
//						sendKeystrokes(position);
                    break;
                default:
                    break;
            }
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
            List<Key> filtered = keyDb.getAllKeys();

            if (!TextUtils.isEmpty(constraint)) {
                String finalConstraint = constraint.toString();
                filtered = filtered.stream()
                        .filter(key -> key.match(finalConstraint))
                        .collect(Collectors.toList());
            }

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
