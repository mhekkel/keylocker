package com.hekkelman.keylocker.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.Utilities.Synchronize;
import com.hekkelman.keylocker.View.KeyCardViewAdapter;
import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDb;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener,
        ActivityResultCallback<ActivityResult> {

    private class ProcessLifecycleObserver implements DefaultLifecycleObserver {
        @Override
        public void onStop(@NonNull LifecycleOwner owner) {
            if (MainActivity.this.mSettings.getRelockOnBackground())
                MainActivity.this.mRequireAuthentication = true;
        }
    }

    private boolean mRequireAuthentication = false;
    private KeyCardViewAdapter mAdapter;
    private String mQuery;
    private RecyclerView mRecyclerView;
    private NavigationView mNavigationView;
    private CountDownTimer mCountDownTimer;
    private ActivityResultLauncher<Intent> mUnlockResult;
    private ActivityResultLauncher<Intent> mInitResult;
    private AsyncTask<List<String>, Void, Void> mDeleteTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

//		ButterKnife.bind(this);

        if (mRecyclerView == null)
            mRecyclerView = findViewById(R.id.recycler_view);

        if (mNavigationView == null)
            mNavigationView = findViewById(R.id.nav_view);

        if (!mSettings.getScreenshotsEnabled())
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        mSettings.registerPreferenceChangeListener(this);

        mUnlockResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this);
        mInitResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this);

        if (savedInstanceState == null)
            mRequireAuthentication = true;

        setBroadcastCallback(() -> {
            if (mSettings.getRelockOnScreenOff())
                mRequireAuthentication = true;
        });

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new ProcessLifecycleObserver());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);
        MenuItem mi = mNavigationView.getMenu().findItem(R.id.nav_keys);
        if (mi != null) mi.setChecked(true);

        mAdapter = new KeyCardViewAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
//				hideProgressBar();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
//				hideProgressBar();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
//				hideProgressBar();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
//				hideProgressBar();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
//				hideProgressBar();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
//				hideProgressBar();
            }
        });

        if (savedInstanceState != null) {
            byte[] encKey = savedInstanceState.getByteArray("encKey");
            if (encKey != null) {
//				mAdapter.setEncryptionKey(EncryptionHelper.generateSymmetricKey(encKey));
                mRequireAuthentication = false;
            }
        }


