package com.gtfp.workingmemory.colorPicker;

import com.gtfp.workingmemory.R;
import com.gtfp.workingmemory.settings.appSettings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Created by Drawn on 2015-03-25.
 */
public class colorPickerActivity extends Activity
        implements colorPickerView.OnColorChangedListener, AdapterView.OnItemSelectedListener {

    private Spinner mLedOnMs;

    private Spinner mLedOffMs;

    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIntent = getIntent();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // We're being restored from a previous state,
        if (savedInstanceState != null) {

        }

        setContentView(R.layout.activity_color_picker);

        setLEDSettings();
    }

    private void setLEDSettings() {

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.LedOnMS_rates, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mLedOnMs = (Spinner) findViewById(R.id.spnLedOnMs);

        mLedOnMs.setOnItemSelectedListener(this);

        // Apply the adapter to the spinner
        mLedOnMs.setAdapter(adapter);

        int LEDonRate = mIntent.getIntExtra("LedOnMs", 100);

        if (LEDonRate == 100){

            LEDonRate = 0;

        }else

        if (LEDonRate == 300){

            LEDonRate = 1;

        }else

        if (LEDonRate == 500){

            LEDonRate = 2;
        }

        mLedOnMs.setSelection(LEDonRate);

        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(this, R.array.LedOffMS_rates, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mLedOffMs = (Spinner) findViewById(R.id.spnLedOffMs);

        mLedOffMs.setOnItemSelectedListener(this);

        // Apply the adapter to the spinner
        mLedOffMs.setAdapter(adapter);

        LEDonRate = mIntent.getIntExtra("LedOffMs", 2000);

        LEDonRate = (LEDonRate/1000) - 1;

        mLedOffMs.setSelection(LEDonRate);
    }


    public static Intent getLEDRates() {

        Intent intent = new Intent();

        intent.putExtra("LedOnMs", appSettings.getInt("LedOnMs", 100));

        intent.putExtra("LedOffMs", appSettings.getInt("LedOffMs", 2000));

        return intent;
    }


    public static void OnActivityResult(Intent data, SharedPreferences.Editor editor){

        int LEDSetting = data.getIntExtra("LedOnMs", 0);

        if (LEDSetting != 0) {

            editor.putInt("LedOnMs", LEDSetting);

            editor.apply();
        }

        LEDSetting = data.getIntExtra("LedOffMs", 0);

        if (LEDSetting != 0) {

            editor.putInt("LedOffMs", LEDSetting);

            editor.apply();
        }
    }


    @Override
    public void colorChanged(int colour) {

        mIntent.putExtra("colour", colour);

        // Activity finished ok, return the data
        // set result code and bundle data for response
        setResult(RESULT_OK, mIntent);

        // Close the activity
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        if (mLedOnMs == parent){

            int LEDonRate = 300;

            if (pos == 0){

                LEDonRate = 100;

            }else

            if (pos == 1){

                LEDonRate = 300;

            }else

            if (pos == 2){

                LEDonRate = 500;
            }

            mIntent.putExtra("LedOnMs", LEDonRate);
        } else

        if (mLedOffMs == parent){

            // Number of seconds the LED of off
            mIntent.putExtra("LedOffMs", 1000 * (pos+1));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
