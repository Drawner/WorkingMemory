package com.gtfp.workingmemory.edit;

import com.gtfp.workingmemory.utils.Dialogue;

import android.app.Activity;
import android.os.Bundle;


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
public class AlarmActivity extends Activity {

    private static final String ALARM_ITEM = "item";

    private static final String ALARM_TIME = "time";

    private static final String ALARM_ID = "id";

    private Dialogue mDialWnd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDialWnd = new Dialogue(this);

        mDialWnd.show();
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();

        mDialWnd.onDestroy();

        mDialWnd = null;
    }
}
