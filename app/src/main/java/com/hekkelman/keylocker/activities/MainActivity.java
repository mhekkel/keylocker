package com.hekkelman.keylocker.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.hekkelman.keylocker.KeyLockerApp;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.databinding.ActivityMainBinding;
import com.hekkelman.keylocker.databinding.CardviewKeyItemBinding;
import com.hekkelman.keylocker.databinding.DialogAskPasswordBinding;
import com.hekkelman.keylocker.datamodel.KeyDbException;
import com.hekkelman.keylocker.datamodel.KeyNote;
import com.hekkelman.keylocker.tasks.SyncSDTask;
import com.hekkelman.keylocker.tasks.TaskResult;
import com.hekkelman.keylocker.utilities.AppContainer;
import com.hekkelman.keylocker.utilities.Settings;
import com.hekkelman.keylocker.utilities.Tools;
import com.hekkelman.keylocker.utilities.SimpleDoubleClickListener;

import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends KeyDbBaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private KeyNoteCardViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private String mQuery;
    private ActivityResultLauncher<Intent> mNewKeyResult;
    private ActivityResultLauncher<Intent> mEditKeyResult;
    private KEY_OR_NOTE_TYPE mType = KEY_OR_NOTE_TYPE.KEY;
    private SyncSDTask mSyncSDTask;
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppContainer appContainer = ((KeyLockerApp) getApplication()).mAppContainer;
        this.mSyncSDTask = new SyncSDTask(this, appContainer.executorService, appContainer.mainThreadHandler);

        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = mBinding.getRoot();
        setContentView(view);

        mRecyclerView = mBinding.recyclerView;
        NavigationView navigationView = mBinding.navView;
        FloatingActionButton fabView = mBinding.fab;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        Toolbar toolbar = mBinding.toolbar;
        setSupportActionBar(toolbar);

        mNewKeyResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onEditKeyResult);

        mEditKeyResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onEditKeyResult);

        DrawerLayout drawer = mBinding.drawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        MenuItem mi = navigationView.getMenu().findItem(R.id.nav_keys);
        if (mi != null) mi.setChecked(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new KeyNoteCardViewAdapter(this::onCardTapped, this::onCardCopy, this::onCardEdit, this::onCardRemove);
        mRecyclerView.setAdapter(mAdapter);

        fabView.setOnClickListener(this::onClickFab);

        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
            mQuery = intent.getStringExtra(SearchManager.QUERY);
    }

    private void onCardCopy(KeyNote keyNote) {
        Tools.copyToClipboard(this, keyNote.getText());
    }

    private void onCardEdit(KeyNote keyNote) {
        Intent intent;
        if (keyNote instanceof KeyNote.Key) {
            intent = new Intent(MainActivity.this, KeyDetailActivity.class);
            intent.putExtra("key-id", keyNote.getId());
        } else {
            intent = new Intent(MainActivity.this, NoteDetailActivity.class);
            intent.putExtra("note-id", keyNote.getId());
        }
        mEditKeyResult.launch(intent);
    }

    private void onCardRemove(KeyNote keyNote) {
        try {
            mViewModel.keyDb.delete(keyNote);
            loadData();

            Snackbar.make(mRecyclerView, R.string.key_was_removed, BaseTransientBottomBar.LENGTH_LONG)
                    .setAction(R.string.undo_remove, view -> {
                        mViewModel.keyDb.undoDelete(keyNote);
                        loadData();
                    })
                    .show();

        } catch (KeyDbException exception) {
            exception.printStackTrace();
        }
    }


    private void onEditKeyResult(ActivityResult result) {
        loadData();
    }

    public void onClickFab(View view) {
        Intent intent = new Intent(MainActivity.this,
                mType == KEY_OR_NOTE_TYPE.KEY ? KeyDetailActivity.class : NoteDetailActivity.class);
        mNewKeyResult.launch(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
            mAdapter.getFilter().filter(intent.getStringExtra(SearchManager.QUERY));
    }

    @Override
    public void loadData() {
        loadData(mType);
    }

    public void loadData(KEY_OR_NOTE_TYPE type) {
        if (mViewModel.keyDb != null) {
            List<KeyNote> items;
            if (type == KEY_OR_NOTE_TYPE.KEY)
                items = mViewModel.keyDb.getAllKeys().stream().map(k -> (KeyNote) k).collect(Collectors.toList());
            else
                items = mViewModel.keyDb.getAllNotes().stream().map(k -> (KeyNote) k).collect(Collectors.toList());

            mAdapter.loadEntries(items);
        }
        mType = type;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!TextUtils.isEmpty(mQuery))
            mAdapter.getFilter().filter(mQuery);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = mBinding.drawerLayout;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mainmenu, menu);

        // Get the SearchView and set the searchable configuration
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
//        // Assumes current activity is the searchable activity
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }

    private void onCardTapped(KeyNote keyNote, boolean doubleTap) {
        Settings.TapMode tapMode = doubleTap ? mSettings.getTapDouble() : mSettings.getTapSingle();

        switch (tapMode) {
            case EDIT:
                onCardEdit(keyNote);
                break;
            case COPY:
                onCardCopy(keyNote);
                break;
            case COPY_BACKGROUND:
                onCardCopy(keyNote);
                moveTaskToBack(true);
                break;
            case SEND_KEYSTROKES:
//						sendKeystrokes(position);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_keys) {
            if (mType != KEY_OR_NOTE_TYPE.KEY) loadData(KEY_OR_NOTE_TYPE.KEY);
        } else if (id == R.id.nav_notes) {
            if (mType != KEY_OR_NOTE_TYPE.NOTE) loadData(KEY_OR_NOTE_TYPE.NOTE);
        } else if (id == R.id.nav_sync_sdcard) {
            syncWithSDCard();
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = mBinding.drawerLayout;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void syncWithSDCard() {
        String backupDir = mSettings.getLocalBackupDir();

        if (TextUtils.isEmpty(backupDir)) {
            Snackbar.make(mRecyclerView, R.string.backup_toast_no_location, BaseTransientBottomBar.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri backupDirUri = Uri.parse(backupDir);
            mSyncSDTask.syncToSD(this, backupDirUri, null, this::onTaskResult);
        } catch (Exception e) {
            syncFailed(e.getMessage());
        }
    }

    void onTaskResult(TaskResult<Void> result) {
        if (result instanceof TaskResult.Success) {
            Snackbar.make(mRecyclerView, R.string.sync_successful, BaseTransientBottomBar.LENGTH_SHORT).show();
//            mAdapter.loadEntries(mViewModel.keyDb);
            loadData();
        } else {
            Exception e = ((TaskResult.Error<Void>) result).exception;

            if (e instanceof KeyDbException.InvalidPasswordException) {
                DialogAskPasswordBinding binding = DialogAskPasswordBinding.inflate(getLayoutInflater());
                final View view = binding.getRoot();
                final EditText pw = binding.dlogPassword;
                if (mSettings.getBlockAccessibility())
                    pw.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);

                if (mSettings.getBlockAutofill())
                    pw.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);

                new AlertDialog.Builder(MainActivity.this)
                        .setView(view)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            String backupDir = mSettings.getLocalBackupDir();

                            Uri backupDirUri = Uri.parse(backupDir);
                            mSyncSDTask.syncToSD(this, backupDirUri, null, this::onTaskResult);
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        })
                        .show();

            } else {
                syncFailed(e.getMessage());
            }
        }
    }

    void syncFailed(String errorMessage) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.sync_failed)
                .setMessage(getString(R.string.sync_failed_msg) + errorMessage)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private enum KEY_OR_NOTE_TYPE {KEY, NOTE}

    public static class KeyNoteCardViewAdapter extends RecyclerView.Adapter<KeyNoteCardViewAdapter.KeyNoteCardHolder>
            implements Filterable {

        private final CardActionCallback editCallback;
        private final CardActionCallback removeCallback;
        protected List<KeyNote> items, allItems;
        protected CardTappedCallback tappedCallback;
        protected SimpleDoubleClickListener clickCallback;
        protected CardActionCallback copyCallback;


        @Override
        public Filter getFilter() {
            return new KeyNoteFilter();
        }

        public interface CardTappedCallback {
            void onCardTapped(KeyNote item, boolean doubleTap);
        }

        public interface CardActionCallback {
            void action(KeyNote item);
        }

        public KeyNoteCardViewAdapter(CardTappedCallback tappedCallback, CardActionCallback copyCallback,
                                      CardActionCallback editCallback, CardActionCallback removeCallback) {
            this.tappedCallback = tappedCallback;
            this.copyCallback = copyCallback;
            this.editCallback = editCallback;
            this.removeCallback = removeCallback;

            this.clickCallback = new SimpleDoubleClickListener() {
                @Override
                public void onClick(View v, boolean doubleClick) {
                    tappedCallback.onCardTapped((KeyNote) v.getTag(), doubleClick);
                }
            };
        }

        public void loadEntries(List<KeyNote> items) {
            allItems = items;
            this.items = items;
            notifyDataSetChanged();;
        }

        @NonNull
        @Override
        public KeyNoteCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            CardviewKeyItemBinding binding =
                    CardviewKeyItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

            return new KeyNoteCardHolder(binding, clickCallback, this::onCopyButtonClicked, this::onMenuButtonClicked);
        }

        private void onCopyButtonClicked(View view) {
            KeyNote item = (KeyNote) view.getTag();
            if (item != null && copyCallback != null)
                copyCallback.action(item);
        }

        protected void onMenuButtonClicked(View view) {
            View menuItemView = view.findViewById(R.id.menuButton);
            PopupMenu popup = new PopupMenu(view.getContext(), menuItemView);
            MenuInflater inflate = popup.getMenuInflater();
            inflate.inflate(R.menu.menu_popup, popup.getMenu());

            KeyNote keyNote = (KeyNote)view.getTag();

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_popup_edit) {
                    editCallback.action(keyNote);
                    return true;
                } else if (id == R.id.menu_popup_remove) {
                    removeCallback.action(keyNote);
                    return true;
                } else return false;
            });
            popup.show();
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

        static class KeyNoteCardHolder extends RecyclerView.ViewHolder {
            protected TextView nameView;
            protected TextView infoView;
            protected ImageButton copyButton;
            protected ImageButton menuButton;

            public KeyNoteCardHolder(CardviewKeyItemBinding binding, View.OnClickListener onClickListener,
                                     View.OnClickListener onCopyClick, View.OnClickListener onMenuClick) {
                super(binding.getRoot());

                itemView.setOnClickListener(onClickListener);

                nameView = binding.itemCaption;
                infoView = binding.itemUser;
                copyButton = binding.copyButton;
                menuButton = binding.menuButton;

                copyButton.setOnClickListener(onCopyClick);
                menuButton.setOnClickListener(onMenuClick);
            }

            protected void setItem(KeyNote keyNote) {
                itemView.setTag(keyNote);
                copyButton.setTag(keyNote);
                menuButton.setTag(keyNote);

                nameView.setText(keyNote.getName());
                infoView.setText(keyNote.getDescription());
            }
        }

        public class KeyNoteFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<KeyNote> filtered = allItems;

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
                items = (List<KeyNote>) filterResults.values;
                notifyDataSetChanged();
            }
        }

    }

}
