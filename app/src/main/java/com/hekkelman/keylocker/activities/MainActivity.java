package com.hekkelman.keylocker.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.databinding.ActivitySingleBinding;
import com.hekkelman.keylocker.datamodel.KeyDbFactory;
import com.hekkelman.keylocker.datamodel.KeyDbModel;
import com.hekkelman.keylocker.dialogs.UnlockDialog;
import com.hekkelman.keylocker.tasks.TaskResult;
import com.hekkelman.keylocker.tasks.UnlockTask;
import com.hekkelman.keylocker.utilities.DrawerLocker;
import com.hekkelman.keylocker.utilities.Settings;

public class MainActivity extends AppCompatActivity
        implements DrawerLocker, UnlockDialog.UnlockDialogListener {

    private Settings settings;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawer;
    private ActivityResultLauncher<Intent> unlockResult;
    private ActivityResultLauncher<Intent> initResult;
    private KeyDbModel keyDbModel;

    private UnlockTask unlockTask;
    private ScreenOffReceiver screenOffReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new Settings(this);

        keyDbModel = new ViewModelProvider(this).get(KeyDbModel.class);

        if (settings.getRelockOnBackground()) {
            screenOffReceiver = new ScreenOffReceiver(keyDbModel);
            registerReceiver(screenOffReceiver, screenOffReceiver.filter);
        }

        unlockResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onUnlockedResult);

        initResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onUnlockedResult);

        ActivitySingleBinding binding = ActivitySingleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        NavigationView navigationView = binding.navView;
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_main);
        NavController navController = navHostFragment.getNavController();
        drawer = binding.drawerLayout;

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration
                .Builder(navController.getGraph())
                .setOpenableLayout(drawer)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(item -> {
            return false;
        });

//        navigationView.setNavigationItemSelectedListener(this);
        MenuItem mi = navigationView.getMenu().findItem(R.id.nav_keys);
        if (mi != null) mi.setChecked(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!KeyDbFactory.exists(this)) {
            Intent initIntent = new Intent(this, InitActivity.class);
            initResult.launch(initIntent);
        } else if (keyDbModel.locked()) {
            new UnlockDialog().show(getSupportFragmentManager(), UnlockDialog.TAG);
//            Intent authIntent = new Intent(this, UnlockActivity.class);
//            initResult.launch(authIntent);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_main);

        boolean result = navController.navigateUp();
        if (!result) {
            result = super.onSupportNavigateUp();
        }

        if (!result && drawer.getDrawerLockMode(GravityCompat.START) == DrawerLayout.LOCK_MODE_UNLOCKED) {
            drawer.openDrawer(GravityCompat.START);
            result = true;
        }

        return result;
    }

    public void onUnlockedResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_CANCELED)
            finish();
        else {
            Intent data = result.getData();
            if (data.hasExtra(UnlockActivity.EXTRA_AUTH_PASSWORD_KEY)) {
                char[] password = data.getCharArrayExtra(UnlockActivity.EXTRA_AUTH_PASSWORD_KEY);
                keyDbModel.unlock(password);
            }
        }
    }

    @Override
    public void setDrawerLockerEnabled(boolean enabled) {
        int lockMode = enabled ? DrawerLayout.LOCK_MODE_UNLOCKED :
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        drawer.setDrawerLockMode(lockMode);
    }

    @Override
    public void onUnlockDone(TaskResult<Void> result) {
        if (result instanceof TaskResult.Error)
            finish();
    }

    public static class ScreenOffReceiver extends BroadcastReceiver {
        private final KeyDbModel keyDbModel;
        public IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);

        public ScreenOffReceiver(KeyDbModel keyDbModel) {
            super();

            this.keyDbModel = keyDbModel;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                keyDbModel.lock();
            }
        }
    }

}


