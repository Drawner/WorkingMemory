package com.gtfp.workingmemory.settings;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Drawn on 2015-04-12.
 */
public class CheckBoxPreference extends android.preference.CheckBoxPreference {

    public CheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckBoxPreference(Context context) {
        super(context, null);
    }

    public void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue, defaultValue);
    }
}