//		mRecyclerView.addOnItemTouchListener(
//			new SwipeOutTouchListener(mRecyclerView,
//				new SwipeOutTouchListener.SwipeOutListener() {
//					@Override
//					public boolean canSwipe(int position) {
//						return mDeleteTask == null;     // only one at a time
//					}
//
//					@Override
//					public void onSwipeOutLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
//						for (int position : reverseSortedPositions) {
//							mAdapter.notifyItemRemoved(position);
//						}
//						mAdapter.notifyDataSetChanged();
//
//						removeKeys(reverseSortedPositions);
//					}
//
//					@Override
//					public void onSwipeOutRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
//						for (int position : reverseSortedPositions) {
//							mAdapter.notifyItemRemoved(position);
//						}
//						mAdapter.notifyDataSetChanged();
//
//						removeKeys(reverseSortedPositions);
//					}
//				}));

        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchKeys(query);
        } else if (intent.getBooleanExtra("unlocked", false)) {
            // we've just been unlocked. Check to see if there's a key left in the temp storage

            Key key = KeyDb.getCachedKey();
            if (key != null) {
                intent = new Intent(MainActivity.this, KeyDetailActivity.class);
                intent.putExtra("restore-key", true);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent intent = result.getData();
            if (intent != null) {
                File keyFile = new File(getFilesDir(), KeyDb.KEY_DB_NAME);

                if (intent.hasExtra(UnlockActivity.EXTRA_AUTH_DB_RESET))
                {
                    if (keyFile.exists())
                        keyFile.delete();
                    Intent authIntent = new Intent(this, InitActivity.class);
                    mInitResult.launch(authIntent);
                    return;
                }

                if (intent.hasExtra(UnlockActivity.EXTRA_AUTH_PASSWORD_KEY)) {
                    char[] password = intent.getCharArrayExtra(UnlockActivity.EXTRA_AUTH_PASSWORD_KEY);

                    mAdapter.setPassword(password, keyFile);
                    mRequireAuthentication = false;
                    return;
                }

                authenticate();
            }
        } else {
            authenticate();
        }
    }

    //	@OnClick(R.id.fab)
    public void onClickFab(View view) {
        Intent intent = new Intent(MainActivity.this, KeyDetailActivity.class);
        startActivity(intent);
    }

    private void searchKeys(String query) {
//		mKeys = mKeyDb.getKeys();
//
//		mQuery = query;
//
//		if (TextUtils.isEmpty(query) == false) {
//			Iterator<Key> iter = mKeys.iterator();
//			while (iter.hasNext()) {
//				Key key = iter.next();
//				if (key.match(query) == false)
//					iter.remove();
//			}
//		}
//
//		mAdapter.notifyDataSetChanged();
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

    private static class DeleteKeysTask extends AsyncTask<List<String>, Void, Void> {

        private final WeakReference<MainActivity> mainActivityWeakReference;

        private DeleteKeysTask(MainActivity mainActivity) {
            this.mainActivityWeakReference = new WeakReference<>(mainActivity);
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(List<String>... params) {
//			try {
//				MainActivity mainActivity = mainActivityWeakReference.get();
//				if (mainActivity != null)
//				{
//					KeyDb keyDb = mainActivity.mKeyDb;
//					for (String keyId : params[0])
//						keyDb.deleteKey(keyId);
//				}
//			} catch (KeyDbException ignored) {
//			}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            MainActivity mainActivity = mainActivityWeakReference.get();
            if (mainActivity != null)
                mainActivity.mDeleteTask = null;
        }
    }

    private void removeKeys(int[] position) {
//		List<String> ids = new ArrayList<String>();
//
//		for (int pos : position) {
//			Key key = mKeys.remove(pos);
//			ids.add(key.getId());
//		}
//
//		mDeleteTask = new DeleteKeysTask(this);
//		mDeleteTask.execute(ids);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchKeys(query);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mRequireAuthentication) {
            authenticate();
        } else {
            populateAdapter();
//				}
//				checkIntent();
////			}

            if (setCountDownTimerNow())
                mCountDownTimer.start();
        }
    }

    public void authenticate() {
        File keyFile = new File(getFilesDir(), KeyDb.KEY_DB_NAME);
        if (!keyFile.exists()) {
            Intent authIntent = new Intent(this, InitActivity.class);
            mInitResult.launch(authIntent);
        } else {
            Intent authIntent = new Intent(this, UnlockActivity.class);
            mUnlockResult.launch(authIntent);
        }
    }

    private void checkIntent() {
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

    private void populateAdapter() {
        mAdapter.loadEntries();
//		tagsDrawerAdapter.setTags(TagsAdapter.createTagsMap(adapter.getEntries(), settings));
//		adapter.filterByTags(tagsDrawerAdapter.getActiveTags());
    }


    //	@Override
//	protected void onStart() {
//		super.onStart();
//
//		mKeyDb = KeyDb.getInstance();
//
//		if (mKeyDb == null) {
//			startActivity(new Intent(this, UnlockActivity.class));
//			finish();
//		} else {
//			searchKeys(mQuery);
//
//			KeyDb.reference();
//		}
//	}

    @Override
    protected void onStop() {
        super.onStop();

        if (mDeleteTask != null) {
            try {
                mDeleteTask.wait();
                KeyDb.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchKeys(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        boolean result = true;

        switch (item.getItemId()) {
//            case android.R.id.home:
//                result = false;
//                break;
//            case R.id.action_settings:
//                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
//                startActivity(intent);
//                result = true;
//                break;
//            case R.id.action_synchronize:
//                syncWithSDCard(false);
//                break;
            case R.id.action_undelete:
//				try {
//					mKeys = mKeyDb.undeleteAll().getKeys();
//					mAdapter.notifyDataSetChanged();
//				} catch (KeyDbException e) {
////                    e.printStackTrace();
//				}
                break;
            default:
                result = false;
                break;
        }

        return result || super.onOptionsItemSelected(item);
    }

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
                    mAdapter.notifyDataSetChanged();
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
        int secondsToBlackout = 1000 * mSettings.getAuthInactivityDelay();

        if (!mSettings.getAuthInactivity() || secondsToBlackout == 0)
            return false;

        mCountDownTimer = new CountDownTimer(secondsToBlackout, 1000) {
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
