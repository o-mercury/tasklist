package com.timehacks.list.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Pair;

import com.timehacks.list.R;
import com.timehacks.list.datamodals.DataItem;

import java.util.ArrayList;

/**
 * Created by Redixbit on 30-01-2018.
 */

public class DBManager
{
    private static final String TBL_LIST="tbl_list";
    private static final String COL_LIST_ID="list_id";
    private static final String COL_LIST_TITLE="list_title";
    private static final String COL_LIST_ICON="list_icon";

    private static final String TBL_ITEM="tbl_item";
    private static final String COL_ITEM_ID="item_id";
    private static final String COL_ITEM_TITLE="item_title";
    private static final String COL_ITEM_STATE="item_state";
    private static final String COL_REMIND_TIME="remind_time";
    private static final String COL_HOME_SCREEN="is_on_home_screen";

    static final int STATE_DONE=1;
    static final int STATE_REMAINING=0;


    private DBAdapter adapter;
    public DBManager(Context context)
    {
        adapter=new DBAdapter(context);
        adapter.openDataBase();
    }

    public void newList(DataItem item)
    {
        adapter.db.insert(TBL_LIST,null,setListCV(item));
    }

    public void updateList(DataItem item)
    {
        adapter.db.update(TBL_LIST,setListCV(item),COL_LIST_ID+"=?",new String[]{item.getId()+""});
    }

    public void deleteList(int id)
    {
        adapter.db.delete(TBL_ITEM,COL_LIST_ID+"=?",new String[]{id+""});
        adapter.db.delete(TBL_LIST,COL_LIST_ID+"=?",new String[]{id+""});
    }

    public ArrayList<DataItem> getList()
    {
        Cursor cr=adapter.db.query(TBL_LIST,null,null,null,null,null,null);
        ArrayList<DataItem> list=new ArrayList<>();
        while (cr.moveToNext())
            list.add(getListFromCr(cr));
        cr.close();
        return list;
    }

    public DataItem getListById(int id)
    {
        Cursor cr=adapter.db.query(TBL_LIST,null,COL_LIST_ID+"=?",new String[]{id+""},null,null,null);
        if (cr.moveToNext())
            return getListFromCr(cr);

        return null;
    }

    private DataItem getListFromCr(Cursor cr)
    {
        DataItem tmp=new DataItem();
        tmp.setId(cr.getInt(cr.getColumnIndex(COL_LIST_ID)));
        tmp.setTitle(cr.getString(cr.getColumnIndex(COL_LIST_TITLE)));
        tmp.setIcon(cr.getString(cr.getColumnIndex(COL_LIST_ICON)));
        return tmp;
    }



    public void newItem(DataItem item)
    {
        adapter.db.insert(TBL_ITEM,null,setItemCV(item));
    }

    public void updateItem(DataItem item)
    {
        adapter.db.update(TBL_ITEM,setItemCV(item),COL_ITEM_ID+"=?",new String[]{item.getId()+""});
    }

    public void updateState(int id,boolean isDone)
    {
        ContentValues cv=new ContentValues();
        cv.put(COL_ITEM_STATE,isDone?STATE_DONE:STATE_REMAINING);
        adapter.db.update(TBL_ITEM,cv,COL_ITEM_ID+"=?",new String[]{id+""});
    }

    public void deleteItem(DataItem item)
    {
        adapter.db.delete(TBL_ITEM,COL_ITEM_ID+"=?",new String[]{item.getId()+""});
    }

    public DataItem getItemById(int id)
    {
        Cursor cr=adapter.db.query(TBL_ITEM,null,COL_ITEM_ID+"=?",new String[]{id+""},null,null,null);
        if (cr.moveToNext())
            return getItemfromCr(cr);

        return null;
    }

    public ArrayList<DataItem> getListItems(int listId)
    {
        Cursor cr=adapter.db.query(TBL_ITEM,null,COL_LIST_ID+"=?",new String[]{listId+""},null,null,null);
        ArrayList<DataItem> items=new ArrayList<>();
        while (cr.moveToNext())
        {
            items.add(getItemfromCr(cr));
        }
        cr.close();
        return items;
    }


