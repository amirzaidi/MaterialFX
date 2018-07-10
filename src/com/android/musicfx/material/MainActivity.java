package com.android.musicfx.material;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Typeface mGSans;
    private AudioHandler mAudioHandler;

    private ColorStateList mWhite;
    private ColorStateList mAccent;
    private ColorStateList mGreyFg;
    private ColorStateList mGreyBg;

    private FloatingActionButton mSpeakerToggle;
    private FloatingActionButton mAuxToggle;
    private FloatingActionButton mBtToggle;
    private FloatingActionButton mUsbToggle;

    private PresetsFragment mPresets;
    private BandsFragment mBands;
    private EffectsFragment mEffects;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_presets:
                    switchToPresets();
                    return true;
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

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, AudioPortUpdater.class));

        final Intent intent = getIntent();
        int audioSession = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION,
                AudioEffect.ERROR_BAD_VALUE);
        Log.v(TAG, "audio session: " + audioSession);

        mAudioHandler = new AudioHandler(this, getCallingPackage(), audioSession);

        View barView = getLayoutInflater().inflate(R.layout.actionbar, null);
        mGSans = Typeface.createFromAsset(getAssets(), "fonts/GoogleSans-Medium.ttf");
        ((TextView) barView.findViewById(R.id.actionbar_title)).setTypeface(mGSans);

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

        mPresets = new PresetsFragment();
        mBands = new BandsFragment();
        mEffects = new EffectsFragment();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_bands);

        if (Utilities.V26) {
            View root = getWindow().getDecorView();
            root.setSystemUiVisibility(root.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            getWindow().setNavigationBarColor(getColor(R.color.background_material_light));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public AudioHandler getAudioHandler() {
        return mAudioHandler;
    }

    public Typeface getGSans() {
        return mGSans;
    }

    private void switchToPresets() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_holder, mPresets)
                .commit();
    }

    private void switchToBands() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_holder, mBands)
                .commit();
    }

    private void switchToEffects() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_holder, mEffects)
                .commitNow();
    }

    /**
     * Inverts the mode (enabled or disabled) for the pressed button.
     * @param view The button that was pressed.
     */
    public void toggleFab(View view) {
        FloatingActionButton button = (FloatingActionButton) view;
        AudioPortUpdater.Mode mode = getMode(button);
        mAudioHandler.setEnabled(mode, !mAudioHandler.isEnabled(mode));
        loadToggleViews();
    }

    /**
     * Sets the colors for all the buttons.
     */
    private void loadToggleViews() {
        for (View view : new View[] { mSpeakerToggle, mAuxToggle, mBtToggle, mUsbToggle }) {
            FloatingActionButton button = (FloatingActionButton) view;
            if (mAudioHandler.isEnabled(getMode(button))) {
                button.setImageTintList(mWhite);
                button.setBackgroundTintList(mAccent);
            } else {
                button.setImageTintList(mGreyFg);
                button.setBackgroundTintList(mGreyBg);
            }
        }
    }

    /**
     * Transforms a button into the corresponding Mode.
     * @param view The button.
     * @return The corresponding mode.
     */
    private AudioPortUpdater.Mode getMode(FloatingActionButton view) {
        AudioPortUpdater.Mode mode = AudioPortUpdater.Mode.Unknown;
        if (view == mSpeakerToggle) {
            mode = AudioPortUpdater.Mode.Speaker;
        } else if (view == mAuxToggle) {
            mode = AudioPortUpdater.Mode.Aux;
        } else if (view == mBtToggle) {
            mode = AudioPortUpdater.Mode.Bluetooth;
        }
        return mode;
    }
}