//package com.hekkelman.keylocker.activities;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.SearchManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.SharedPreferences;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.CountDownTimer;
//import android.text.TextUtils;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.EditText;
//
//import androidx.activity.result.ActivityResult;
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.appcompat.app.ActionBarDrawerToggle;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.SearchView;
//import androidx.appcompat.widget.Toolbar;
//import androidx.core.view.GravityCompat;
//import androidx.drawerlayout.widget.DrawerLayout;
//import androidx.preference.PreferenceManager;
//import androidx.recyclerview.widget.DefaultItemAnimator;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.android.material.navigation.NavigationView;
//import com.google.android.material.snackbar.BaseTransientBottomBar;
//import com.google.android.material.snackbar.Snackbar;
//import com.hekkelman.keylocker.KeyLockerApp;
//import com.hekkelman.keylocker.R;
//import com.hekkelman.keylocker.datamodel.InvalidPasswordException;
//import com.hekkelman.keylocker.datamodel.KeyDb;
//import com.hekkelman.keylocker.tasks.SaveNoteTask;
//import com.hekkelman.keylocker.tasks.SyncSDTask;
//import com.hekkelman.keylocker.tasks.TaskResult;
//import com.hekkelman.keylocker.utilities.AppContainer;
//import com.hekkelman.keylocker.utilities.Settings;
//import com.hekkelman.keylocker.view.KeyCardViewAdapter;
//import com.hekkelman.keylocker.view.KeyNoteCardViewAdapter;
//import com.hekkelman.keylocker.view.NoteCardViewAdapter;
//
//import java.io.File;
//
//public class MainActivity extends AppCompatActivity
//        implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {
//
//    public Settings settings;
//    private ScreenOffReceiver screenOffReceiver;
//    private KeyNoteCardViewAdapter mAdapter;
//    private RecyclerView mRecyclerView;
//    private String query;
//    private CountDownTimer countDownTimer;
//    private ActivityResultLauncher<Intent> unlockResult;
//    private ActivityResultLauncher<Intent> initResult;
//    private ActivityResultLauncher<Intent> newKeyResult;
//    private ActivityResultLauncher<Intent> editKeyResult;
//    private SHOW_KEYNOTE mType = SHOW_KEYNOTE.KEY;
//    private SyncSDTask syncSDTask;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        settings = new Settings(this);
//
//        super.onCreate(savedInstanceState);
//
//        AppContainer appContainer = ((KeyLockerApp) getApplication()).appContainer;
//        this.syncSDTask = new SyncSDTask(this, appContainer.getExecutorService(), appContainer.getMainThreadHandler());
//
//        if (settings.getRelockOnBackground()) {
//            screenOffReceiver = new ScreenOffReceiver();
//            registerReceiver(screenOffReceiver, screenOffReceiver.filter);
//        }
//
//        setContentView(R.layout.activity_main);
//        mRecyclerView = findViewById(R.id.recycler_view);
//        NavigationView navigationView = findViewById(R.id.nav_view);
//        FloatingActionButton fabView = findViewById(R.id.fab);
//
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
//                WindowManager.LayoutParams.FLAG_SECURE);
//
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
//        settings.registerPreferenceChangeListener(this);
//
//        KeyDb.init(settings);
//
//        unlockResult = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(), this::onUnlockedResult);
//
//        initResult = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(), this::onUnlockedResult);
//
//        newKeyResult = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(), this::onEditKeyResult);
//
//        editKeyResult = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(), this::onEditKeyResult);
//
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
//
//        navigationView.setNavigationItemSelectedListener(this);
//        MenuItem mi = navigationView.getMenu().findItem(R.id.nav_keys);
//        if (mi != null) mi.setChecked(true);
//
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//
//        swapAdapter(SHOW_KEYNOTE.KEY);
//
//        fabView.setOnClickListener(this::onClickFab);
//
//        Intent intent = getIntent();
//
//        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
//            query = intent.getStringExtra(SearchManager.QUERY);
//    }
//
//    private void onEditKeyResult(ActivityResult result) {
////        this.requireAuthentication = false;
//    }
//
//    public void onUnlockedResult(ActivityResult result) {
//        if (result.getResultCode() == Activity.RESULT_CANCELED)
//            finish();
//    }
//
//    public void onClickFab(View view) {
//        Intent intent = new Intent(MainActivity.this,
//                mType == SHOW_KEYNOTE.KEY ?  KeyDetailActivity.class : NoteDetailActivity.class);
//        newKeyResult.launch(intent);
//    }
//
//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
//        if (key.equals(getString(R.string.settings_key_relock_background)))
//            KeyDb.setRelockOnBackground(settings.getRelockOnBackground());
//
////		if (key.equals(getString(R.string.settings_key_label_size)) ||
////				key.equals(getString(R.string.settings_key_label_display)) ||
////				key.equals(getString(R.string.settings_key_split_group_size)) ||
////				key.equals(getString(R.string.settings_key_thumbnail_size))) {
////			adapter.notifyDataSetChanged();
////		} else if (key.equals(getString(R.string.settings_key_search_includes))) {
////			adapter.clearFilter();
////		} else if (key.equals(getString(R.string.settings_key_tap_single)) ||
////				key.equals(getString(R.string.settings_key_tap_double)) ||
////				key.equals(getString(R.string.settings_key_theme)) ||
////				key.equals(getString(R.string.settings_key_lang)) ||
////				key.equals(getString(R.string.settings_key_enable_screenshot)) ||
////				key.equals(getString(R.string.settings_key_tag_functionality)) ||
////				key.equals(getString(R.string.settings_key_label_highlight_token)) ||
////				key.equals(getString(R.string.settings_key_card_layout)) ||
////				key.equals(getString(R.string.settings_key_theme_mode)) ||
////				key.equals(getString(R.string.settings_key_theme_black_auto)) ||
////				key.equals(getString(R.string.settings_key_hide_global_timeout)) ||
////				key.equals(getString(R.string.settings_key_hide_issuer)) ||
////				key.equals(getString(R.string.settings_key_show_prev_token))) {
////			recreateActivity = true;
////		}
//    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//
//        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
//            mAdapter.getFilter().filter(intent.getStringExtra(SearchManager.QUERY));
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        if (!KeyDb.isUnlocked()) {
//            authenticate();
//        } else {
//            mAdapter.loadEntries();
//
//            if (setCountDownTimerNow())
//                countDownTimer.start();
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        if (countDownTimer != null)
//            countDownTimer.cancel();
//
//        super.onPause();
//    }
//
//    public void authenticate() {
//        File keyFile = new File(getFilesDir(), KeyDb.KEY_DB_NAME);
//        if (!keyFile.exists()) {
//            Intent authIntent = new Intent(this, InitActivity.class);
//            initResult.launch(authIntent);
//        } else {
//            Intent authIntent = new Intent(this, UnlockActivity.class);
//            unlockResult.launch(authIntent);
//        }
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (!TextUtils.isEmpty(query))
//            mAdapter.getFilter().filter(query);
//    }
//
//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        if (screenOffReceiver != null)
//            unregisterReceiver(screenOffReceiver);
//        super.onDestroy();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.mainmenu, menu);
//
//        // Get the SearchView and set the searchable configuration
////        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
////        // Assumes current activity is the searchable activity
////        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
////        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                mAdapter.getFilter().filter(newText);
//                return false;
//            }
//        });
//
//        return true;
//    }
//
//    void swapAdapter(SHOW_KEYNOTE type) {
//        mType = type;
//        if (mType == SHOW_KEYNOTE.KEY) {
//            mAdapter = new KeyCardViewAdapter(this);
//            mAdapter.setEditCallback(keyID -> {
//                Intent intent = new Intent(MainActivity.this, KeyDetailActivity.class);
//                intent.putExtra("key-id", keyID);
//                editKeyResult.launch(intent);
//            });
//            mAdapter.setKeyNoteRemovedCallback(keyID -> {
//                 Snackbar.make(mRecyclerView, R.string.key_was_removed, BaseTransientBottomBar.LENGTH_LONG)
//                         .setAction(R.string.undo_remove, view -> {
//                             KeyDb.undoDeleteKey(keyID);
//                             mAdapter.loadEntries();
//                         })
//                         .show();
//            });
//        } else {
//            mAdapter = new NoteCardViewAdapter(this);
//            mAdapter.setEditCallback(noteID -> {
//                Intent intent = new Intent(MainActivity.this, NoteDetailActivity.class);
//                intent.putExtra("note-id", noteID);
//                editKeyResult.launch(intent);
//            });
//            mAdapter.setKeyNoteRemovedCallback(noteID -> {
//                Snackbar.make(mRecyclerView, R.string.key_was_removed, BaseTransientBottomBar.LENGTH_LONG)
//                        .setAction(R.string.undo_remove, view -> {
//                            KeyDb.undoDeleteNote(noteID);
//                            mAdapter.loadEntries();
//                        })
//                        .show();
//            });
//        }
//
//        mRecyclerView.setAdapter(mAdapter);
//    }
//
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
//        int id = item.getItemId();
//
//        if (id == R.id.nav_keys) {
//            if (mType != SHOW_KEYNOTE.KEY) {
//                swapAdapter(SHOW_KEYNOTE.KEY);
//                mAdapter.loadEntries();
//            }
//        } else if (id == R.id.nav_notes) {
//            if (mType != SHOW_KEYNOTE.NOTE) {
//                swapAdapter(SHOW_KEYNOTE.NOTE);
//                mAdapter.loadEntries();
//            }
//        } else if (id == R.id.nav_sync_sdcard) {
//            syncWithSDCard();
//        } else if (id == R.id.action_settings) {
//            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
//            startActivity(intent);
//        }
//
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }
//
//    private void syncWithSDCard() {
//        String backupDir = settings.getLocalBackupDir();
//
//        if (TextUtils.isEmpty(backupDir)) {
//            Snackbar.make(mRecyclerView, R.string.backup_toast_no_location, BaseTransientBottomBar.LENGTH_SHORT).show();
//            return;
//        }
//
//        try {
//            Uri backupDirUri = Uri.parse(backupDir);
//            syncSDTask.syncToSD(this, backupDirUri, null, this::onTaskResult);
//        } catch (Exception e) {
//            syncFailed(e.getMessage());
//        }
//    }
//
//    void onTaskResult(TaskResult<Void> result) {
//        if (result instanceof TaskResult.Success) {
//            Snackbar.make(mRecyclerView, R.string.sync_successful, BaseTransientBottomBar.LENGTH_SHORT).show();
//            mAdapter.loadEntries();
//        } else {
//            Exception e = ((TaskResult.Error<Void>)result).exception;
//
//            if (e instanceof InvalidPasswordException) {
//                final View view = getLayoutInflater().inflate(R.layout.dialog_ask_password, null);
//
//                final EditText pw = view.findViewById(R.id.dlog_password);
//                if (settings.getBlockAccessibility())
//                    pw.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
//
//                if (settings.getBlockAutofill())
//                    pw.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
//
//                new AlertDialog.Builder(MainActivity.this)
//                        .setView(view)
//                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
//                            String backupDir = settings.getLocalBackupDir();
//
//                            Uri backupDirUri = Uri.parse(backupDir);
//                            syncSDTask.syncToSD(this, backupDirUri, null, this::onTaskResult);
//                        })
//                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
//                        })
//                        .show();
//
//            } else {
//                syncFailed(e.getMessage());
//            }
//        }
//    }
//
//    void syncFailed(String errorMessage) {
//        new AlertDialog.Builder(MainActivity.this)
//                .setTitle(R.string.sync_failed)
//                .setMessage(getString(R.string.sync_failed_msg) + errorMessage)
//                .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .show();
//    }
//
//    private boolean setCountDownTimerNow() {
//        try {
//            int secondsToBlackout = 1000 * settings.getAuthInactivityDelay();
//
//            if (!settings.getAuthInactivity() || secondsToBlackout == 0)
//                return false;
//
//            countDownTimer = new CountDownTimer(secondsToBlackout, 1000) {
//                @Override
//                public void onTick(long millisUntilFinished) {
//                }
//
//                @Override
//                public void onFinish() {
//                    authenticate();
//                    this.cancel();
//                }
//            };
//
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//
//    private enum SHOW_KEYNOTE {KEY, NOTE}
//
//    public static class ScreenOffReceiver extends BroadcastReceiver {
//        public IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
//                KeyDb.onReceivedScreenOff();
//        }
//    }
//
//}
