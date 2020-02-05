package com.timehacks.list.broadcastReceivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import com.timehacks.list.adapters.DBManager;

import java.util.ArrayList;

import static android.content.Context.ALARM_SERVICE;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        DBManager manager=new DBManager(context);

        ArrayList<Pair<String,Long>> ids=manager.getReminders();
        for (int i=0;i<ids.size();i++)
        {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                Intent receiver = new Intent(context, ReminderReceiver.class);
                receiver.putExtra(Intent.EXTRA_TEXT, ids.get(i).first);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, i, receiver, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.set(AlarmManager.RTC, ids.get(i).second, pendingIntent);
        }
    }
}
