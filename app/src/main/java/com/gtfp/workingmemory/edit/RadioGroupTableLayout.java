package com.gtfp.workingmemory.edit;

import com.gtfp.workingmemory.BuildConfig;
import com.gtfp.workingmemory.todo.ToDoItem;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class RadioGroupTableLayout extends TableLayout implements OnClickListener {

    private static final String TAG = RadioGroupTableLayout.class.getSimpleName();

    private RadioButton mActiveButton;

    // holds the checked id; the selection is empty by default
    private int mCheckedId = -1;

    // when true, mOnCheckedChangeListener discards events
    private boolean mProtectFromCheckedChange = false;

    editToDoItem mEditItem;

    private ArrayList<RadioButton> radioButtons = new ArrayList<RadioButton>();

    private static Calendar mTmpCal;

    private Calendar mToday = Calendar.getInstance();

    /**
     * @param context
     */
    public RadioGroupTableLayout(Context context) {
        super(context);

        initTime(context);
    }


    /**
     * @param context
     * @param attrs
     */
    public RadioGroupTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        initTime(context);
    }

    @Override
    public void onClick(View v) {

        setChecked((RadioButton) v);

        if (mEditItem.mCanSnooze) {

            stepTime(mEditItem.mOldDate, 1);

            mEditItem.editDueDate.setText(ToDoItem.displayDueDate(mTmpCal.getTimeInMillis()));

            mEditItem.mDateTime = (Calendar) mTmpCal.clone();

            if (!mEditItem.testPastDue(mTmpCal, true)){

                mEditItem.mTP.setCurrentHour(mTmpCal.get(Calendar.HOUR_OF_DAY));

                mEditItem.mTP.setCurrentMinute(mTmpCal.get(Calendar.MINUTE));
            }
        }else{

            // No need to save the date value back. The radio buttons are read again when saving.
            stepTime(mEditItem.mDateTime, -1);

            if (!mEditItem.testPastDue(mTmpCal, true)){

                Toast.makeText(mEditItem, ToDoItem.displayTime(mTmpCal.getTimeInMillis()), Toast.LENGTH_SHORT).show();
            }

            enableButtons();
        }
    }


    private void initTime(Context context){

        if (context instanceof editToDoItem)
            mEditItem = (editToDoItem) context;
    }

    private void stepTime(Calendar original, int increment) {

        mTmpCal = (Calendar) original.clone();

        if (BuildConfig.DEBUG){

            String test = ToDoItem.EpochToDateTime(mTmpCal.getTimeInMillis());

            test = null;
        }

        String label = mActiveButton.getText().toString();

        int step = increment > -1 ? 1 : -1;

        step = step * Integer.parseInt(label.replaceAll("[^\\d.]", ""));

        mTmpCal.add(Calendar.MINUTE, step);
    }


    // Initialize a radio group.
    public boolean check(int id) {

        boolean checked = false;

        // don't even bother
        if (id > 0 && (id == mCheckedId)) {

            return checked;
        }

        if (mCheckedId != -1) {

            setCheckedStateForView(mCheckedId, false);
        }

        if (id > 0) {

            setCheckedStateForView(id, true);

            checked = true;
        }

        return checked;
    }

    public void enableButtons(){

        mToday.setTimeInMillis(System.currentTimeMillis());
        mToday.set(Calendar.SECOND, 0);
        mToday.set(Calendar.MILLISECOND, 0);

        Calendar tmpCal;

        for (RadioButton btn : getRadioButtons()) {

            tmpCal = (Calendar) mEditItem.mDateTime.clone();

            int mins = 3 + Integer.parseInt(btn.getText().toString().replaceAll("[^\\d.]", ""));

            tmpCal.add(Calendar.MINUTE, -1 * mins);

            btn.setEnabled(!tmpCal.before(mToday));
        }
    }


    public void setCheckedStateForView(int radioId, boolean checked) {

        // If not a valid id there's nothing to check
        if (radioId < 1 ) return;

        View checkedView = findViewById(radioId);

        if (checkedView != null && checkedView instanceof RadioButton) {

            RadioButton rb = (RadioButton) checkedView;

            if ( checked ){

                setChecked(rb);
            }else {

                rb.setChecked(checked);
            }
        }
    }


    private void setChecked(RadioButton rb){

        if ( mActiveButton != null ) {

            mActiveButton.setChecked(false);
        }

        if (!rb.isEnabled()){

            mActiveButton = null;
        }else {

            rb.setChecked(true);

            mActiveButton = rb;
        }
    }


    /* (non-Javadoc)
     * @see android.widget.TableLayout#addView(android.view.View, int, android.view.ViewGroup.LayoutParams)
     */
    @Override
    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);

        setChildrenOnClickListener((TableRow)child);
    }


    /* (non-Javadoc)
     * @see android.widget.TableLayout#addView(android.view.View, android.view.ViewGroup.LayoutParams)
     */
    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);

        setChildrenOnClickListener((TableRow)child);
    }


    private void setChildrenOnClickListener(TableRow tr) {

        final int c = tr.getChildCount();

        for (int i=0; i < c; i++) {

            final View v = tr.getChildAt(i);

            if ( v instanceof RadioButton ) {

                radioButtons.add((RadioButton)v);

                v.setOnClickListener(this);
            }
        }
    }


    ArrayList<RadioButton> getRadioButtons(){

       return radioButtons;
    }


    public int getCheckedRadioButtonId() {

        if ( mActiveButton != null && mActiveButton.isChecked() ) {

            return mActiveButton.getId();
        }

        return -1;
    }


    public int getStep(){

        return -1;
    }

}