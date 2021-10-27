package com.hekkelman.keylocker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.databinding.ActivityMainDetailBinding;
import com.hekkelman.keylocker.databinding.FragmentKeyDetailBinding;
import com.hekkelman.keylocker.datamodel.Key;
import com.hekkelman.keylocker.datamodel.KeyDbModel;

public class KeyDetailFragment extends Fragment {

    private FragmentKeyDetailBinding binding;
    private KeyDbModel keyDbModel;

    protected EditText nameField;
    protected EditText userField;
    protected EditText passwordField;
    protected EditText urlField;
    protected TextView lastModified;

    public KeyDetailFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        keyDbModel = new ViewModelProvider(getActivity()).get(KeyDbModel.class);
        keyDbModel.getSelectedKey().observe(this, this::onChanged);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentKeyDetailBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        nameField = binding.keyName;
        userField = binding.keyUser;
        passwordField = binding.keyPassword;
        urlField = binding.keyUrl;
        lastModified = binding.keyLastModified;

//        mToolbarLayout = rootView.findViewById(R.id.toolbar_layout);
//        mTextView = binding.itemDetail;

        // Show the placeholder content as text in a TextView & in the toolbar if available.
//        updateContent();
//        rootView.setOnDragListener(dragListener);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void onChanged(Key key) {
//        this.key = key;
//
        String name = key.getName();
        if (name != null) nameField.setText(name);

        String password = key.getPassword();
        if (password != null) passwordField.setText(password);

        String user = key.getUser();
        if (user != null) userField.setText(user);

        String url = key.getUrl();
        if (url != null) urlField.setText(url);

        String lastModified = key.getTimestamp();
        if (lastModified != null)
            this.lastModified.setText(String.format(getString(R.string.lastModifiedTemplate), lastModified));


    }


    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        ActivityMainDetailBinding binding = ActivityMainDetailBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.nav_host_fragment_item_detail);
//        NavController navController = navHostFragment.getNavController();
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.
//                Builder(navController.getGraph())
//                .build();
//
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_item_detail);
//        return navController.navigateUp() || super.onSupportNavigateUp();
//    }

}
