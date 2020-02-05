package com.timehacks.list.adapters;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.timehacks.list.R;
import com.timehacks.list.activities.AddActivity;
import com.timehacks.list.activities.MainActivity;
import com.timehacks.list.broadcastReceivers.WidgetReceiver;
import com.timehacks.list.databinding.IconListItemBinding;
import com.timehacks.list.databinding.ListDetailItemBinding;
import com.timehacks.list.databinding.ListItemBinding;
import com.timehacks.list.datamodals.DataItem;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Redixbit on 30-01-2018.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public static final int TYPE_ICONS=1;
    public static final int TYPE_LIST=2;
    public static final int TYPE_ITEM_LIST=3;
    public static final int TYPE_COLOR_LIST=4;

    private Context context;

    public int selected=0;
    private int type;

    public ArrayList items;

    public DataItem deletedItem;

    ToggleButton selectedTheme;

    int[] themes= new int[]{R.style.AppTheme
            ,R.style.GreenTheme
            ,R.style.RedTheme
            ,R.style.PurpleTheme
            ,R.style.TealTheme
            ,R.style.GreenBlueTheme
            ,R.style.BlueGrayTheme
            ,R.style.PinkTheme
            ,R.style.DarkBlueTheme
            ,R.style.DarkYellowTheme
            ,R.style.BrownTheme};


    public RecyclerAdapter(Context context, int type, ArrayList<DataItem> items)
    {
        this.context = context;
        this.type = type;
        this.items = items;
    }


    @Override
    public int getItemViewType(int position)
    {
        if ((type==TYPE_LIST|| type==TYPE_ITEM_LIST)  && position==getItemCount()-1)
            return 1;
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        if (viewType==1)
        {
            TextView textView=new TextView(context);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextColor(Color.GRAY);
            textView.setTypeface(ResourcesCompat.getFont(context,R.font.latoregular),Typeface.BOLD);
            textView.setText(R.string.swipe_to_delete);
            int p= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,10,context.getResources().getDisplayMetrics());
            textView.setPadding(p,p,p,p);
            return new HelpViewHolder(textView);
        }
        if (type==TYPE_ICONS)
        {
            IconListItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.icon_list_item, parent, false);
            return new IconViewHolder(binding);
        }
        if (type==TYPE_LIST)
        {
            ListItemBinding binding=DataBindingUtil.inflate(LayoutInflater.from(context),R.layout.list_item,parent,false);
            return new ListItemViewHolder(binding);
        }
        if (type==TYPE_COLOR_LIST)
        {
            View v=LayoutInflater.from(context).inflate(R.layout.theme_chooser,parent,false);
            return new ColorViewHolder(v);
        }
        ListDetailItemBinding binding=DataBindingUtil.inflate(LayoutInflater.from(context),R.layout.list_detail_item,parent,false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
    {
        if (type==TYPE_ICONS)
        {
            IconViewHolder iconViewHolder = (IconViewHolder) holder;
            iconViewHolder.binding.listIcon.setImageBitmap(getBitmapFrom(context,items.get(position).toString()));
            iconViewHolder.binding.setClick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected = position;
                    ((AddActivity) context).behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            });
            return;
        }
        if (getItemViewType(position)==1)
        {
            if (items.size()==0)
                ((TextView)holder.itemView).setText(String.format(context.getString(R.string.nodata_message), type == TYPE_LIST ? "list" : "task"));
            return;
        }
        if (type==TYPE_LIST)
        {
            final DataItem tmp= (DataItem) items.get(position);
            ListItemViewHolder itemViewHolder= (ListItemViewHolder) holder;
            itemViewHolder.binding.setTitle(tmp.getTitle());
            itemViewHolder.binding.setSubTitle(MainActivity.dbManager.getRemainingTasks(tmp.getId()));
            itemViewHolder.binding.icon.setImageBitmap(getBitmapFrom(context,tmp.getIcon()));
            itemViewHolder.binding.editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context,AddActivity.class)
                                            .putExtra(AddActivity.EXTRA_MODE,AddActivity.MODE_EDIT_LIST)
                                            .putExtra(AddActivity.EXTRA_ITEM_DATA,tmp));
                }
            });
            itemViewHolder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    context.startActivity(new Intent(context,MainActivity.class)
                            .putExtra(MainActivity.EXTRA_TYPE,MainActivity.TYPE_LIST_DETAILS)
                            .putExtra(AddActivity.EXTRA_ITEM_DATA,tmp));
                }
            });
        }
        if (type==TYPE_ITEM_LIST)
        {
            final DataItem tmp= (DataItem) items.get(position);
            final ItemViewHolder itemViewHolder= (ItemViewHolder) holder;
            itemViewHolder.binding.setState(tmp.isState());
            itemViewHolder.binding.setTitle(tmp.getTitle());
            View.OnClickListener listener=new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tmp.setState(!itemViewHolder.binding.getState());
                    notifyItemChanged(position);
                    MainActivity.dbManager.updateState(tmp.getId(),!itemViewHolder.binding.getState());
                    MainActivity.isChanged=true;
                    if (!itemViewHolder.binding.getState())
                        Snackbar.make(view, R.string.task_complete_msg,Snackbar.LENGTH_SHORT).show();
                    if (tmp.isHomeScreen())
                        MainActivity.updateWidget(context);
                }
            };
            itemViewHolder.binding.getRoot().setOnClickListener(listener);
            itemViewHolder.binding.itemStateCb.setOnClickListener(listener);
            itemViewHolder.binding.editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context,AddActivity.class)
                            .putExtra(AddActivity.EXTRA_MODE,AddActivity.MODE_EDIT_ITEM)
                            .putExtra(AddActivity.EXTRA_ITEM_DATA,tmp));
                }
            });
        }
        if (type==TYPE_COLOR_LIST)
        {
            ColorViewHolder viewHolder= (ColorViewHolder) holder;
            if (themes[position]==MainActivity.themeId.get())
            {
                selectedTheme=viewHolder.theme;
                selectedTheme.setChecked(true);
            }
            TypedArray array=context.obtainStyledAttributes(themes[position],new int[]{R.attr.colorPrimary});
            int clr=array.getColor(0,Color.WHITE);
            Drawable drawable=getDrawable(clr);
            viewHolder.theme.setBackgroundDrawable(drawable);
        }
    }

    @Override
    public int getItemCount()
    {
        if (type==TYPE_ICONS)
            return items.size();
        else if (type==TYPE_COLOR_LIST)
            return themes.length;
        return items.size()+1;
    }

    public void deleteItem(int pos)
    {
        deletedItem=(DataItem)items.get(pos);
        items.remove(pos);
        if (items.size()==0)
            notifyDataSetChanged();
        else
            notifyItemRemoved(pos);
    }

    public void deletePermanantly()
    {
        if (deletedItem==null)
            return;
        if (type==TYPE_LIST)
            MainActivity.dbManager.deleteList(deletedItem.getId());
        else
            MainActivity.dbManager.deleteItem(deletedItem);
    }

    class IconViewHolder extends RecyclerView.ViewHolder
    {
        IconListItemBinding binding;

        IconViewHolder(IconListItemBinding binding)
        {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    class ListItemViewHolder extends RecyclerView.ViewHolder
    {
        ListItemBinding binding;
        public ListItemViewHolder(ListItemBinding binding)
        {
            super(binding.getRoot());
            this.binding=binding;
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder
    {
        ListDetailItemBinding binding;

        public ItemViewHolder(ListDetailItemBinding binding)
        {
            super(binding.getRoot());
            this.binding = binding;
            Drawable drawable=ContextCompat.getDrawable(context,R.drawable.rounded_box);
            binding.itemStateCb.setBackground(getDrawableCheck(drawable,MainActivity.themeId.get()));
        }
    }

    class HelpViewHolder extends RecyclerView.ViewHolder
    {
        public HelpViewHolder(View itemView)
        {
            super(itemView);
        }
    }

    class ColorViewHolder extends RecyclerView.ViewHolder
    {
        ToggleButton theme;
        public ColorViewHolder(View itemView)
        {
            super(itemView);
            theme= (ToggleButton) itemView;
            theme.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (theme.isChecked())
                    {
                        if (selectedTheme!=null)
                            selectedTheme.setChecked(false);
                        selectedTheme=theme;
                        MainActivity.preferences.edit().putInt(context.getString(R.string.pref_theme),themes[getAdapterPosition()]).apply();
                        MainActivity.themeId.set(themes[getAdapterPosition()]);
                        ((AppCompatActivity)context).recreate();
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetReceiver.class));
                        RemoteViews rv=new RemoteViews(context.getPackageName(),R.layout.widget_layout);

                        int clr=context.getSharedPreferences(context.getString(R.string.pref_name),Context.MODE_PRIVATE).getInt(context.getString(R.string.pref_theme),R.style.AppTheme);
                        TypedArray array=context.obtainStyledAttributes(clr,new int[]{R.attr.colorPrimary});
                        clr=array.getColor(0, Color.WHITE);

                        rv.setInt(R.id.app_name,"setBackgroundColor",clr);

                        appWidgetManager.updateAppWidget(appWidgetIds,rv);

                        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.app_name);
                    }
                }
            });
        }
    }

    Drawable getDrawable(int clr)
    {
        /*Drawable drawable=ContextCompat.getDrawable(context,R.drawable.ic_check_black_24dp);
        StateListDrawable listDrawable=new StateListDrawable();
        LayerDrawable layerDrawable=new LayerDrawable(new Drawable[]{bottom,drawable});
        listDrawable.addState(new int[]{android.R.attr.state_checked},layerDrawable);
        listDrawable.addState(new int[]{}, bottom);
        return listDrawable;*/
        GradientDrawable main_drawable=new GradientDrawable();
        main_drawable.setShape(GradientDrawable.OVAL);
        main_drawable.setColor(clr);

        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_check_black_24dp);
        StateListDrawable stateListDrawable = new StateListDrawable();
        LayerDrawable layerDrawable =new  LayerDrawable(new Drawable[]{main_drawable, drawable});
        stateListDrawable.addState(new int[]{android.R.attr.state_checked}, layerDrawable);
        stateListDrawable.addState(new int[]{}, main_drawable);
        return stateListDrawable;
    }

    public static Bitmap getBitmapFrom(Context context, String title)
    {
        if (title.endsWith(".png"))
            return BitmapFactory.decodeFile(context.getFilesDir()+"/icons/"+title);
        try {
            return BitmapFactory.decodeStream(context.getAssets().open("icons/"+title+".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    Drawable getDrawableCheck(Drawable bottom,int theme)
    {
        Drawable drawable=ContextCompat.getDrawable(context,R.drawable.ic_check_circle_black_24dp);
        TypedArray array=context.obtainStyledAttributes(theme,new int[]{R.attr.colorPrimary});
        int clr=array.getColor(0,Color.WHITE);
        DrawableCompat.setTint(drawable,clr);
        //drawable.setColorFilter(new PorterDuffColorFilter(clr, PorterDuff.Mode.SRC_IN));
        StateListDrawable listDrawable=new StateListDrawable();
        LayerDrawable layerDrawable=new LayerDrawable(new Drawable[]{bottom,drawable});
        listDrawable.addState(new int[]{android.R.attr.state_checked},layerDrawable);
        listDrawable.addState(new int[]{}, bottom);
        return listDrawable;
    }
}
