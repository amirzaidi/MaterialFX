package com.android;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.musicfx.R;

public class MainActivity extends AppCompatActivity {
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

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mBands = new BandsFragment();
        mEffects = new EffectsFragment();

        getFragmentManager().beginTransaction().add(R.id.fragment_holder, mBands).commit();
    }

    private void switchToBands() {
        getFragmentManager().beginTransaction().replace(R.id.fragment_holder, mBands).commit();
    }

    private void switchToEffects() {
        getFragmentManager().beginTransaction().replace(R.id.fragment_holder, mEffects).commit();
    }
}
