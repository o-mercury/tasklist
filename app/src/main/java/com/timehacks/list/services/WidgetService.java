package com.timehacks.list.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.timehacks.list.R;
import com.timehacks.list.activities.AddActivity;
import com.timehacks.list.activities.MainActivity;
import com.timehacks.list.adapters.DBManager;
import com.timehacks.list.adapters.RecyclerAdapter;
import com.timehacks.list.datamodals.DataItem;

import java.util.ArrayList;


public class WidgetService extends RemoteViewsService
{

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        Log.e("data",intent+"");
        return new AppWidgetListView(getApplicationContext(),intent);
    }

}
class AppWidgetListView implements RemoteViewsService.RemoteViewsFactory
{
    Context context;
    ArrayList<DataItem> data;
    DBManager manager;

    public AppWidgetListView(Context context, Intent intent) {
        this.context=context;
    }

    @Override
    public void onCreate()
    {
        manager= new DBManager(context);
        data=manager.getWidgetData();
    }

    @Override
    public void onDataSetChanged()
    {
        data=manager.getWidgetData();
    }

    @Override
    public void onDestroy()
    {

    }

    @Override
    public int getCount()
    {
        return data.size();
    }

    @Override
    public RemoteViews getViewAt(int i)
    {
        RemoteViews views=new RemoteViews(context.getPackageName(), R.layout.widget_item);
        DataItem lst=manager.getListById(data.get(i).getList_id());
        views.setTextViewText(R.id.text,data.get(i).getTitle());
        views.setTextViewText(R.id.sub_title,lst.getTitle());
        views.setImageViewBitmap(R.id.img, RecyclerAdapter.getBitmapFrom(context,lst.getIcon()));
        Intent fill=new Intent()
                .putExtra(MainActivity.EXTRA_TYPE,MainActivity.TYPE_LIST_DETAILS)
                .putExtra(AddActivity.EXTRA_ITEM_DATA,lst)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        views.setOnClickFillInIntent(R.id.item_view,fill);
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}