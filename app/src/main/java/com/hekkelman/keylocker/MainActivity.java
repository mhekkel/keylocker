package com.hekkelman.keylocker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDb;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ListView mListView;
    private KeyDb mKeyDb;

    protected ListView getListView() {
        if (mListView == null) {
            mListView = (ListView) findViewById(android.R.id.list);
        }
        return mListView;
    }

    protected void setListAdapter(ListAdapter adapter) {
        getListView().setAdapter(adapter);
    }

    public class KeyAdapter extends BaseAdapter {

        private List<Key> mKeys;

        public KeyAdapter() {
            this.mKeys = mKeyDb.getKeys();
        }

        @Override
        public int getCount() {
            return mKeys.size();
        }

        @Override
        public Object getItem(int position) {
            return mKeys.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listitem, parent, false);
            }

            Key key = mKeys.get(position);

            TextView caption = (TextView) convertView.findViewById(R.id.itemCaption);
            caption.setText(key.getName());

            TextView user = (TextView) convertView.findViewById(R.id.itemUser);
            user.setText(key.getUser());

            return convertView;
        }

        protected ListAdapter getListAdapter() {
            ListAdapter adapter = getListView().getAdapter();
            if (adapter instanceof HeaderViewListAdapter) {
                return ((HeaderViewListAdapter) adapter).getWrappedAdapter();
            } else {
                return adapter;
            }
        }

        @Override
        public void notifyDataSetChanged() {
            mKeys = mKeyDb.getKeys();
            super.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mKeyDb = KeyDb.getInstance();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, KeyDetailActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setListAdapter(new KeyAdapter());

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Key key = mKeyDb.getKeys().get(position);

                Intent intent = new Intent(MainActivity.this, KeyDetailActivity.class);
                intent.putExtra("keyId", key.getId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {

        KeyAdapter adapter = (KeyAdapter)mListView.getAdapter();
        adapter.notifyDataSetChanged();

        super.onResume();
    }

    //    @Override
//    protected void onPause() {
//        KeyDb.setInstance(null);
//
//        super.onPause();
//    }
//
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_keys) {
            // Handle the camera action
        } else if (id == R.id.nav_notes) {

        } else if (id == R.id.nav_sync) {
            syncWithSDCard();
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void syncWithSDCard() {
        if (isExternalStorageWritable()) {
            try {

                File dir = new File(Environment.getExternalStorageDirectory(), "KeyLocker");
                if (dir.isDirectory() == false && dir.mkdir() == false)
                    throw new Exception("could not create directory on SDCard");

                File file = new File(dir, KeyDb.KEY_DB_NAME);

                KeyDb.getInstance().synchronize(file);

                KeyAdapter adapter = (KeyAdapter)mListView.getAdapter();
                adapter.notifyDataSetChanged();

                Toast.makeText(this, "Sync successful", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                new AlertDialog.Builder(this)
                        .setTitle("Synchronization Failed")
                        .setMessage("Somehow, KeyLocker failed to synchronize, the error is: " + e.getMessage())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        }
    }
}
