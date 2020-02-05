package com.timehacks.list.utils;

import android.graphics.Canvas;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.timehacks.list.R;
import com.timehacks.list.activities.MainActivity;

/**
 * Created by Redixbit on 09-02-2018.
 */

public class RecyclerSwipeToDelete extends ItemTouchHelper.SimpleCallback
{

    MainActivity mainActivity;
    Snackbar snackbar;
    boolean isUndo;

    public RecyclerSwipeToDelete(int dragDirs, int swipeDirs,MainActivity mainActivity)
    {
        super(dragDirs, swipeDirs);
        this.mainActivity=mainActivity;
        snackbar=Snackbar.make(mainActivity.binding.getRoot(),"Deleted",Snackbar.LENGTH_LONG);
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
    {
        if (viewHolder.getItemViewType()!=1)
            return ItemTouchHelper.LEFT;
        return super.getSwipeDirs(recyclerView,viewHolder);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction)
    {
        if (snackbar.isShown())
            mainActivity.adapter.deletePermanantly();
        final int pos=viewHolder.getAdapterPosition();
        if (viewHolder.getItemViewType()!=1)
        {
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    mainActivity.adapter.items.add(pos,mainActivity.adapter.deletedItem);
                    snackbar.dismiss();
                    mainActivity.adapter.notifyItemInserted(pos);
                }
            });
            snackbar.addCallback(new Snackbar.Callback()
            {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event)
                {
                    if (event==Snackbar.BaseCallback.DISMISS_EVENT_TIMEOUT)
                    {
                        mainActivity.adapter.deletePermanantly();
                        MainActivity.updateWidget(mainActivity);
                    }
                }
            });
            mainActivity.adapter.deleteItem(viewHolder.getAdapterPosition());
            snackbar.show();
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState)
    {
        if (viewHolder!=null && viewHolder.getItemViewType()!=1)
        {
            View v=viewHolder.itemView.findViewById(R.id.item_view);
            getDefaultUIUtil().onSelected(v);
        }
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
    {
        if (viewHolder!=null && viewHolder.getItemViewType()!=1) {
            View v = viewHolder.itemView.findViewById(R.id.item_view);
            getDefaultUIUtil().clearView(v);
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive)
    {
        if (viewHolder!=null && viewHolder.getItemViewType()!=1)
        {
            View v = viewHolder.itemView.findViewById(R.id.item_view);
            getDefaultUIUtil().onDraw(c, recyclerView,v, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive)
    {
        if (viewHolder!=null && viewHolder.getItemViewType()!=1) {
            View v = viewHolder.itemView.findViewById(R.id.item_view);
            getDefaultUIUtil().onDrawOver(c, recyclerView, v, dX, dY, actionState, isCurrentlyActive);
        }
    }
}
