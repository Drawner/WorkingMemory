package com.gtfp.workingmemory.todo;

import com.gtfp.workingmemory.R;
import com.gtfp.workingmemory.edit.rowView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;


/**
 * Created by Drawn on 2015-02-10.
 */
public class ToDoListAdapter extends BaseAdapter {

    ToDoItemsList mToDoItemsList;

    private int mModItemId = -1;

    // This gets accessed alot!
    private int mCount = 0;


    public ToDoListAdapter(List<ToDoItem> listToDos) {

        mToDoItemsList = new ToDoItemsList(listToDos);

        mCount = mToDoItemsList.size();
    }

    void setItems(List<ToDoItem> listToDos) {

        mToDoItemsList.setItems(listToDos);

        mToDoItemsList.sortByDueDate();

        mCount = mToDoItemsList.size();
    }

    List<ToDoItem> ToDoList(){

        return mToDoItemsList.ToDoList();
    }

    public boolean add(ToDoItem itemToDo) {

        boolean added = mToDoItemsList.add(itemToDo);

        if (added) {

            mToDoItemsList.sortByDueDate();

            // getView will provide a null view that will be populated.
            mCount = mToDoItemsList.size();

            // Note: mCount is accessed here.
            notifyDataSetChanged();
        }

        return added;
    }

    public boolean save(ToDoItem itemToDo) {

        mModItemId = mToDoItemsList.update(itemToDo);

        boolean saved = mModItemId > -1;

        if (saved) {

            mToDoItemsList.sortByDueDate();

            notifyDataSetChanged();
        }

        return saved;
    }

    boolean update(ToDoItem itemToDo) {

        return mToDoItemsList.update(itemToDo) > -1;
    }



    public boolean remove (ToDoItem itemToDo){

        return remove(itemToDo.getListPosn());
    }


    boolean remove(int index) {

        int prevCount = mCount;

        mToDoItemsList.remove(index);

        mCount = mToDoItemsList.size();

        // Note: mCount is accessed here!
        notifyDataSetChanged();

        return mCount < prevCount;
    }


    public void refresh(List<ToDoItem> listToDos){

        setItems(listToDos);

        notifyDataSetChanged();
    }


    public void sort(){

        mToDoItemsList.sortByDueDate();

        notifyDataSetChanged();
    }

    public int getListId(ToDoItem itemId){

        return mToDoItemsList.getListId(itemId);
    }

    public void Iterate(ToDoItemsList.ToDoIterator iterateObj){

        ToDoItemsList.ToDoIterator current = ToDoItemsList.setIterator(iterateObj);

        mToDoItemsList.iterateToDoItems();

        ToDoItemsList.setIterator(current);
    }

    @Override
    // This should be called recycleView()  hehe!
    public View getView(int position, View convertView, ViewGroup parent) {

        rowView h;

        if (convertView != null) {

            h = (rowView) convertView.getTag();

        } else {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            convertView = inflater.inflate(R.layout.row, parent, false);

            h = new rowView();

            h.position = position;

            h.container = (LinearLayout) convertView.findViewById(R.id.container);

            h.mTextView = (TextView) convertView.findViewById(R.id.itemText);

            h.priorityImage = (ImageView) convertView.findViewById(R.id.priorityImage);

            h.itemDueDateView = (TextView) convertView.findViewById(R.id.itemDueDate);

//            h.container.setBackgroundColor(Color.argb(0, 0, 0, 0));

//            h.setFont(parent.getContext());

//            h.unHighLightHolder();

            convertView.setTag(h);

            convertView.setBackgroundResource(R.drawable.rounded_corners);
        }

        ToDoItem itemToDo = mToDoItemsList.get(position);

        // It's a convertview now 'out of sight' and so you are to recycle it.
        h.mTextView.setText(itemToDo.getItemName());

        if (itemToDo.isDeleted()){

            h.setFont(parent.getContext());

            h.markAsDeleted();

        }else {

            h.setTextOriginal();
        }

        h.itemDueDateView.setText(itemToDo.getDueDateToDisplay());

//        if (mModItemId == position) {
//
//            h.highLightHolder(parent.getContext());
//        } else {
//
//            h.unHighLightHolder();
//        }

        return convertView;
    }

    @Override
    // This is being called alot by the UI thread.
    public int getCount() {

        return mCount;
    }

    @Override
    public Object getItem(int index) {

        mModItemId = -1;

        return mToDoItemsList.get(index);
    }

    @Override
    // Return the rowid
    public long getItemId(int index) {

        return mToDoItemsList.getId(index);
    }

    @Override
    public boolean hasStableIds() {

        return false;
    }


    public void onDestroy() {

        mToDoItemsList.onDestroy();

        mToDoItemsList = null;
    }
}