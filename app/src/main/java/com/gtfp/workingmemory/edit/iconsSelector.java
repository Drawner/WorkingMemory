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
 * Created by Drawn on 4/20/2017.
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