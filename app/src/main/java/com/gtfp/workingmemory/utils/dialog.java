package com.gtfp.workingmemory.utils;

import com.gtfp.workingmemory.R;
import com.gtfp.workingmemory.settings.appSettings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

/**
 * Created by Drawn on 2015-03-01.
 */
public class dialog {

// Interesting, but can't pursue this idea right now.
//    private static dialog mThis = new dialog();

    public static final int REQUEST_CODE = dialog.class.hashCode();

    private Dialogue mDialogue;

    private DialogBoxListener mListener;

    private Context mContext;

    public static final int YESNO = 1;

    public dialog() {
    }

    public dialog(Context context) {

        mContext = context;

        if (context instanceof DialogBoxListener) {

            setDialogBoxListener((DialogBoxListener) context);
        }
    }


    public void show(CharSequence message) {

        show(message, YESNO);
    }


    public void showBox(CharSequence message) {

        show(message, 0);
    }


    public void show(CharSequence message, int buttons) {

        if (mDialogue == null) {

            mDialogue = new Dialogue(message, buttons);
        } else {

            mDialogue.setMessage(message);
        }

        mDialogue.show();
    }


    public void onDestroy() {

        mListener = null;

        mContext = null;

        //If never shown, the dialogue may not have been created yet.
        if (mDialogue != null) {

            mDialogue.onDestroy();

            mDialogue = null;
        }
    }


    public interface DialogBoxListener {

        void dialogResult(boolean result);
    }

    // This is the means to access the outside world.
    public DialogBoxListener setDialogBoxListener(DialogBoxListener listener) {

        DialogBoxListener current = mListener;

        mListener = listener;

        return current;
    }


    class Dialogue implements DialogInterface.OnCancelListener, DialogInterface.OnShowListener {

        private AlertDialog mDialog;

        private CharSequence mMessage = "";

        Dialogue(CharSequence message, int buttons) {

            mMessage = message;

            if (mMessage == "") {

                mMessage = "<Enter message here>";
            }

            mDialog = buildDialog(buttons);

            // Handle if this dialog window is cancelled.
            mDialog.setOnCancelListener(this);

            mDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            mDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners);

            mDialog.setOnShowListener(this);
        }


        void show() {

            mDialog.show();
        }


        public void onShow(DialogInterface dialog) {

           switchButtons(appSettings.getBoolean("left_handed", false));
        }


        void switchButtons(boolean switchButtons) {

            if (mDialog == null) {

                return;
            }

            Button negativeButton = (Button) mDialog.findViewById(android.R.id.button2);

            ViewGroup buttonPanelContainer = (ViewGroup) negativeButton.getParent();

            int negativeButtonIndex = buttonPanelContainer.indexOfChild(negativeButton);

            // Already switched around. No need to continue.
            if (switchButtons && negativeButtonIndex == 2 || !switchButtons && negativeButtonIndex == 0) return;

            Button positiveButton = (Button) mDialog.findViewById(android.R.id.button1);

            // prepare exchange their index in ViewGroup
            buttonPanelContainer.removeView(positiveButton);

            buttonPanelContainer.removeView(negativeButton);

            if (negativeButtonIndex == 0){

                buttonPanelContainer.addView(positiveButton, 0);

                buttonPanelContainer.addView(negativeButton, buttonPanelContainer.getChildCount());
            }else{

                buttonPanelContainer.addView(negativeButton, 0);

                buttonPanelContainer.addView(positiveButton, buttonPanelContainer.getChildCount());
            }
        }

        void setMessage(CharSequence message) {

            mDialog.setMessage(message);
        }

        private AlertDialog buildDialog(int buttons) {

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder alertBuilt = new AlertDialog.Builder(mContext);

            alertBuilt.setMessage(mMessage);

            switch (buttons) {
                case YESNO:

                    alertBuilt
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int id) {

                                    runListener(dialog, id, true);
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int id) {

                                    runListener(dialog, id, false);
                                }
                            });
            }

            // Create the AlertDialog object and return it
            return alertBuilt.create();
        }

        private void runListener(DialogInterface dialog, int id, boolean result) {

            if (mListener != null) {

                mListener.dialogResult(result);
            }

            mDialog.dismiss();
        }

        public void onCancel(DialogInterface dialog) {

            // Always cancel with the Negative selection.
            runListener(dialog, 0, false);
        }

        private void onDestroy() {

            mDialog = null;
        }
    }
}