    private DataItem getItemfromCr(Cursor cr)
    {
        DataItem item=new DataItem();
        item.setId(cr.getInt(cr.getColumnIndex(COL_ITEM_ID)));
        item.setTitle(cr.getString(cr.getColumnIndex(COL_ITEM_TITLE)));
        item.setList_id(cr.getInt(cr.getColumnIndex(COL_LIST_ID)));
        item.setRemindTime(Long.parseLong(cr.getString(cr.getColumnIndex(COL_REMIND_TIME))));
        item.setState(cr.getInt(cr.getColumnIndex(COL_ITEM_STATE))==STATE_DONE);
        item.setHomeScreen(cr.getInt(cr.getColumnIndex(COL_HOME_SCREEN))==1);

        return item;
    }

    //get number of remaining tasks in given list

    public String getRemainingTasks(int listId)
    {
        String select=String.format("select count(%s),(select count(%s) from %s where %s=?) as 'total' from %s where %s=? and %s=?",
                COL_ITEM_ID,COL_ITEM_ID,TBL_ITEM,COL_LIST_ID,TBL_ITEM,COL_LIST_ID,COL_ITEM_STATE);
        //Cursor cr=adapter.db.query(TBL_ITEM,new String[]{"count("+COL_ITEM_ID+")"},select,new String[]{listId+"",STATE_REMAINING+""},null,null,null);
        Cursor cr=adapter.db.rawQuery(select,new String[]{listId+"",listId+"",STATE_REMAINING+""});
        cr.moveToNext();
        if (cr.getInt(0)>0)
            select=cr.getInt(0)+" "+adapter.ctx.getString(R.string.item_subtitle_remaining);
        else if (cr.getInt(cr.getColumnIndex("total"))==0)
            select=adapter.ctx.getString(R.string.item_subtitle_no_item);
        else
            select=adapter.ctx.getString(R.string.item_subtitle_all_done);
        cr.close();
        return select;
    }

    private ContentValues setListCV(DataItem item)
    {
        ContentValues cv=new ContentValues();
        cv.put(COL_LIST_TITLE,item.getTitle());
        cv.put(COL_LIST_ICON,item.getIcon());
        return cv;
    }


    private ContentValues setItemCV(DataItem item)
    {
        ContentValues cv=new ContentValues();
        cv.put(COL_ITEM_TITLE,item.getTitle());
        cv.put(COL_LIST_ID,item.getList_id());
        cv.put(COL_ITEM_STATE,item.isState()?1:0);
        cv.put(COL_REMIND_TIME,item.getRemindTime()+"");
        cv.put(COL_HOME_SCREEN,item.isHomeScreen()?1:0);
        return cv;
    }

    public ArrayList<Pair<String,Long>> getReminders()
    {
        ArrayList<Pair<String,Long>> reminders=new ArrayList<>();
        Cursor cr=adapter.db.query(TBL_ITEM,new String[]{COL_ITEM_TITLE,COL_REMIND_TIME},COL_REMIND_TIME+">? and not("+COL_REMIND_TIME+"=0)",new String[]{System.currentTimeMillis()+""},null,null,null);
        while (cr.moveToNext())
        {
            Pair<String,Long> p=new Pair<>(cr.getString(cr.getColumnIndex(COL_ITEM_TITLE)),Long.parseLong(cr.getString(cr.getColumnIndex(COL_REMIND_TIME))));
            reminders.add(p);
        }
        cr.close();
        return reminders;
    }
    public ArrayList<DataItem> getWidgetData()
    {
        Cursor cr=adapter.db.query(TBL_ITEM,null,COL_HOME_SCREEN+"=? and "+COL_ITEM_STATE+"=?",new String[]{"1",STATE_REMAINING+""},null,null,COL_REMIND_TIME);
        ArrayList<DataItem> items=new ArrayList<>();
        while (cr.moveToNext())
        {
            items.add(getItemfromCr(cr));
        }
        return items;
    }

}
