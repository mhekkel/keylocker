package com.hekkelman.keylocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;
import com.onedrive.sdk.concurrency.ICallback;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
//        , ItemFragment.OnFragmentInteractionListener
{

    private KeyDb mKeyDb;
    private List<Key> mKeys;
    private KeyCardViewAdapter mAdapter;
    private String mQuery;

    private AsyncTask<List<String>,Void,Void> mDeleteTask;

    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.nav_view) NavigationView mNavigationView;

//    @Override
//    public void onFragmentInteraction(final DisplayItem item) {
//        getFragmentManager()
//                .beginTransaction()
//                .replace(R.id.fragment, ItemFragment.newInstance(item.getId()))
//                .addToBackStack(null)
//                .commit();
//    }


    // New CardView/RecycleView based interface
    class KeyCardViewAdapter extends RecyclerView.Adapter<KeyCardViewAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_key_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Key key = mKeys.get(position);
            holder.nameView.setText(key.getName());
            holder.userView.setText(key.getUser());
        }

        @Override
        public int getItemCount() {
            return mKeys.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @Bind(R.id.itemCaption) protected TextView nameView;
            @Bind(R.id.itemUser) protected TextView userView;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);

                itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                    Key key = mKeys.get(getAdapterPosition());

                    Intent intent = new Intent(MainActivity.this, KeyDetailActivity.class);
                    intent.putExtra("keyId", key.getId());
                    startActivity(intent);
                    }
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);
        MenuItem mi = mNavigationView.getMenu().findItem(R.id.nav_keys);
        if (mi != null) {
            mi.setChecked(true);
        }

        mAdapter = new KeyCardViewAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(
                new SwipeOutTouchListener(mRecyclerView,
                        new SwipeOutTouchListener.SwipeOutListener() {
                            @Override
                            public boolean canSwipe(int position)
                            {
                                return mDeleteTask == null;     // only one at a time
                            }

                            @Override
                            public void onSwipeOutLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    mAdapter.notifyItemRemoved(position);
                                }
                                mAdapter.notifyDataSetChanged();

                                removeKeys(reverseSortedPositions);
                            }

                            @Override
                            public void onSwipeOutRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    mAdapter.notifyItemRemoved(position);
                                }
                                mAdapter.notifyDataSetChanged();

                                removeKeys(reverseSortedPositions);
                            }
                        }));

        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchKeys(query);
        }
        else if (intent.getBooleanExtra("unlocked", false)) {
            // we've just been unlocked. Check to see if there's a key left in the temp storage

            Key key = KeyDb.getCachedKey();
            if (key != null) {
                intent = new Intent(MainActivity.this, KeyDetailActivity.class);
                intent.putExtra("restore-key", true);
                startActivity(intent);
            }
        }
    }

    @OnClick(R.id.fab)
    public void onClickFab(View view) {
        Intent intent = new Intent(MainActivity.this, KeyDetailActivity.class);
        startActivity(intent);
    }

    private void searchKeys(String query) {
        mKeys = mKeyDb.getKeys();

        mQuery = query;

        if (TextUtils.isEmpty(query) == false) {
            Iterator<Key> iter = mKeys.iterator();
            while (iter.hasNext()) {
                Key key = iter.next();
                if (key.match(query) == false)
                    iter.remove();
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    private void removeKeys(int position[]) {
        List<String> ids = new ArrayList<String>();

        for (int pos: position) {
            Key key = mKeys.remove(pos);
            ids.add(key.getId());
        }

        mDeleteTask = new AsyncTask<List<String>, Void, Void>() {
            @Override
            protected Void doInBackground(List<String>... params) {
                try {
                    for (String keyId: params[0])
                        mKeyDb.deleteKey(keyId);
                } catch (KeyDbException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mDeleteTask = null;
            }
        }.execute(ids);
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
    protected void onStart() {
        super.onStart();

        mKeyDb = KeyDb.getInstance();

        if (mKeyDb == null) {
            startActivity(new Intent(this, UnlockActivity.class));
            finish();
        } else {
            searchKeys(mQuery);

            KeyDb.reference();
        }
    }

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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
                try {
                    mKeys = mKeyDb.undeleteAll().getKeys();
                    mAdapter.notifyDataSetChanged();
                } catch (KeyDbException e) {
//                    e.printStackTrace();
                }
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
        }
        else if (id == R.id.nav_sync_onedrive) {
            syncWithOneDrive();
        }
        else if (id == R.id.action_settings) {
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
        final BaseApplication app = (BaseApplication)getApplication();
        final ICallback<Void> serviceCreated = new DefaultCallback<Void>(this) {
            @Override
            public void success(final Void result) {
                final BaseApplication app = (BaseApplication)getApplication();
                Synchronize.syncWithOneDrive(mSyncHandler, app);
            }
        };
        try {
            app.getOneDriveClient();
        } catch (final UnsupportedOperationException ignored) {
            app.createOneDriveClient(this, serviceCreated);
        }
    }

    private void syncWithSDCard() {
        final BaseApplication app = (BaseApplication)getApplication();
        if (isExternalStorageWritable()) {
            Synchronize.syncWithSDCard(mSyncHandler, app);
        }
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


}
