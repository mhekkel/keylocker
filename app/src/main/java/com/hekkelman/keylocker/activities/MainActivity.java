package com.hekkelman.keylocker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.hekkelman.keylocker.KeyLockerApp;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.datamodel.InvalidPasswordException;
import com.hekkelman.keylocker.tasks.SyncSDTask;
import com.hekkelman.keylocker.tasks.TaskResult;
import com.hekkelman.keylocker.utilities.AppContainer;
import com.hekkelman.keylocker.utilities.Settings;
import com.hekkelman.keylocker.view.KeyCardViewAdapter;
import com.hekkelman.keylocker.view.KeyNoteCardViewAdapter;
import com.hekkelman.keylocker.view.NoteCardViewAdapter;

public class MainActivity extends KeyDbBaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private KeyNoteCardViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private String mQuery;
    private ActivityResultLauncher<Intent> mNewKeyResult;
    private ActivityResultLauncher<Intent> mEditKeyResult;
    private SHOW_KEYNOTE mType = SHOW_KEYNOTE.KEY;
    private SyncSDTask mSyncSDTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppContainer appContainer = ((KeyLockerApp)getApplication()).mAppContainer;
        this.mSyncSDTask = new SyncSDTask(this, appContainer.executorService, appContainer.mainThreadHandler);

        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.recycler_view);
        NavigationView navigationView = findViewById(R.id.nav_view);
        FloatingActionButton fabView = findViewById(R.id.fab);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        mSettings.registerPreferenceChangeListener(this);

        mNewKeyResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onEditKeyResult);

        mEditKeyResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onEditKeyResult);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        MenuItem mi = navigationView.getMenu().findItem(R.id.nav_keys);
        if (mi != null) mi.setChecked(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        swapAdapter(SHOW_KEYNOTE.KEY);

        fabView.setOnClickListener(this::onClickFab);

        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
            mQuery = intent.getStringExtra(SearchManager.QUERY);
    }

    private void onEditKeyResult(ActivityResult result) {
//        this.requireAuthentication = false;
    }

    public void onClickFab(View view) {
        Intent intent = new Intent(MainActivity.this,
                mType == SHOW_KEYNOTE.KEY ?  KeyDetailActivity.class : NoteDetailActivity.class);
        mNewKeyResult.launch(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
//        if (key.equals(getString(R.string.settings_key_relock_background)))
//            KeyDb.setRelockOnBackground(mSettings.getRelockOnBackground());

//		if (key.equals(getString(R.string.settings_key_label_size)) ||
//				key.equals(getString(R.string.settings_key_label_display)) ||
//				key.equals(getString(R.string.settings_key_split_group_size)) ||
//				key.equals(getString(R.string.settings_key_thumbnail_size))) {
//			adapter.notifyDataSetChanged();
//		} else if (key.equals(getString(R.string.settings_key_search_includes))) {
//			adapter.clearFilter();
//		} else if (key.equals(getString(R.string.settings_key_tap_single)) ||
//				key.equals(getString(R.string.settings_key_tap_double)) ||
//				key.equals(getString(R.string.settings_key_theme)) ||
//				key.equals(getString(R.string.settings_key_lang)) ||
//				key.equals(getString(R.string.settings_key_enable_screenshot)) ||
//				key.equals(getString(R.string.settings_key_tag_functionality)) ||
//				key.equals(getString(R.string.settings_key_label_highlight_token)) ||
//				key.equals(getString(R.string.settings_key_card_layout)) ||
//				key.equals(getString(R.string.settings_key_theme_mode)) ||
//				key.equals(getString(R.string.settings_key_theme_black_auto)) ||
//				key.equals(getString(R.string.settings_key_hide_global_timeout)) ||
//				key.equals(getString(R.string.settings_key_hide_issuer)) ||
//				key.equals(getString(R.string.settings_key_show_prev_token))) {
//			recreateActivity = true;
//		}
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
            mAdapter.getFilter().filter(intent.getStringExtra(SearchManager.QUERY));
    }

    @Override
    public void loadData() {
        if (mViewModel.keyDb != null)
            mAdapter.loadEntries(mViewModel.keyDb);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!TextUtils.isEmpty(mQuery))
            mAdapter.getFilter().filter(mQuery);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

    void swapAdapter(SHOW_KEYNOTE type) {
        mType = type;
        if (mType == SHOW_KEYNOTE.KEY) {
            mAdapter = new KeyCardViewAdapter(this);
            mAdapter.setEditCallback(keyID -> {
                Intent intent = new Intent(MainActivity.this, KeyDetailActivity.class);
                intent.putExtra("key-id", keyID);
                mEditKeyResult.launch(intent);
            });
            mAdapter.setKeyNoteRemovedCallback(keyID -> {
                 Snackbar.make(mRecyclerView, R.string.key_was_removed, BaseTransientBottomBar.LENGTH_LONG)
                         .setAction(R.string.undo_remove, view -> {
                             mViewModel.keyDb.undoDeleteKey(keyID);
                             mAdapter.loadEntries(mViewModel.keyDb);
                         })
                         .show();
            });
        } else {
            mAdapter = new NoteCardViewAdapter(this);
            mAdapter.setEditCallback(noteID -> {
                Intent intent = new Intent(MainActivity.this, NoteDetailActivity.class);
                intent.putExtra("note-id", noteID);
                mEditKeyResult.launch(intent);
            });
            mAdapter.setKeyNoteRemovedCallback(noteID -> {
                Snackbar.make(mRecyclerView, R.string.key_was_removed, BaseTransientBottomBar.LENGTH_LONG)
                        .setAction(R.string.undo_remove, view -> {
                            mViewModel.keyDb.undoDeleteNote(noteID);
                            mAdapter.loadEntries(mViewModel.keyDb);
                        })
                        .show();
            });
        }

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_keys) {
            if (mType != SHOW_KEYNOTE.KEY) {
                swapAdapter(SHOW_KEYNOTE.KEY);
                mAdapter.loadEntries(mViewModel.keyDb);
            }
        } else if (id == R.id.nav_notes) {
            if (mType != SHOW_KEYNOTE.NOTE) {
                swapAdapter(SHOW_KEYNOTE.NOTE);
                mAdapter.loadEntries(mViewModel.keyDb);
            }
        } else if (id == R.id.nav_sync_sdcard) {
            syncWithSDCard();
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
            mAdapter.loadEntries(mViewModel.keyDb);
        } else {
            Exception e = ((TaskResult.Error<Void>)result).exception;

            if (e instanceof InvalidPasswordException) {
                final View view = getLayoutInflater().inflate(R.layout.dialog_ask_password, null);

                final EditText pw = view.findViewById(R.id.dlog_password);
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


    private enum SHOW_KEYNOTE {KEY, NOTE}

}
