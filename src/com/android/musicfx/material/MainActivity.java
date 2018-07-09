package com.android.musicfx.material;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        getFragmentManager().beginTransaction().replace(R.id.fragment_holder, mBands).commit();
    }

    private void switchToEffects() {
        getFragmentManager().beginTransaction().replace(R.id.fragment_holder, mEffects).commit();
    }
}
