package com.gtfp.workingmemory.edit;


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
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class EditedText extends EditText {

    protected CharSequence mOldText;

    public EditedText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);

        // TODO This is not going to work with a 'new' item.
        if (text.length() > 0 && mOldText == null) {

            mOldText = text;
        }
    }


    public boolean isChanged() {

        return mOldText == null || !mOldText.equals(getText().toString());
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event){

        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {

            editToDoItem activity = (editToDoItem) getContext();

            activity.finish();

            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }
}