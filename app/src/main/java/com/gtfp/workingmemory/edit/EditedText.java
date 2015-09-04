package com.gtfp.workingmemory.edit;

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