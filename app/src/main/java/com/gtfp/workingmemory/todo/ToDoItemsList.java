package com.gtfp.workingmemory.todo;

import com.gtfp.workingmemory.settings.appSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.NoSuchElementException;


public class ToDoItemsList {

    ArrayList<ToDoItem> mToDoItems;

    private boolean mDescending;

    private Comparator mComparator;

    private static ToDoIterator mIterateObj;

    ToDoItemsList(){

        mComparator = new CompareDueDate();
    }

    ToDoItemsList(List<ToDoItem> list){
        this();

        setItems(list);

        // Sort items
        sortByDueDate();
    }

    public void setItems(List<ToDoItem> list) {

        mToDoItems = new ArrayList<ToDoItem>(list);
    }

    public final List<ToDoItem> ToDoList() {

        return Collections.unmodifiableList(mToDoItems);
    }

    public ToDoItem newToDoItem(){

        // Return an empty one
        ToDoItem itemToDo = new ToDoItem();

        long today = ToDoItem.TodayInEpoch();

        itemToDo.setDueDate(today);

        itemToDo.setReminderEpoch(today);

        return itemToDo;
    }

    public ToDoItem get(int index) {

        ToDoItem itemToDo;

        // Return an empty item.
        if (index < 0) {

            return newToDoItem();
        }

        try{

            itemToDo = mToDoItems.get(index);

        }catch(RuntimeException ex){

            itemToDo = newToDoItem();
        }
        return itemToDo;
    }

    // Returns the rowid
    public long getId(int index){

        ToDoItem itemToDo = get(index);

        long id =  itemToDo.getId();

        itemToDo = null;

        return id;
    }

    public int SelectItemId(ToDoItem itemToDo){

        return getListId(itemToDo);
    }

    public int getListId(ToDoItem itemToDo){

        return getListId(itemToDo.getId());
    }

    public int getListId(long itemId){

        int indx = -1;

        for (int cnt=0; cnt < mToDoItems.size(); cnt++){

            if(mToDoItems.get(cnt).getId() == itemId){

                indx = cnt;
                break;
            }
        }
        return indx;
    }

    public int size() {

        return mToDoItems.size();
    }

    public boolean add(ToDoItem item) {

        try {

            return mToDoItems.add(item);

        }catch(RuntimeException ex){

            return false;
        }
    }

    public int update(ToDoItem item){

        int listId = item.getListPosn();

        if ( listId > -1){

            try{

               mToDoItems.set(listId, item);

            }catch(RuntimeException ex){

                listId = -1;
            }
        }

        return listId;
    }

    public ToDoItem remove(int index) {

        ToDoItem itemToDo;

        try{

            itemToDo = mToDoItems.remove(index);

        }catch(RuntimeException ex){

            itemToDo = newToDoItem();
        }

        return itemToDo;
    }

    public void setItem(int index, ToDoItem item) {
        mToDoItems.set(index, item);
    }

    public void set(int index, ToDoItem item) {
        mToDoItems.set(index, item);
    }

    public void sortByDueDate() {

        mDescending = appSettings.getBoolean("order_of_items", false);

        Collections.sort(mToDoItems, mComparator);
    }

    public void setDueDate(int index, int year, int monthOfYear, int dayOfMonth) {

        assert(index != -1);

        ToDoItem item = get(index);

        String month, day;

        if ( monthOfYear < 10 ) month = "0" + monthOfYear;

        else month = Integer.toString(monthOfYear);

        if ( dayOfMonth < 10 ) day = "0" + dayOfMonth;

        else day = Integer.toString(dayOfMonth);

        String duedate = year + "-" + month + "-" + day;

        item.setDueDate(duedate);
    }

    void iterateToDoItems(){

       if (mIterateObj == null) return;

       try {

           for (ToDoItem itemToDo : mToDoItems) {

               if (!mIterateObj.Iterate(itemToDo)) {

                   break;
               }
           }

       }catch(ConcurrentModificationException ex){

       }catch(NoSuchElementException ex){

       }catch(RuntimeException ex){

       }
    }

    static ToDoIterator setIterator(ToDoIterator iterateObj){

        ToDoIterator currentObj = mIterateObj;

        mIterateObj = iterateObj;

        return currentObj;
    }

    public interface ToDoIterator{

        boolean Iterate(ToDoItem itemToDo);
    }


    protected void onDestroy() {

        mComparator = null;
    }


    public class CompareDueDate implements Comparator<ToDoItem>{

        @Override
        public int compare(ToDoItem o1, ToDoItem o2) {

            if (mDescending){

                // Closest Time first
                return (int) (o2.getDueDateInEpoch() > o1.getDueDateInEpoch() ? 1 : -1);
            }else{

                // Oldest Time first
                return (int) (o2.getDueDateInEpoch() < o1.getDueDateInEpoch() ? 1 : -1);
            }
        }
    }
}