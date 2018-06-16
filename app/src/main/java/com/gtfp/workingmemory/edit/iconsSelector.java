package com.gtfp.workingmemory.edit;

import com.gtfp.workingmemory.R;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


/**
 * Copyright (C) 2017  Greg T. F. Perry
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
public class iconsSelector extends Fragment{

    private GridView gridView;

    private ImageView imageView;




    public iconsSelector(){

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.activity_edit_item, container, false);
//
//        GridView gridView = (GridView) view.findViewById(R.id.gridview);
//
//        gridView.setAdapter(new iconsList(view.getContext()));

        return view;
    }





    class iconsList extends BaseAdapter{

        // references to our images
        private Integer[] mThumbIds = {};



        iconsList(Context context){

        }



        @Override
        public int getCount(){

            return 0;
        }



        @Override
        public Object getItem(int position){

            return new Object();
        }



        @Override
        public long getItemId(int position){

            return 0L;
        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent){

            return convertView;
        }
    }
}