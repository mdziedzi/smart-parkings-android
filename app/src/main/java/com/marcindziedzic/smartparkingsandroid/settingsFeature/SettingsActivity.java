package com.marcindziedzic.smartparkingsandroid.settingsFeature;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;

import com.marcindziedzic.smartparkingsandroid.R;
import com.marcindziedzic.smartparkingsandroid.util.Constants;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar seekBar;
    private int seekBarValue;
    private FloatingActionButton applyPreferencesFloatingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();

        getSettingsValues();

    }

    private void getSettingsValues() {
        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
                Activity
                        .MODE_PRIVATE);
        seekBarValue = sp.getInt(Constants.PREFERENCES_KEY, -1);
        if (seekBarValue == -1) {
            seekBarValue = 50;
        }
        seekBar.setProgress(seekBarValue);
    }

    private void initViews() {
        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // do nothing
            }
        });

        applyPreferencesFloatingButton = findViewById(R.id.applyPreferencesFloatingButton);
        applyPreferencesFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveValueToSharedPreferences();
                goToMapsActivity();
            }
        });
    }

    private void goToMapsActivity() {
        finish();
//        Intent intent = new Intent(this, MapsActivity.class);
//        startActivity(intent);
    }

    private void saveValueToSharedPreferences() {
        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
                Activity
                        .MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(Constants.PREFERENCES_KEY, seekBarValue);
        editor.apply();
    }
}
