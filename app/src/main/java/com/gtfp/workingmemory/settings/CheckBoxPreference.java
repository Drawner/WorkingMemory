package com.gtfp.workingmemory.settings;

import android.content.Context;
import android.util.AttributeSet;


/**
 * Copyright (C) 2015  Greg T. F. Perry
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
