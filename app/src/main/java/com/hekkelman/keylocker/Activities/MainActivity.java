package com.hekkelman.keylocker.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.hekkelman.keylocker.BaseApplication;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.Utilities.Synchronize;
import com.hekkelman.keylocker.View.KeyCardViewAdapter;
import com.hekkelman.keylocker.datamodel.KeyDb;

import java.io.File;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private KeyCardViewAdapter adapter;
    private String query;
    private RecyclerView recyclerView;
    private NavigationView navigationView;
    private CountDownTimer countDownTimer;
    private FloatingActionButton fabView;

    private ActivityResultLauncher<Intent> unlockResult;
    private ActivityResultLauncher<Intent> initResult;
    private ActivityResultLauncher<Intent> newKeyResult;
    private ActivityResultLauncher<Intent> editKeyResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler_view);
        navigationView = findViewById(R.id.nav_view);
        fabView = findViewById(R.id.fab);

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

		adapter = new KeyCardViewAdapter(this);
		adapter.setCallback(new KeyCardViewAdapter.KeyCardViewCallback() {
            @Override
            public void onEditKey(String keyID) {
                Intent intent = new Intent(MainActivity.this, KeyDetailActivity.class);
                intent.putExtra("key-id", keyID);
                editKeyResult.launch(intent);
            }
        });
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setAdapter(adapter);

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
            adapter.getFilter().filter(intent.getStringExtra(SearchManager.QUERY));
    }

    @Override
    public void onResume() {
        super.onResume();

        if (! KeyDb.isUnlocked()) {
            authenticate();
        } else {
            adapter.loadEntries();

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

//	private void updateEncryption(byte[] newKey) {
//		SecretKey encryptionKey = null;
//
////		encryptionType = settings.getEncryption();
////
////		if (encryptionType == EncryptionType.KEYSTORE) {
////			encryptionKey = KeyStoreHelper.loadEncryptionKeyFromKeyStore(this, false);
////		} else if (encryptionType == EncryptionType.PASSWORD) {
//			if (newKey != null && newKey.length > 0) {
//				encryptionKey = EncryptionHelper.generateSymmetricKey(newKey);
//			} else {
//				authenticate(R.string.auth_msg_confirm_encryption);
//			}
////		}
//
//		if (encryptionKey != null)
//			mAdapter.setEncryptionKey(encryptionKey);
//
//		populateAdapter();
//	}

   	@Override
	protected void onStart() {
		super.onStart();
		if (! TextUtils.isEmpty(query))
            adapter.getFilter().filter(query);
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
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//
//        boolean result = true;
//
//        switch (item.getItemId()) {
////            case android.R.id.home:
////                result = false;
////                break;
////            case R.id.action_settings:
////                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
////                startActivity(intent);
////                result = true;
////                break;
////            case R.id.action_synchronize:
////                syncWithSDCard(false);
////                break;
//            case R.id.action_undelete:
////				try {
////					mKeys = mKeyDb.undeleteAll().getKeys();
////					mAdapter.notifyDataSetChanged();
////				} catch (KeyDbException e) {
//////                    e.printStackTrace();
////				}
//                break;
//            default:
//                result = false;
//                break;
//        }
//
//        return result || super.onOptionsItemSelected(item);
//    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_keys) {

        } else if (id == R.id.nav_notes) {

        } else if (id == R.id.nav_sync_sdcard) {
            syncWithSDCard();
        } else if (id == R.id.nav_sync_onedrive) {
            syncWithOneDrive();
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    final Synchronize.OnSyncTaskResult mSyncHandler = new Synchronize.OnSyncTaskResult() {
        @Override
        public void syncResult(Synchronize.SyncResult result, String message, final Synchronize.SyncTask task) {
            switch (result) {
                case SUCCESS:
                    adapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, R.string.sync_successful, Toast.LENGTH_LONG).show();
                    break;

                case FAILED:
                case CANCELLED:
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.sync_failed)
                            .setMessage(message != null ? message : getString(R.string.sync_cancelled))
                            .show();
                    break;

                case MKDIR_FAILED:
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.sync_failed)
                            .setMessage(message != null ? message : getString(R.string.sync_mkdir_exception))
                            .show();
                    break;

                case PERMISSION_DENIED:
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.sync_failed)
                            .setMessage(message != null ? message : getString(R.string.sync_permission_denied))
                            .show();
                    break;

                case MEDIA_NOT_MOUNTED:
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.sync_failed)
                            .setMessage(message != null ? message : getString(R.string.sync_media_not_mounted))
                            .show();
                    break;

                case NEED_PASSWORD:
                    final View view = getLayoutInflater().inflate(R.layout.dialog_ask_password, null);
                    new AlertDialog.Builder(MainActivity.this)
                            .setView(view)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EditText pw = (EditText) view.findViewById(R.id.dlog_password);
                                    task.retryWithPassword(pw.getText().toString());
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                    break;
            }
        }

        @Override
        public Activity getActivity() {
            return MainActivity.this;
        }
    };

    private void syncWithOneDrive() {
//		final BaseApplication app = (BaseApplication) getApplication();
//		final ICallback<Void> serviceCreated = new DefaultCallback<Void>(this) {
//			@Override
//			public void success(final Void result) {
//				final BaseApplication app = (BaseApplication) getApplication();
//				Synchronize.syncWithOneDrive(mSyncHandler, app);
//			}
//		};
//		try {
//			app.getOneDriveClient();
//		} catch (final UnsupportedOperationException ignored) {
//			app.createOneDriveClient(this, serviceCreated);
//		}
    }

    private void syncWithSDCard() {
        if (settings.getLocalBackupDir().isEmpty()) {
            Toast.makeText(this, R.string.backup_toast_no_location, Toast.LENGTH_LONG).show();
            return;
        }

//		final BaseApplication app = (BaseApplication) getApplication();
//		if (isExternalStorageWritable()) {
//			Synchronize.syncWithSDCard(mSyncHandler, app);
//		}
    }

//    protected void onPostExecute(final SyncResult result) {
//
//        String error = sSyncTask.getError();
//        sSyncTask = null;
//
//        switch (result) {
//            case SUCCESS:
//                break;
//
//            case FAILED:
//                new AlertDialog.Builder(MainActivity.this)
//                        .setTitle(R.string.sync_failed)
//                        .setMessage(mSyncTask.getError())
//                        .show();
//                break;
//
//            case NEED_PASSWORD:
//                syncWithSDCard(true);
//                break;
//        }
//    }
//
//    @Override
//    protected void onCancelled() {
//        sSyncTask = null;
//        Toast.makeText(MainActivity.this, R.string.sync_cancelled, Toast.LENGTH_LONG).show();
//    }

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


}
