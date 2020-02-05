package com.timehacks.list.broadcastReceivers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.timehacks.list.R;
import com.timehacks.list.activities.MainActivity;
import com.timehacks.list.services.WidgetService;

public class WidgetReceiver extends AppWidgetProvider
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
        onUpdate(context,AppWidgetManager.getInstance(context),AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context,WidgetReceiver.class)));
    }

    @Override
    public void onEnabled(Context context)
    {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        int clr=context.getSharedPreferences(context.getString(R.string.pref_name),Context.MODE_PRIVATE).getInt(context.getString(R.string.pref_theme),R.style.AppTheme);
        TypedArray array=context.obtainStyledAttributes(clr,new int[]{R.attr.colorPrimary});
        clr=array.getColor(0, Color.WHITE);
        for (int i=0;i<appWidgetIds.length;i++)
        {
            Intent intent = new Intent(context, WidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            remoteViews.setRemoteAdapter(R.id.widget_list, intent);
            remoteViews.setEmptyView(R.id.widget_list,R.id.empty);
            remoteViews.setInt(R.id.app_name,"setBackgroundColor",clr);


            Intent click=new Intent(context,MainActivity.class);
            remoteViews.setPendingIntentTemplate(R.id.widget_list, PendingIntent.getActivity(context,0,click,0));
            remoteViews.setOnClickPendingIntent(R.id.empty,PendingIntent.getActivity(context,1,new Intent(context,MainActivity.class),PendingIntent.FLAG_CANCEL_CURRENT));


            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }
        super.onUpdate(context,appWidgetManager,appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
    {
        onUpdate(context,appWidgetManager,appWidgetManager.getAppWidgetIds(new ComponentName(context,WidgetReceiver.class)));
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }
}
