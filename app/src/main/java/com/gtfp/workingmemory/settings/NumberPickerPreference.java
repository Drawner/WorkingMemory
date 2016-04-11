package com.gtfp.workingmemory.settings;

import com.gtfp.workingmemory.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

/*
 * Copyright (C) 2011 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * @author Danesh
 * @author nebkat
 * @author G Perry
 */

public class NumberPickerPreference extends DialogPreference {

    public static final int[] PreferenceNumbers = {0x7f0100c1, 0x7f0100c2, 0x7f0100c3};

    public static final int NumberPickerPreference_maxExternal = 1;

    public static final int NumberPickerPreference_minExternal = 0;

    private final int mMin, mMax, mDefault;

    private final String mMaxExternalKey, mMinExternalKey;

    private NumberPicker mNumberPicker;

    public NumberPickerPreference(Context context, AttributeSet attrs) {

        super(context, attrs);
        // com.android.internal.R.styleable.DialogPreference
        TypedArray dialogType = context.obtainStyledAttributes(attrs,
                NumberPickerPreference.PreferenceNumbers, 0, 0);
        TypedArray numberPickerType = context.obtainStyledAttributes(attrs,
                NumberPickerPreference.PreferenceNumbers, 0, 0);

        mMaxExternalKey = numberPickerType.getString(NumberPickerPreference.NumberPickerPreference_maxExternal);
        mMinExternalKey = numberPickerType.getString(NumberPickerPreference.NumberPickerPreference_minExternal);

        mMax = numberPickerType.getInt(1, 1);
        mMin = numberPickerType.getInt(0, 0);

        // com.android.internal.R.styleable.Preference_defaultValue
        mDefault = dialogType.getInt(1, mMin);

        dialogType.recycle();
        numberPickerType.recycle();
    }

    @Override
    protected View onCreateDialogView() {

        int max = mMax;
        int min = mMin;

        // External values
        if (mMaxExternalKey != null) {

            max = getSharedPreferences().getInt(mMaxExternalKey, mMax);
        }

        if (mMinExternalKey != null) {

            min = getSharedPreferences().getInt(mMinExternalKey, mMin);
        }

        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.number_picker_dialog, null);

        mNumberPicker = (NumberPicker) view.findViewById(R.id.number_picker);

        // Initialize state
        mNumberPicker.setMaxValue(max);
        mNumberPicker.setMinValue(min);
        mNumberPicker.setValue(getPersistedInt(mDefault));
        mNumberPicker.setWrapSelectorWheel(false);

        int id = Resources.getSystem().getIdentifier("numberpicker_input", "id", "android");

        // No keyboard popup
        EditText textInput = (EditText) mNumberPicker.findViewById(id); //com.android.internal.R.id.numberpicker_input);
        textInput.setCursorVisible(false);
        textInput.setFocusable(false);
        textInput.setFocusableInTouchMode(false);

        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {

        if (positiveResult) {

            persistInt(mNumberPicker.getValue());
        }
    }

}
