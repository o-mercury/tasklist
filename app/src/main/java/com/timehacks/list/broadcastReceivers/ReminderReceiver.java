package com.timehacks.list.broadcastReceivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.timehacks.list.R;
import com.timehacks.list.activities.AddActivity;
import com.timehacks.list.activities.MainActivity;
import com.timehacks.list.adapters.DBManager;
import com.timehacks.list.adapters.RecyclerAdapter;
import com.timehacks.list.datamodals.DataItem;

public class ReminderReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        DBManager dbManagerr=new DBManager(context);

        int id=intent.getIntExtra(AddActivity.EXTRA_ITEM_DATA,-1);
        if (id<0)
            return;
        DataItem item= dbManagerr.getItemById(id);
        if (item==null)
            return;

        DataItem list=dbManagerr.getListById(item.getList_id());
        if (list==null)
            return;


        Intent action=new Intent(context,MainActivity.class);
        action.putExtra(AddActivity.EXTRA_ITEM_DATA,list).putExtra(MainActivity.EXTRA_TYPE,MainActivity.TYPE_LIST_DETAILS);

        PendingIntent pi=PendingIntent.getActivity(context,item.getList_id(),action,PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationManager manager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder notification=new Notification.Builder(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel=new NotificationChannel(context.getPackageName(),"Reminder",NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(true);
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),null);
            manager.createNotificationChannel(channel);
            notification=new Notification.Builder(context,channel.getId());
        }
        notification.setSmallIcon(R.drawable.time)
                .setLargeIcon(RecyclerAdapter.getBitmapFrom(context,list.getIcon()))
                .setContentTitle(item.getTitle())
                .setContentText(list.getTitle())
                .setContentIntent(pi)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH)
        {
            Intent broadcast=new Intent(context,MarkAsDoneReciever.class);
            broadcast.putExtra(MarkAsDoneReciever.EXTRA_ITEM_ID,item.getId());
            PendingIntent actionIntent=PendingIntent.getBroadcast(context,item.getId(),broadcast,0);
            notification.addAction(new Notification.Action(R.drawable.ic_check_black_24dp,context.getString(R.string.notification_action_title),actionIntent));
        }




        Vibrator vibrator= (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));
        else
        {
            vibrator.vibrate(500);
            notification.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        manager.notify(item.getId(),notification.build());
    }
}
