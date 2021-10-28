package com.hekkelman.keylocker.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.databinding.ActivitySingleBinding;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbModel;
import com.hekkelman.keylocker.utilities.DrawerLocker;

public class MainFActivity extends AppCompatActivity
        implements DrawerLocker {

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        KeyDbModel keyDbModel = new ViewModelProvider(this).get(KeyDbModel.class);
//        keyDbModel.getKeyDb().observe(this, this::onKeyDbChanged);

        ActivitySingleBinding binding = ActivitySingleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        NavigationView navigationView = binding.navView;
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_main);
        NavController navController = navHostFragment.getNavController();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_main);
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





//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        drawerToggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(drawerToggle);
//
//        drawerToggle.setDrawerIndicatorEnabled(true);
//        drawerToggle.syncState();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_main);

        boolean result = navController.navigateUp();
        if (! result) {
            result = super.onSupportNavigateUp();
        }

        if (!result && drawer.getDrawerLockMode(GravityCompat.START) == DrawerLayout.LOCK_MODE_UNLOCKED) {
            drawer.openDrawer(GravityCompat.START);
            result = true;
        }

        return result;
    }

    void onKeyDbChanged(@Nullable KeyDb keyDb) {

    }

//    @Override
//    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        drawerToggle.syncState();
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }

    @Override
    public void setDrawerLockerEnabled(boolean enabled) {
        int lockMode = enabled ? DrawerLayout.LOCK_MODE_UNLOCKED :
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        drawer.setDrawerLockMode(lockMode);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(!enabled);

//        drawerToggle.setDrawerIndicatorEnabled(enabled);
//        drawerToggle.syncState();

//        if (enabled) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//            drawerToggle.setDrawerIndicatorEnabled(true);
//            drawerToggle.syncState();
//        } else {
//            drawerToggle.setDrawerIndicatorEnabled(false);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }

//        drawerToggle.syncState();
//        drawerToggle.setDrawerIndicatorEnabled(enabled);
//        if (enabled)
//            drawerToggle.setDrawerIndicatorEnabled(true);
//        else
//            drawerToggle.setHomeAsUpIndicator();
    }
}
