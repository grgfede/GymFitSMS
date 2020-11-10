package com.example.gymfit.system.conf.recycleview;

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymfit.R;

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final OnItemSwipeListener actionListener;

    public ItemTouchHelperCallback(OnItemSwipeListener actionListener) {
        this.actionListener = actionListener;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    /**
     * In this method we will get the movement of recycleView item when activated
     *
     * @param recyclerView RecycleView subject of the action
     * @param viewHolder RecycleView Holder subject of the action
     * @return both of swipe and drag action flag
     */
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final int dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        final int swipeFlag = ItemTouchHelper.START;

        return makeMovementFlags(dragFlag, swipeFlag);
    }

    /**
     * In this method when we move our item we just get its old position and send in interface in news
     *
     * @param viewHolder RecycleView Holder subject of the action
     * @param direction direction of action
     */
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        actionListener.onItemSwipe(viewHolder, viewHolder.getAdapterPosition());
    }

    /**
     * In this method we show delete button when we swipe
     *
     * @param c default
     * @param recyclerView default
     * @param viewHolder default
     * @param dX default
     * @param dY default
     * @param actionState default
     * @param isCurrentlyActive default
     */
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        } else {
            final View foregroundView;
            if (viewHolder instanceof ListGymAdapter.MyViewHolder) {
                foregroundView = ((ListGymAdapter.MyViewHolder) viewHolder).cardContainer;
            } else if (viewHolder instanceof ListUserSubscribedAdapter.MyViewHolder) {
                foregroundView = ((ListUserSubscribedAdapter.MyViewHolder) viewHolder).cardContainer;
            } else if (viewHolder instanceof ListSubscriberAdapter.MyViewHolder) {
                foregroundView = ((ListSubscriberAdapter.MyViewHolder) viewHolder).cardContainer;
            } else {
                foregroundView = ((ListTurnPickedAdapter.MyViewHolder) viewHolder).cardContainer;
            }
            getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive);
        }
    }

    /**
     * In this method when we swipe create smooth animation
     *
     * @param c default
     * @param recyclerView default
     * @param viewHolder default
     * @param dX default
     * @param dY default
     * @param actionState default
     * @param isCurrentlyActive default
     */
    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        } else {
            final View foregroundView;
            if (viewHolder instanceof ListGymAdapter.MyViewHolder) {
                foregroundView = ((ListGymAdapter.MyViewHolder) viewHolder).bcContainer;
            } else if (viewHolder instanceof ListUserSubscribedAdapter.MyViewHolder) {
                foregroundView = ((ListUserSubscribedAdapter.MyViewHolder) viewHolder).bcContainer;
            } else if (viewHolder instanceof ListSubscriberAdapter.MyViewHolder) {
                foregroundView = ((ListSubscriberAdapter.MyViewHolder) viewHolder).deleteContainer;
            } else {
                foregroundView = ((ListTurnPickedAdapter.MyViewHolder) viewHolder).deleteContainer;
            }
            getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive);
        }
    }

    /**
     * In this method we clear view when swipe or drag
     *
     * @param recyclerView default
     * @param viewHolder default
     */
    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final View foregroundView;
        if (viewHolder instanceof ListGymAdapter.MyViewHolder) {
            foregroundView = ((ListGymAdapter.MyViewHolder) viewHolder).cardContainer;
            foregroundView.setBackgroundColor(ContextCompat.getColor(((ListGymAdapter.MyViewHolder) viewHolder).cardContainer.getContext(), R.color.quantum_white_100));
        } else if (viewHolder instanceof ListUserSubscribedAdapter.MyViewHolder) {
            foregroundView = ((ListUserSubscribedAdapter.MyViewHolder) viewHolder).cardContainer;
            foregroundView.setBackgroundColor(ContextCompat.getColor(((ListUserSubscribedAdapter.MyViewHolder) viewHolder).cardContainer.getContext(), R.color.quantum_white_100));
        } else if (viewHolder instanceof ListSubscriberAdapter.MyViewHolder) {
            foregroundView = ((ListSubscriberAdapter.MyViewHolder) viewHolder).cardContainer;
            foregroundView.setBackgroundColor(ContextCompat.getColor(((ListSubscriberAdapter.MyViewHolder) viewHolder).cardContainer.getContext(), R.color.quantum_white_100));
        } else {
            foregroundView = ((ListTurnPickedAdapter.MyViewHolder) viewHolder).cardContainer;
            foregroundView.setBackgroundColor(ContextCompat.getColor(((ListTurnPickedAdapter.MyViewHolder) viewHolder).cardContainer.getContext(), R.color.quantum_white_100));
        }
        getDefaultUIUtil().clearView(foregroundView);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

}
