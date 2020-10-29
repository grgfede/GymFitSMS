package com.example.gymfit.system.conf.recycleview;

import androidx.recyclerview.widget.RecyclerView;

public interface OnItemSwipeListener {
    void onItemSwipe(RecyclerView.ViewHolder viewHolder, int position);
}
