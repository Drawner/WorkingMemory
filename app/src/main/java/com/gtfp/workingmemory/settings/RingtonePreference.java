package com.gtfp.workingmemory.settings;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Drawn on 2015-04-12.
 */
public class RingtonePreference extends android.preference.RingtonePreference {

    private String mRingtone;

    public RingtonePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RingtonePreference(Context context) {
        super(context, null);
    }

    public void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {

        super.onSetInitialValue(restorePersistedValue, defaultValue);

        mRingtone = (String) defaultValue;
    }

    protected String getPersistedString(String defaultReturnValue) {

        if (!shouldPersist()) {

            return mRingtone == null ? defaultReturnValue : mRingtone;
        }else {

            return appSettings.getString(getKey(), defaultReturnValue);
        }
    }
}
