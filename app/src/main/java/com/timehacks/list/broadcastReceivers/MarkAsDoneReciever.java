package com.timehacks.list.broadcastReceivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.timehacks.list.R;
import com.timehacks.list.activities.MainActivity;
import com.timehacks.list.adapters.DBManager;

public class MarkAsDoneReciever extends BroadcastReceiver
{
    public static String EXTRA_ITEM_ID="item_id";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        DBManager manager=new DBManager(context);
        int id=intent.getIntExtra(EXTRA_ITEM_ID,0);
        manager.updateState(id,true);

        Toast.makeText(context, R.string.task_complete_msg,Toast.LENGTH_LONG).show();

        MainActivity.updateWidget(context);

        NotificationManager manager1= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager1.cancel(id);

    }
}
