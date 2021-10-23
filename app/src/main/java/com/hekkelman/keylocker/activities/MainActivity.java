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
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.tasks.SyncSDTask;
import com.hekkelman.keylocker.view.KeyCardViewAdapter;
import com.hekkelman.keylocker.view.KeyNoteCardViewAdapter;
import com.hekkelman.keylocker.view.NoteCardViewAdapter;

import java.io.File;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends BackgroundTaskActivity<SyncSDTask.Result>
        implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private KeyNoteCardViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private String query;
    private CountDownTimer countDownTimer;
    private ActivityResultLauncher<Intent> unlockResult;
    private ActivityResultLauncher<Intent> initResult;
    private ActivityResultLauncher<Intent> newKeyResult;
    private ActivityResultLauncher<Intent> editKeyResult;
    private SHOW_KEYNOTE mType = SHOW_KEYNOTE.KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.recycler_view);
        NavigationView navigationView = findViewById(R.id.nav_view);
        FloatingActionButton fabView = findViewById(R.id.fab);

        if (!settings.getScreenshotsEnabled())
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        settings.registerPreferenceChangeListener(this);

        KeyDb.init(settings);

        unlockResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onUnlockedResult);

        initResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onUnlockedResult);

        newKeyResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onEditKeyResult);

        editKeyResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onEditKeyResult);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        MenuItem mi = navigationView.getMenu().findItem(R.id.nav_keys);
        if (mi != null) mi.setChecked(true);

        if (mType == SHOW_KEYNOTE.KEY) {
            mAdapter = new KeyCardViewAdapter(this);
            ((KeyCardViewAdapter) mAdapter).setCallback(keyID -> {
                Intent intent = new Intent(MainActivity.this, KeyDetailActivity.class);
                intent.putExtra("key-id", keyID);
                editKeyResult.launch(intent);
            });
        } else {
            mAdapter = new NoteCardViewAdapter(this);
            ((NoteCardViewAdapter) mAdapter).setCallback(keyID -> {
//            Intent intent = new Intent(MainActivity.this, KeyDetailActivity.class);
//            intent.putExtra("key-id", keyID);
//            editKeyResult.launch(intent);
            });
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        fabView.setOnClickListener(this::onClickFab);

        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
            query = intent.getStringExtra(SearchManager.QUERY);
    }

    private void onEditKeyResult(ActivityResult result) {
//        this.requireAuthentication = false;
    }

    public void onUnlockedResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_CANCELED)
            finish();
    }

    public void onClickFab(View view) {
        Intent intent = new Intent(MainActivity.this, KeyDetailActivity.class);
        newKeyResult.launch(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
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
    public void onResume() {
        super.onResume();

        if (!KeyDb.isUnlocked()) {
            authenticate();
        } else {
            mAdapter.loadEntries();

            if (setCountDownTimerNow())
                countDownTimer.start();
        }
    }

    public void authenticate() {
        File keyFile = new File(getFilesDir(), KeyDb.KEY_DB_NAME);
        if (!keyFile.exists()) {
            Intent authIntent = new Intent(this, InitActivity.class);
            initResult.launch(authIntent);
        } else {
            Intent authIntent = new Intent(this, UnlockActivity.class);
            unlockResult.launch(authIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!TextUtils.isEmpty(query))
            mAdapter.getFilter().filter(query);
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
            ((KeyCardViewAdapter) mAdapter).setCallback(keyID -> {
                Intent intent = new Intent(MainActivity.this, KeyDetailActivity.class);
                intent.putExtra("key-id", keyID);
                editKeyResult.launch(intent);
            });
        } else {
            mAdapter = new NoteCardViewAdapter(this);
            ((NoteCardViewAdapter) mAdapter).setCallback(keyID -> {
//            Intent intent = new Intent(MainActivity.this, KeyDetailActivity.class);
//            intent.putExtra("key-id", keyID);
//            editKeyResult.launch(intent);
            });
        }

        mAdapter.loadEntries();
        mRecyclerView.swapAdapter(mAdapter, true);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_keys) {
            swapAdapter(SHOW_KEYNOTE.KEY);
        } else if (id == R.id.nav_notes) {
            swapAdapter(SHOW_KEYNOTE.NOTE);
        } else if (id == R.id.nav_sync_sdcard) {
            syncWithSDCard();
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void syncWithSDCard() {
        String backupDir = settings.getLocalBackupDir();

        if (TextUtils.isEmpty(backupDir)) {
            Toast.makeText(this, R.string.backup_toast_no_location, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri backupDirUri = Uri.parse(backupDir);

            SyncSDTask syncSDTask = new SyncSDTask(this, backupDirUri, null);
            startBackgroundTask(syncSDTask);
        } catch (Exception e) {
            syncFailed(e.getMessage());
        }
    }

    @Override
    void onTaskResult(SyncSDTask.Result result) {
        if (result.synced) {
            Toast.makeText(this, R.string.sync_successful, Toast.LENGTH_SHORT).show();
            mAdapter.loadEntries();
        } else if (result.needPassword) {
            final View view = getLayoutInflater().inflate(R.layout.dialog_ask_password, null);
            new AlertDialog.Builder(MainActivity.this)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        EditText pw = view.findViewById(R.id.dlog_password);

                        String backupDir = settings.getLocalBackupDir();
                        Uri backupDirUri = Uri.parse(backupDir);

                        SyncSDTask syncSDTask = new SyncSDTask(MainActivity.this, backupDirUri, pw.getText().toString());
                        startBackgroundTask(syncSDTask);
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    })
                    .show();
        } else {
            syncFailed(result.errorMessage);
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

    private boolean setCountDownTimerNow() {
        int secondsToBlackout = 1000 * settings.getAuthInactivityDelay();

        if (!settings.getAuthInactivity() || secondsToBlackout == 0)
            return false;

        countDownTimer = new CountDownTimer(secondsToBlackout, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                authenticate();
                this.cancel();
            }
        };

        return true;
    }


    private enum SHOW_KEYNOTE {KEY, NOTE}


}
