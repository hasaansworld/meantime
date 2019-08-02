package com.meantime;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {
    private RecyclerItemTouchHelperListener listener;
    boolean isSwipeEnabled = true;

    public RecyclerItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null && viewHolder instanceof ScheduleAdapter.ViewHolder) {
            final View foregroundView = ((ScheduleAdapter.ViewHolder) viewHolder).foreground;

            getDefaultUIUtil().onSelected(foregroundView);
        }
        else if (viewHolder != null && viewHolder instanceof TrashAdapter.ViewHolder) {
            final View foregroundView = ((TrashAdapter.ViewHolder) viewHolder).foreground;

            getDefaultUIUtil().onSelected(foregroundView);
        }
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
        if(viewHolder instanceof ScheduleAdapter.ViewHolder) {
            final View foregroundView = ((ScheduleAdapter.ViewHolder) viewHolder).foreground;
            getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive);
        }
        else if(viewHolder instanceof TrashAdapter.ViewHolder) {
            final View foregroundView = ((TrashAdapter.ViewHolder) viewHolder).foreground;
            getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive);
        }
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if(viewHolder instanceof ScheduleAdapter.ViewHolder) {
            final View foregroundView = ((ScheduleAdapter.ViewHolder) viewHolder).foreground;
            getDefaultUIUtil().clearView(foregroundView);
        }
        else if(viewHolder instanceof TrashAdapter.ViewHolder){
            final View foregroundView = ((TrashAdapter.ViewHolder) viewHolder).foreground;
            getDefaultUIUtil().clearView(foregroundView);
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {

        if(viewHolder instanceof ScheduleAdapter.ViewHolder) {
            final View foregroundView = ((ScheduleAdapter.ViewHolder) viewHolder).foreground;

            getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive);
        }
        else if(viewHolder instanceof TrashAdapter.ViewHolder){
            final View foregroundView = ((TrashAdapter.ViewHolder) viewHolder).foreground;

            getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive);
            if(dX < 0){
                ((TrashAdapter.ViewHolder) viewHolder).background1.setVisibility(View.GONE);
                ((TrashAdapter.ViewHolder) viewHolder).background2.setVisibility(View.VISIBLE);
            }
            else{
                ((TrashAdapter.ViewHolder) viewHolder).background2.setVisibility(View.GONE);
                ((TrashAdapter.ViewHolder) viewHolder).background1.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    public interface RecyclerItemTouchHelperListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return isSwipeEnabled;
    }

    public void setIsSwipeEnabled(boolean swipeEnabled){
        isSwipeEnabled = swipeEnabled;
    }
}
