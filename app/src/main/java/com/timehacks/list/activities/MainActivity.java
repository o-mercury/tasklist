package com.timehacks.list.activities;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.databinding.ObservableInt;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;


import com.timehacks.list.R;
import com.timehacks.list.adapters.AdManager;
import com.timehacks.list.adapters.DBManager;
import com.timehacks.list.adapters.RecyclerAdapter;
import com.timehacks.list.broadcastReceivers.WidgetReceiver;
import com.timehacks.list.databinding.ActivityMainBinding;
import com.timehacks.list.datamodals.DataItem;
import com.timehacks.list.utils.RecyclerSwipeToDelete;

public class MainActivity extends AppCompatActivity
{
    public static final int TYPE_HOME=0;
    public static final int TYPE_LIST_DETAILS=1;

    public static final String EXTRA_TYPE="type";


    public static DBManager dbManager;
    public static boolean isChanged=false;
    public static ObservableInt themeId=new ObservableInt();
    public static SharedPreferences preferences;

    public ActivityMainBinding binding;

    int type;

    DataItem item;

    public RecyclerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        preferences=getSharedPreferences(getString(R.string.pref_name),MODE_PRIVATE);
        themeId.set(preferences.getInt(getString(R.string.pref_theme),R.style.AppTheme));
        setTheme(themeId.get());

        binding= DataBindingUtil.setContentView(this, R.layout.activity_main);
        if (dbManager==null)
            dbManager = new DBManager(this);

        type=getIntent().getIntExtra(EXTRA_TYPE,TYPE_HOME);
        AdManager.setUpBanner(this);

        if (type==TYPE_LIST_DETAILS)
        {
            item= (DataItem) getIntent().getSerializableExtra(AddActivity.EXTRA_ITEM_DATA);
            binding.setTitle(item.getTitle());
            binding.titleTxt.setPadding(0,0,0,0);
            binding.mainList.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
            AdManager.setUpInterstitialAd(this);
        }
        else
        {
            binding.setTitle(getString(R.string.app_name));
            Drawable drawable=new BitmapDrawable(getApplicationContext().getResources(),RecyclerAdapter.getBitmapFrom(getApplicationContext(),"Cog")).mutate();
            drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            binding.toolbar.getMenu().add("Settings").setIcon(drawable).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {
                    startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
                    return true;
                }
            });
        }

        themeId.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i)
            {
                recreate();
            }
        });
    }


    public void startAdd(View view)
    {
        Intent add=new Intent(this,AddActivity.class);
        if (type==TYPE_LIST_DETAILS)
            add.putExtra(AddActivity.EXTRA_MODE,AddActivity.MODE_ADD_ITEM).putExtra(AddActivity.EXTRA_LIST_ID,item.getId());
        else
            add.putExtra(AddActivity.EXTRA_MODE,AddActivity.MODE_ADD_LIST);
        startActivity(add);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setData();
        ItemTouchHelper helper=new ItemTouchHelper(new RecyclerSwipeToDelete(0,ItemTouchHelper.LEFT, this));
        helper.attachToRecyclerView(binding.mainList);
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        if (!isChanged)
            return;
        setData();
    }

    private void setData()
    {
        if (type==TYPE_HOME)
            adapter=new RecyclerAdapter(this,RecyclerAdapter.TYPE_LIST,dbManager.getList());
        else
            adapter=new RecyclerAdapter(this,RecyclerAdapter.TYPE_ITEM_LIST,dbManager.getListItems(item.getId()));
        binding.mainList.setAdapter(adapter);
    }

    @Override
    public void onBackPressed()
    {
        if (type==TYPE_LIST_DETAILS)
            AdManager.onBackPress(this);
        super.onBackPressed();
    }

    public static void updateWidget(Context context)
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetReceiver.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
    }
}
