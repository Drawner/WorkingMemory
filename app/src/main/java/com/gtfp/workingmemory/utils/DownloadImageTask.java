package com.gtfp.workingmemory.utils;

import com.andrioussolutions.errorhandler.ErrorHandler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;


/**
 * Copyright (C) 2017 Greg T. F. Perry
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