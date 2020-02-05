package com.timehacks.list.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.TimePicker;

import com.timehacks.list.R;
import com.timehacks.list.adapters.AdManager;
import com.timehacks.list.adapters.RecyclerAdapter;
import com.timehacks.list.broadcastReceivers.ReminderReceiver;
import com.timehacks.list.databinding.ActivityAddBinding;
import com.timehacks.list.datamodals.DataItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddActivity extends AppCompatActivity
{
    public static final int MODE_ADD_LIST=0;
    public static final int MODE_EDIT_LIST=1;
    public static final int MODE_ADD_ITEM=2;
    public static final int MODE_EDIT_ITEM=3;

    public static final int REQUEST_CODE_CAPTURE=0;
    public static final int REQUEST_CODE_GALLERY=1;


    public static String EXTRA_MODE="page_mode";
    public static String EXTRA_ITEM_DATA="item_data";
    public static String EXTRA_LIST_ID="listID";

    ActivityAddBinding binding;
    RecyclerAdapter adapter;

    public BottomSheetBehavior behavior;

    int mode;

    DataItem item;

    Calendar calendar;

    Uri customIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTheme(MainActivity.themeId.get());
        binding= DataBindingUtil.setContentView(this, R.layout.activity_add);

        AdManager.setUpBanner(this);
        AdManager.setUpInterstitialAd(this);

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mode=getIntent().getIntExtra(EXTRA_MODE,MODE_ADD_LIST);

        if (mode==MODE_EDIT_LIST || mode==MODE_EDIT_ITEM)
            item= (DataItem) getIntent().getSerializableExtra(EXTRA_ITEM_DATA);
        else
            item=new DataItem();

        if (mode<MODE_ADD_ITEM)
        {
            setIconsDialog();
            String icon="Backpack";
            if (mode==MODE_EDIT_LIST)
            {
                icon=item.getIcon();
                binding.editTitle.setText(item.getTitle());
                adapter.selected=adapter.items.indexOf(item.getIcon());
            }
            binding.icon.setImageBitmap(RecyclerAdapter.getBitmapFrom(this,icon));
            setFabParams(binding.iconChooser.getRoot().getId());
            setIconColorFilter(icon);
        }
        else
        {
            TextView textView = findViewById(R.id.title);
            textView.setText(R.string.add_item);
            binding.timerContainer.setVisibility(View.VISIBLE);
            binding.lblIcon.setVisibility(View.GONE);
            long time=System.currentTimeMillis();
            binding.homeScreenOpt.setChecked(true);
            if (mode==MODE_EDIT_ITEM)
            {
                binding.editTitle.setText(item.getTitle());
                binding.switchReminder.setChecked(item.getRemindTime()!=0);
                binding.homeScreenOpt.setChecked(item.isHomeScreen());
                if (item.getRemindTime()!=0)
                    time=item.getRemindTime();
            }
            binding.setReminderTime(new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault()).format(time));
        }

        binding.setModeEdit(mode==MODE_EDIT_LIST || mode==MODE_EDIT_ITEM);
    }

    public void addList(View view)
    {
        if (TextUtils.isEmpty(binding.editTitle.getText()))
        {
            Snackbar sn=Snackbar.make(view,"Enter Title",Snackbar.LENGTH_LONG);
            sn.getView().setBackgroundColor(Color.RED);
            sn.show();
            return;
        }
        item.setTitle(binding.editTitle.getText().toString());
        if (mode<MODE_ADD_ITEM && customIcon==null)
            item.setIcon(adapter.items.get(adapter.selected).toString());
        else
        {
            if (mode==MODE_ADD_ITEM)
                item.setList_id(getIntent().getIntExtra(EXTRA_LIST_ID,0));


            item.setHomeScreen(binding.homeScreenOpt.isChecked());

            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

            Intent intent = new Intent(getApplicationContext(), ReminderReceiver.class);
            intent.putExtra(EXTRA_ITEM_DATA, item.getId());
            PendingIntent pd = PendingIntent.getBroadcast(getApplicationContext(), item.getId(), intent, PendingIntent.FLAG_ONE_SHOT);
            if (calendar!=null && binding.switchReminder.isChecked())
            {
                manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pd);
                item.setRemindTime(calendar.getTimeInMillis());
            }
            if (mode==MODE_EDIT_ITEM && !binding.switchReminder.isChecked())
            {
                manager.cancel(pd);
                item.setRemindTime(0);
            }
        }
        switch (mode)
        {
            case MODE_ADD_LIST: {
                MainActivity.dbManager.newList(item);
                break;
            }
            case MODE_EDIT_LIST:
                {
                MainActivity.dbManager.updateList(item);
                break;
            }
            case MODE_ADD_ITEM:
            {
                MainActivity.dbManager.newItem(item);
                break;
            }
            case MODE_EDIT_ITEM:
            {
                MainActivity.dbManager.updateItem(item);
                break;
            }
        }
        if (mode>MODE_EDIT_LIST)
            MainActivity.updateWidget(this);

        view.setEnabled(false);
        MainActivity.isChanged=true;
        onBackPressed();
    }

    public void showIconsDialog(View view)
    {
        hideKeyboard(view);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }


    private void setIconsDialog()
    {
        behavior=BottomSheetBehavior.from(binding.iconChooser.getRoot());

        adapter=new RecyclerAdapter(this,RecyclerAdapter.TYPE_ICONS,getIconsFromAssets());
        binding.iconChooser.iconList.setAdapter(adapter);

        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState)
            {
                if (newState==BottomSheetBehavior.STATE_COLLAPSED)
                    if (adapter.selected!=-1 && customIcon==null)
                    {
                        setIconColorFilter(adapter.items.get(adapter.selected).toString());
                        binding.icon.setImageBitmap(RecyclerAdapter.getBitmapFrom(getApplicationContext(), adapter.items.get(adapter.selected).toString()));
                    }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    void setIconColorFilter(String name)
    {
        if(!name.endsWith(".png"))
        {
            TypedArray array=obtainStyledAttributes(new TypedValue().data,new int[]{R.attr.colorPrimaryDark});
            int clr=array.getColor(0,Color.WHITE);
            array.recycle();
            binding.icon.setColorFilter(clr);
        }
        else
            binding.icon.clearColorFilter();
    }

    public void setTime(View view)
    {
        hideKeyboard(view);
        behavior=BottomSheetBehavior.from(binding.reminderPicker.getRoot());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);


        calendar=Calendar.getInstance();
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        binding.reminderPicker.timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                calendar.set(Calendar.HOUR_OF_DAY,i);
                calendar.set(Calendar.MINUTE,i1);
            }
        });
        binding.reminderPicker.setTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                calendar.set(binding.reminderPicker.datePicker.getYear(),binding.reminderPicker.datePicker.getMonth(),binding.reminderPicker.datePicker.getDayOfMonth());
                if (calendar.getTimeInMillis()<=System.currentTimeMillis())
                {
                    Snackbar sn=Snackbar.make(view, R.string.invalid_time_error,Snackbar.LENGTH_LONG);
                    sn.getView().setBackgroundColor(Color.RED);
                    sn.show();
                    return;
                }
                binding.setReminderTime(new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault()).format(calendar.getTimeInMillis()));
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        binding.reminderPicker.down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private ArrayList getIconsFromAssets()
    {
        ArrayList icons=new ArrayList<>();
        try {
            String[] names=getAssets().list("icons");
            for (int i=0;i<names.length;i++)
                if (names[i].endsWith(".png"))
                    icons.add(names[i].substring(0,names[i].indexOf(".")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        File f=new File(getFilesDir()+"/icons");
        if (f.exists())
        {
            String[] files=f.list();
            if (files.length>0) {
                for (int i = 0; i < files.length; i++) {
                    icons.add(files[i]);
                }
            }
        }
        return icons;
    }

    private void setFabParams(int resId)
    {
        CoordinatorLayout.LayoutParams params= (CoordinatorLayout.LayoutParams) binding.fab.getLayoutParams();
        params.setAnchorId(resId);
        binding.fab.setLayoutParams(params);
    }

    private void hideKeyboard(View view)
    {
        InputMethodManager methodManager= (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        methodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }
    @Override
    public void onBackPressed()
    {
        if (behavior!=null && behavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else
        {
            AdManager.onBackPress(this);
            super.onBackPressed();
        }
    }

    public void customImage(View view)
    {
        if (view.getId()==R.id.camera)
        {
            Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            customIcon=FileProvider.getUriForFile(this,getString(R.string.authority),createNewIconFile());
            i.putExtra(MediaStore.EXTRA_OUTPUT,customIcon);
            startActivityForResult(i,REQUEST_CODE_CAPTURE);
        }
        else
        {
            Intent i=new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            startActivityForResult(i,REQUEST_CODE_GALLERY);
        }
    }

    private File createNewIconFile()
    {
        File f=new File(getFilesDir()+"/icons/");
        if (!f.exists())
            f.mkdir();
        f=new File(f.getAbsolutePath()+"/"+System.currentTimeMillis()+".png");
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode!=RESULT_OK)
            return;

        File f;
        Bitmap bitmap = null;
        if (requestCode==REQUEST_CODE_GALLERY)
        {
            try {
                bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
            f=createNewIconFile();
            customIcon=FileProvider.getUriForFile(getApplicationContext(),getString(R.string.authority),f);
        }
        else
        {
            f = new File(getFilesDir() + "/icons/" + customIcon.getLastPathSegment());
            bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(f.getAbsolutePath()), 90, 90, false);
        }
        try {
            FileOutputStream stream=new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        binding.icon.setImageBitmap(bitmap);
        item.setIcon(customIcon.getLastPathSegment());
        setIconColorFilter(item.getIcon());
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }


}
