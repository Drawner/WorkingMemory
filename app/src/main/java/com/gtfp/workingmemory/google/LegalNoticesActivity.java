package com.gtfp.workingmemory.google;

import com.google.android.gms.common.GoogleApiAvailability;

import com.gtfp.workingmemory.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Drawn on 2016-03-06.
 */
public class LegalNoticesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legal);

        TextView legal = (TextView) findViewById(R.id.legal);

        legal.setText(
                GoogleApiAvailability
                        .getInstance()
                        .getOpenSourceSoftwareLicenseInfo(this));
    }
}

