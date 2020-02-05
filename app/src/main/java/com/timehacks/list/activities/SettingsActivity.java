package com.timehacks.list.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;


import com.timehacks.list.R;
import com.timehacks.list.adapters.AdManager;
import com.timehacks.list.adapters.DBAdapter;
import com.timehacks.list.adapters.DBManager;
import com.timehacks.list.adapters.RecyclerAdapter;
import com.timehacks.list.broadcastReceivers.ReminderReceiver;
import com.timehacks.list.databinding.ActivitySettingsBinding;
import com.timehacks.list.datamodals.DataItem;

import java.io.File;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity
{

    ActivitySettingsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(MainActivity.themeId.get());
        binding= DataBindingUtil.setContentView(this, R.layout.activity_settings);

        AdManager.setUpBanner(this);
        AdManager.setUpInterstitialAd(this);

        binding.themeChooser.setAdapter(new RecyclerAdapter(this,RecyclerAdapter.TYPE_COLOR_LIST,new ArrayList<DataItem>()));
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void resetApp(View view)
    {
        DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int type)
            {
                if (type==DialogInterface.BUTTON_NEGATIVE) {
                    dialogInterface.cancel();
                    return;
                }

                ArrayList<Pair<String,Long>> ids=MainActivity.dbManager.getReminders();
                for (int i=0;i<ids.size();i++)
                {
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Intent receiver = new Intent(getApplicationContext(), ReminderReceiver.class);
                    receiver.putExtra(Intent.EXTRA_TEXT, ids.get(i).first);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), i, receiver, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.cancel(pendingIntent);
                }

                File f=getDatabasePath(DBAdapter.DB_NAME);
                if (f.exists())
                    f.delete();
                f=new File(getFilesDir()+"/icons");
                if (f.exists())
                {
                    File[] files=f.listFiles();
                    for (int i=0;i<files.length;i++)
                        files[i].delete();
                }
                f.delete();
                MainActivity.preferences.edit().putInt(getString(R.string.pref_theme),R.style.AppTheme).apply();
                MainActivity.preferences.edit().putBoolean(getString(R.string.pref_first_time),true).apply();
                MainActivity.themeId.set(R.style.AppTheme);
                MainActivity.dbManager = new DBManager(getApplicationContext());
                recreate();
            }
        };
        final AlertDialog dialog=new AlertDialog.Builder(this)
                .setTitle(R.string.clear_data)
                .setMessage(R.string.clear_data_msg)
                .setPositiveButton("Yes",listener)
                .setNegativeButton("No",listener)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                ((TextView)dialog.findViewById(android.support.v7.appcompat.R.id.alertTitle)).setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.latoregular));
                ((TextView)dialog.findViewById(android.R.id.message)).setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.latoregular));
            }
        });
        dialog.show();
    }

    public void appOptions(View view)
    {
        Intent i=new Intent(Intent.ACTION_VIEW);
        switch (view.getId())
        {
            case R.id.btn_rate:i.setData(Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName()));
                break;
            case R.id.btn_share:i.setAction(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,"https://play.google.com/store/apps/details?id="+getPackageName());
                break;
            case R.id.btn_more:i.setData(Uri.parse("https://play.google.com/store/apps/developer?id="+getString(R.string.more_apps_url)));
                break;
        }
        startActivity(i);
    }


    @Override
    public void onBackPressed()
    {
        AdManager.onBackPress(this);
        super.onBackPressed();
    }
}
