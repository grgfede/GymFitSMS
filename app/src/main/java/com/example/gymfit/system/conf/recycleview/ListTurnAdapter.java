package com.example.gymfit.system.conf.recycleview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymfit.R;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class ListTurnAdapter extends RecyclerView.Adapter<ListTurnAdapter.MyViewHolder> {
    private final List<String> turns;
    private final Context context;

    private final OnItemClickListener listener;

    public ListTurnAdapter(@NonNull Context context, @NonNull List<String> turns, OnItemClickListener listener) {
        this.turns = turns;
        this.context = context;
        this.listener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView title;
        private final LinearLayout container;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            this.title = itemView.findViewById(R.id.title);
            this.container = itemView.findViewById(R.id.turn_row);
        }

        public void bind(int position, OnItemClickListener listener) {
            this.container.setOnClickListener(v -> listener.onItemClick(this, position));
        }
    }

    @NonNull
    @Override
    public ListTurnAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View view = inflater.inflate(R.layout.layout_recycleview_turn_picker, parent, false);
        return new ListTurnAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListTurnAdapter.MyViewHolder holder, int position) {
        holder.bind(position, this.listener);

        holder.title.setText(this.turns.get(position));
    }

    @Override
    public int getItemCount() {
        return this.turns.size();
    }

    public void removeItem(int position) {
        this.turns.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(@NonNull String item, int position) {
        this.turns.add(position, item);
        notifyItemInserted(position);
    }

}
