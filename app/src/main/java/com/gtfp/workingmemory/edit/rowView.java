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
import com.gtfp.workingmemory.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class rowView {

	public TextView mTextView;

        public TextView itemDueDateView;

        public ImageView priorityImage;

	public LinearLayout container;

	public int position;

        private int mOriginalPaintFlags;

        private Typeface mTypeface;

        private ColorStateList mTextColor;

        public rowView(){
        }


        // Restore the original paint flags.
        public void setTextOriginal(){

            if (mOriginalPaintFlags != 0)
              mTextView.setPaintFlags(mOriginalPaintFlags);

            if (mTypeface != null)
               mTextView.setTypeface(mTypeface, 0);

            if (mTextColor != null)
               mTextView.setTextColor(mTextColor);

        }


        public void markAsDeleted(){

            if (mOriginalPaintFlags == 0)
                mOriginalPaintFlags = mTextView.getPaintFlags();

            mTextView.setPaintFlags(mOriginalPaintFlags | Paint.STRIKE_THRU_TEXT_FLAG);

            if (mTypeface == null)
               mTypeface = mTextView.getTypeface();

            mTextView.setTypeface(mTypeface, Typeface.ITALIC);

            if (mTextColor == null)
                mTextColor = mTextView.getTextColors();

            mTextView.setTextColor(Color.GRAY);
        }


	public void highLightHolder(Context context) {

		itemDueDateView.setBackgroundColor(context
                        .getResources().getColor(R.color.RSSOrange));

		mTextView.setBackgroundColor(context
				.getResources().getColor(R.color.RSSOrange));

		priorityImage.setBackgroundColor(context
				.getResources().getColor(R.color.RSSOrange));
	}


	public boolean setFont(Context context) {

                Typeface font;

                try {

                    // May throw if font not found.
                    font = Typeface.createFromAsset(context.getAssets(), "Roboto-Thin.ttf");

                    if (mTypeface == null)
                        mTypeface = mTextView.getTypeface();

                    mTextView.setTypeface(font);

                }catch (RuntimeException ex){

                    font = null;
                }

                return font != null;
	}


	public void unHighLightHolder() {

		itemDueDateView.setBackgroundColor(Color.WHITE);

		mTextView.setBackgroundColor(Color.WHITE);

		priorityImage.setBackgroundColor(Color.WHITE);
	}
}
