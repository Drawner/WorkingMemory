package com.gtfp.workingmemory.utils;

import com.gtfp.errorhandler.ErrorHandler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
/**
 * Created by Drawn on 4/30/2017.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{

    private ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {

        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {

        String urldisplay = urls[0];

        Bitmap mIcon11;

        try {

            InputStream in = new java.net.URL(urldisplay).openStream();

            mIcon11 = BitmapFactory.decodeStream(in);

        } catch (Exception ex) {

            mIcon11 = null;

            ErrorHandler.logError(ex);
        }
        return mIcon11;
    }



    protected void onPostExecute(Bitmap result) {

        bmImage.setImageBitmap(result);
    }
}