package com.android.musicfx.material;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ColorStateList mWhite;
    private ColorStateList mAccent;
    private ColorStateList mGreyFg;
    private ColorStateList mGreyBg;

    private FloatingActionButton mSpeakerToggle;
    private FloatingActionButton mAuxToggle;
    private FloatingActionButton mBtToggle;
    private FloatingActionButton mUsbToggle;

    private BandsFragment mBands;
    private EffectsFragment mEffects;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_bands:
                    switchToBands();
                    return true;
                case R.id.navigation_effects:
                    switchToEffects();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View barView = getLayoutInflater().inflate(R.layout.actionbar, null);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/GoogleSans-Regular.ttf");
        ((TextView) barView.findViewById(R.id.actionbar_title)).setTypeface(tf);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(barView,
                new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER));
        bar.setTitle(R.string.app_name_override);
        bar.setDisplayHomeAsUpEnabled(true);

        mWhite = ColorStateList.valueOf(getColor(android.R.color.white));
        mAccent = ColorStateList.valueOf(getColor(R.color.colorAccent));
        mGreyFg = ColorStateList.valueOf(getColor(R.color.grey_fg));
        mGreyBg = ColorStateList.valueOf(getColor(R.color.grey_bg));

        mSpeakerToggle = findViewById(R.id.toggle_speaker);
        mAuxToggle = findViewById(R.id.toggle_aux);
        mBtToggle = findViewById(R.id.toggle_bt);
        mUsbToggle = findViewById(R.id.toggle_usb);
        loadToggleViews();

        mBands = new BandsFragment();
        mEffects = new EffectsFragment();

        getFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_holder, mBands)
                .commit();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchToBands() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_holder, mBands)
                .commit();
    }

    private void switchToEffects() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_holder, mEffects)
                .commit();
    }

    public void toggleFab(View view) {
        FloatingActionButton button = (FloatingActionButton) view;
        int id = view.getId();
        loadToggleViews();
    }

    private void loadToggleViews() {
        for (View view : new View[] { mSpeakerToggle, mAuxToggle, mBtToggle, mUsbToggle }) {
            FloatingActionButton button = (FloatingActionButton) view;
            if (isEnabled(button)) {
                button.setImageTintList(mWhite);
                button.setBackgroundTintList(mAccent);
            } else {
                button.setImageTintList(mGreyFg);
                button.setBackgroundTintList(mGreyBg);
            }
        }
    }

    private boolean isEnabled(FloatingActionButton view) {
        return view == mAuxToggle;
    }
}
